/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.fatjar.loader.launcher;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.anywide.dawdler.fatjar.loader.archive.Archive;
import com.anywide.dawdler.fatjar.loader.archive.jar.NestedJarFile;

import sun.misc.Resource;
import sun.misc.URLClassPath;

public class LaunchedURLClassLoader extends URLClassLoader {
	static {
		ClassLoader.registerAsParallelCapable();
	}

	private final Archive rootArchive;

	private final Object packageLock = new Object();

	private volatile DefinePackageCallType definePackageCallType;

	private URLClassPath ucp;

	public LaunchedURLClassLoader(Archive rootArchive, URL[] urls, ClassLoader parent) {
		super(urls, parent);
		ucp = new URLClassPath(urls, null, null);
		this.rootArchive = rootArchive;
	}

	public Resource getResource(String path, boolean check) {
		return ucp.getResource(path, check);
	}

	public void deployResolveClass(Class<?> clazz) {
		resolveClass(clazz);
	}

	public Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs) {
		return defineClass(name, codeBytes, i, length, cs);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		return new UseFastConnectionExceptionsEnumeration(super.findResources(name));
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			definePackageIfNecessary(name);
		} catch (IllegalArgumentException ex) {
			if (getPackage(name) == null) {
				throw new AssertionError("Package " + name + " has already been defined but it could not be found");
			}
		}
		return super.loadClass(name, resolve);
	}

	private void definePackageIfNecessary(String className) {
		int lastDot = className.lastIndexOf('.');
		if (lastDot >= 0) {
			String packageName = className.substring(0, lastDot);
			if (getPackage(packageName) == null) {
				try {
					definePackage(className, packageName);
				} catch (IllegalArgumentException ex) {
					if (getPackage(packageName) == null) {
						throw new AssertionError(
								"Package " + packageName + " has already been defined but it could not be found");
					}
				}
			}
		}
	}

	private void definePackage(String className, String packageName) {
		String packageEntryName = packageName.replace('.', '/') + "/";
		String classEntryName = className.replace('.', '/') + ".class";
		for (URL url : getURLs()) {
			try {
				URLConnection connection = url.openConnection();
				if (connection instanceof JarURLConnection) {
					 JarURLConnection jarURLConnection = (JarURLConnection) connection;
					JarFile jarFile = jarURLConnection.getJarFile();
					if (jarFile.getEntry(classEntryName) != null && jarFile.getEntry(packageEntryName) != null
							&& jarFile.getManifest() != null) {
						definePackage(packageName, jarFile.getManifest(), url);
						return;
					}
				}
			} catch (IOException ex) {
			}
		}
	}

	@Override
	public Package definePackage(String name, Manifest man, URL url) throws IllegalArgumentException {
		synchronized (this.packageLock) {
			return doDefinePackage(DefinePackageCallType.MANIFEST, () -> super.definePackage(name, man, url));
		}
	}

	@Override
	public Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle,
			String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
		synchronized (this.packageLock) {
			if (this.definePackageCallType == null) {
				Manifest manifest = getManifest(this.rootArchive);
				if (manifest != null) {
					return definePackage(name, manifest, sealBase);
				}
			}
			return doDefinePackage(DefinePackageCallType.ATTRIBUTES, () -> super.definePackage(name, specTitle,
					specVersion, specVendor, implTitle, implVersion, implVendor, sealBase));
		}
	}

	private Manifest getManifest(Archive archive) {
		try {
			return (archive != null) ? archive.getManifest() : null;
		} catch (IOException ex) {
			return null;
		}
	}

	private <T> T doDefinePackage(DefinePackageCallType type, Supplier<T> call) {
		DefinePackageCallType existingType = this.definePackageCallType;
		try {
			this.definePackageCallType = type;
			return call.get();
		} finally {
			this.definePackageCallType = existingType;
		}
	}

	public void clearCache() {
		for (URL url : getURLs()) {
			try {
				URLConnection connection = url.openConnection();
				if (connection instanceof JarURLConnection) {
					clearCache(connection);
				}
			} catch (IOException ex) {
			}
		}

	}

	private void clearCache(URLConnection connection) throws IOException {
		Object jarFile = ((JarURLConnection) connection).getJarFile();
		if (jarFile instanceof NestedJarFile) {
			((NestedJarFile) jarFile).clearCache();
		}
	}

	private static class UseFastConnectionExceptionsEnumeration implements Enumeration<URL> {

		private final Enumeration<URL> delegate;

		UseFastConnectionExceptionsEnumeration(Enumeration<URL> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasMoreElements() {
			return this.delegate.hasMoreElements();
		}

		@Override
		public URL nextElement() {
			return this.delegate.nextElement();
		}

	}

	private enum DefinePackageCallType {
		MANIFEST, ATTRIBUTES
	}

	@Override
	public void close() throws IOException {
		if (ucp != null) {
			ucp.closeLoaders();
		}
		super.close();
	}

	public Class<?> deployFindLoadedClass(String name) {
		return super.findLoadedClass(name);
	}

}

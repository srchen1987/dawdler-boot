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
package com.anywide.dawdler.boot.core.loader;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.anywide.dawdler.core.loader.DeployClassLoader;

import jdk.internal.loader.Resource;
import jdk.internal.loader.URLClassPath;
import jdk.internal.perf.PerfCounter;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerMainClassLoader.java
 * @Description boot模块中jar启动的类加载器(parent)
 * @date 2023年11月15日
 * @email suxuan696@gmail.com
 */
public class DawdlerMainClassLoader extends URLClassLoader implements DeployClassLoader {
	private final URLClassPath ucp;
	private ClassLoader parent;

	public DawdlerMainClassLoader(URL[] urls, ClassLoader parent) throws Exception {
		super(urls, parent);
		this.ucp = new URLClassPath(urls, null, null);
		this.parent = parent;
		loadAspectj();
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz != null) {
			return clazz;
		}
		String path = name.replace('.', '/').concat(".class");
		Resource res = ucp.getResource(path, false);
		if (res != null) {
			try {
				long t0 = System.nanoTime();
				int i = name.lastIndexOf('.');
				URL url = res.getCodeSourceURL();
				if (i != -1) {
					String pkgname = name.substring(0, i);
					Manifest man = res.getManifest();
					definePackageInner(pkgname, man, url);
				}
				java.nio.ByteBuffer codeByteBuffer = res.getByteBuffer();
				CodeSigner[] signers = res.getCodeSigners();
				CodeSource cs = new CodeSource(url, signers);
				PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
				byte[] codeBytes = null;
				if (codeByteBuffer != null) {
					codeByteBuffer.flip();
					codeBytes = codeByteBuffer.array();
				} else {
					codeBytes = res.getBytes();
				}
				return deployDefineClass(name, codeBytes, 0, codeBytes.length, cs);
			} catch (IOException e) {
				throw new ClassNotFoundException(name);
			}
		} else {
			throw new ClassNotFoundException(name);
		}

	}

	@Override
	public ClassLoader classLoader() {
		return parent;
	}

	@Override
	public Package getDeployDefinedPackage(String pkgname) {
		return getDefinedPackage(pkgname);
	}

	@Override
	public Package deployDefinePackage(String name, String specTitle, String specVersion, String specVendor,
			String implTitle, String implVersion, String implVendor, URL sealBase) {
		return definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
	}

	@Override
	public Package deployDefinePackage(String pkgname, Manifest man, URL url) {
		return definePackage(pkgname, man, url);
	}

	@Override
	public Class<?> findClassForDawdler(String name, Resource res, boolean useAop, boolean storeVariableNameByASM) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz != null) {
			return clazz;
		}
		if (res == null) {
			String path = name.replace('.', '/').concat(".class");
			res = ucp.getResource(path, false);
		}
		if (res != null) {
			try {
				return defineClassForDawdler(name, res, useAop, storeVariableNameByASM);
			} catch (IOException e) {
				throw new ClassNotFoundException(name, e);
			}
		} else {
			throw new ClassNotFoundException(name);
		}
	}

	@Override
	public void deployResolveClass(Class<?> clazz) {
		resolveClass(clazz);
	}

	@Override
	public Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs) {
		return defineClass(name, codeBytes, i, length, cs);
	}

	@Override
	public Enumeration<URL> getDeployResources(String name) throws IOException {
		return parent.getResources(name);
	}

	@Override
	public Class<?> deployFindClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			definePackageIfNecessary(name);
		} catch (IllegalArgumentException ex) {
			if (getDefinedPackage(name) == null) {
				throw new AssertionError("Package " + name + " has already been defined but it could not be found");
			}
		}
		try {
			Class<?> clazz = findClass(name);
			if (clazz != null) {
				return clazz;
			}
		} catch (Exception e) {
		}
		return super.loadClass(name, resolve);
	}

	private void definePackageIfNecessary(String className) {
		int lastDot = className.lastIndexOf('.');
		if (lastDot >= 0) {
			String packageName = className.substring(0, lastDot);
			if (getDefinedPackage(packageName) == null) {
				try {
					definePackage(className, packageName);
				} catch (IllegalArgumentException ex) {
					if (getDefinedPackage(packageName) == null) {
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
				if (connection instanceof JarURLConnection jarURLConnection) {
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
	public void close() throws IOException {
		ucp.closeLoaders();
		super.close();
	}

}

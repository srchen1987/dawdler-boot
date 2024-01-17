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
package com.anywide.dawdler.boot.web.loader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.anywide.dawdler.util.XmlObject;
import com.anywide.dawdler.util.XmlTool;
import com.anywide.dawdler.util.aspect.AspectHolder;
import com.anywide.dawdler.util.reflectasm.ParameterNameReader;

import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.misc.PerfCounter;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerMainClassLoader.java
 * @Description boot模块中jar启动的类加载器(parent)
 * @date 2023年11月15日
 * @email suxuan696@gmail.com
 */
public class DawdlerMainClassLoader extends URLClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(DawdlerMainClassLoader.class);
	private final URLClassPath ucp;
	private ClassLoader parent;

	public DawdlerMainClassLoader(URL[] urls, ClassLoader parent) {
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
					definePackageInternal(pkgname, man, url);
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

	public ClassLoader classLoader() {
		return parent;
	}

	public Package getDeployDefinedPackage(String pkgname) {
		return getPackage(pkgname);
	}

	public Package deployDefinePackage(String name, String specTitle, String specVersion, String specVendor,
			String implTitle, String implVersion, String implVendor, URL sealBase) {
		return definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
	}

	public Package deployDefinePackage(String pkgname, Manifest man, URL url) {
		return definePackage(pkgname, man, url);
	}

	public Class<?> findClassForDawdler(String name, Resource res, boolean useAop) throws ClassNotFoundException {
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
				return defineClassForDawdler(name, res, useAop);
			} catch (IOException e) {
				throw new ClassNotFoundException(name, e);
			}
		} else {
			throw new ClassNotFoundException(name);
		}
	}

	public void deployResolveClass(Class<?> clazz) {
		resolveClass(clazz);
	}

	public Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs) {
		return defineClass(name, codeBytes, i, length, cs);
	}

	public Enumeration<URL> getDeployResources(String name) throws IOException {
		return parent.getResources(name);
	}

	public Class<?> deployFindClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			definePackageIfNecessary(name);
		} catch (IllegalArgumentException ex) {
			if (getPackage(name) == null) {
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
	public void close() throws IOException {
		ucp.closeLoaders();
		super.close();
	}

	Class<?> defineClassForDawdler(String name, Resource res, boolean useAop)
			throws IOException, ClassNotFoundException {
		long t0 = System.nanoTime();
		int i = name.lastIndexOf('.');
		URL url = res.getCodeSourceURL();
		if (i != -1) {
			String pkgname = name.substring(0, i);
			Manifest man = res.getManifest();
			definePackageInternal(pkgname, man, url);
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
		return loadClassFromBytes(name, codeBytes, classLoader(), cs, useAop);
	}

	void definePackageInternal(String pkgname, Manifest man, URL url) {
		if (getAndVerifyPackage(pkgname, man, url) == null) {
			try {
				if (man != null) {
					deployDefinePackage(pkgname, man, url);
				} else {
					deployDefinePackage(pkgname, null, null, null, null, null, null, null);
				}
			} catch (IllegalArgumentException iae) {
				if (getAndVerifyPackage(pkgname, man, url) == null) {
					throw new AssertionError("Cannot find package " + pkgname);
				}
			}
		}
	}

	Package getAndVerifyPackage(String pkgname, Manifest man, URL url) {
		Package pkg = getDeployDefinedPackage(pkgname);
		if (pkg != null) {
			if (pkg.isSealed()) {
				if (!pkg.isSealed(url)) {
					throw new SecurityException("sealing violation: package " + pkgname + " is sealed");
				}
			} else {
				if ((man != null) && isSealed(pkgname, man)) {
					throw new SecurityException(
							"sealing violation: can't seal package " + pkgname + ": already loaded");
				}
			}
		}
		return pkg;
	}

	boolean isSealed(String name, Manifest man) {
		String path = name.replace('.', '/').concat("/");
		Attributes attr = man.getAttributes(path);
		String sealed = null;
		if (attr != null) {
			sealed = attr.getValue(Name.SEALED);
		}
		if (sealed == null) {
			if ((attr = man.getMainAttributes()) != null) {
				sealed = attr.getValue(Name.SEALED);
			}
		}
		return "true".equalsIgnoreCase(sealed);
	}

	public Class<?> findClassForDawdler(final String name, boolean useAop) throws ClassNotFoundException {
		return findClassForDawdler(name, null, useAop);
	}

	public Class<?> findClassForDawdler(final String name, boolean resolve, Resource res, boolean useAop)
			throws ClassNotFoundException {
		Class<?> clazz = findClassForDawdler(name, res, useAop);
		if (resolve) {
			deployResolveClass(clazz);
		}
		return clazz;
	}

	public Class<?> loadClassFromBytes(String name, byte[] codeBytes, ClassLoader classLoader, CodeSource cs,
			boolean useAop) throws ClassNotFoundException, IOException {
		if (useAop && AspectHolder.aj != null) {
			try {
				codeBytes = (byte[]) AspectHolder.preProcessMethod.invoke(AspectHolder.aj, name, codeBytes, classLoader,
						null);
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				logger.error("", e);
			}
		}
		Class<?> clazz = deployDefineClass(name, codeBytes, 0, codeBytes.length, cs);
		ParameterNameReader.loadAllDeclaredMethodsParameterNames(clazz, codeBytes);
		return clazz;
	}

	public void loadAspectj() {
		if (AspectHolder.aj != null) {
			try {
				Enumeration<URL> enums = getDeployResources("META-INF/aop.xml");
				while (enums.hasMoreElements()) {
					URL url = enums.nextElement();
					InputStream aopXmlInput = url.openStream();
					try {
						XmlObject xmlo = new XmlObject(aopXmlInput);
						for (Node aspectNode : xmlo.selectNodes("/aspectj/aspects/aspect")) {
							String className = XmlTool.getElementAttribute(aspectNode.getAttributes(), "name");
							if (className != null) {
								findClassForDawdler(className, true);
							}
						}
					} catch (Exception e) {
						logger.error("", e);
					} finally {
						if (aopXmlInput != null) {
							try {
								aopXmlInput.close();
							} catch (IOException e) {
							}
						}
					}
				}
			} catch (IOException e) {
				logger.error("", e);
			}
		} else {
			logger.error("not found aspectjweaver in classpath !");
		}
	}

}

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
package com.anywide.dawdler.boot.server.deploys.loader;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.jar.Manifest;

import com.anywide.dawdler.fatjar.loader.launcher.LaunchedURLClassLoader;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.loader.DeployClassLoader;

import jdk.internal.loader.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerDeployClassLoader.java
 * @Description boot模块中jar启动的类加载器
 * @date 2023年11月15日
 * @email suxuan696@gmail.com
 */
public class DawdlerDeployClassLoader extends SecureClassLoader implements DeployClassLoader {
	private final LaunchedURLClassLoader classLoader;
	private final DawdlerContext dawdlerContext;

	public DawdlerDeployClassLoader(LaunchedURLClassLoader classLoader, DawdlerContext dawdlerContext) {
		super(classLoader);
		this.classLoader = classLoader;
		this.dawdlerContext = dawdlerContext;
	}

	@Override
	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	@Override
	public ClassLoader classLoader() {
		return classLoader;
	}

	@Override
	public Package deployDefinePackage(String name, String specTitle, String specVersion, String specVendor,
			String implTitle, String implVersion, String implVendor, URL sealBase) {
		return classLoader.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor,
				sealBase);
	}

	@Override
	public Package deployDefinePackage(String pkgname, Manifest man, URL url) {
		return classLoader.definePackage(pkgname, man, url);
	}

	@Override
	public Class<?> findClassForDawdler(String name, Resource res, boolean useAop) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz == null) {
			clazz = classLoader.deployFindLoadedClass(name);
		}
		if (clazz != null) {
			return clazz;
		}
		if (res == null) {
			String path = name.replace('.', '/').concat(".class");
			res = classLoader.getResource(path, false);
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

	@Override
	public void deployResolveClass(Class<?> clazz) {
		classLoader.deployResolveClass(clazz);
	}

	@Override
	public Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs) {
		return classLoader.deployDefineClass(name, codeBytes, i, length, cs);
	}

	@Override
	public Enumeration<URL> getDeployResources(String name) throws IOException {
		return classLoader.getResources(name);
	}

	public Package getDeployDefinedPackage(String name) {
		return classLoader.getDefinedPackage(name);
	}

	@Override
	public Class<?> deployFindClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

	@Override
	public void close() throws IOException {
		classLoader.close();
	}

}

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.jar.Manifest;

import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.deploys.loader.DeployClassLoader;

import jdk.internal.loader.Resource;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerDeployClassLoader.java
 * @Description boot模块中项目启动的类加载器
 * @date 2023年11月16日
 * @email suxuan696@gmail.com
 */
public class DawdlerProjectClassLoader extends URLClassLoader implements DeployClassLoader {
	private DawdlerContext dawdlerContext;
	private ClassLoader parent;
	private Method method;

	public DawdlerProjectClassLoader(DawdlerContext dawdlerContext, URL[] urls, ClassLoader parent)
			throws NoSuchMethodException, SecurityException {
		super(urls, parent);
		this.dawdlerContext = dawdlerContext;
		this.parent = parent;
		this.method = parent.getClass().getDeclaredMethod("findClassForDawdler", String.class, Resource.class,
				boolean.class);
	}

	@Override
	public DawdlerContext getDawdlerContext() {
		return dawdlerContext;
	}

	@Override
	public ClassLoader classLoader() {
		return parent;
	}

	@Override
	public Package getDeployDefinedPackage(String pkgname) {
		return null;
	}

	@Override
	public Package deployDefinePackage(String name, String specTitle, String specVersion, String specVendor,
			String implTitle, String implVersion, String implVendor, URL sealBase) {
		return null;
	}

	@Override
	public Package deployDefinePackage(String pkgname, Manifest man, URL url) {
		return null;
	}

	@Override
	public Class<?> findClassForDawdler(String name, Resource res, boolean useAop) throws ClassNotFoundException {
		try {
			return (Class<?>) method.invoke(parent, name, res, useAop);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ClassNotFoundException(name, e);
		}
	}

	@Override
	public void deployResolveClass(Class<?> clazz) {
	}

	@Override
	public Class<?> deployDefineClass(String name, byte[] codeBytes, int i, int length, CodeSource cs) {
		return null;
	}

	@Override
	public Enumeration<URL> getDeployResources(String name) throws IOException {
		return null;
	}

	@Override
	public Class<?> deployFindClass(String name) throws ClassNotFoundException {
		return null;
	}

}

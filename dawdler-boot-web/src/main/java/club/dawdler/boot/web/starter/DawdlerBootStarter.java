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
package club.dawdler.boot.web.starter;

import club.dawdler.boot.core.loader.DawdlerDeployClassLoader;
import club.dawdler.boot.core.loader.DawdlerProjectClassLoader;
import club.dawdler.boot.web.server.WebServer;
import club.dawdler.boot.web.server.WebServerProvider;
import club.dawdler.fatjar.loader.launcher.LaunchedURLClassLoader;
import club.dawdler.util.DawdlerTool;
import club.dawdler.util.JVMTimeProvider;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author jackson.song
 * @version V1.0
 * boot启动器
 */
public class DawdlerBootStarter {
	public static void run(Class<?> startClass, String... args) throws Throwable {
		DawdlerTool.printServerBaseInformation();
		long start = JVMTimeProvider.currentTimeMillis();
		Field field = DawdlerTool.class.getDeclaredField("startClass");
		field.setAccessible(true);
		field.set(null, startClass);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader instanceof LaunchedURLClassLoader) {
			LaunchedURLClassLoader launchedURLClassLoader = (LaunchedURLClassLoader) classLoader;
			DawdlerDeployClassLoader dawdlerDeployClassLoader = new DawdlerDeployClassLoader(launchedURLClassLoader,
					null);
			Thread.currentThread().setContextClassLoader(dawdlerDeployClassLoader);
		} else {
			DawdlerProjectClassLoader dawdlerProjectClassLoader = new DawdlerProjectClassLoader(classLoader);
			Thread.currentThread().setContextClassLoader(dawdlerProjectClassLoader);
		}
		List<WebServer> webServers = WebServerProvider.getWebServers();
		if (webServers.isEmpty()) {
			throw new java.lang.IllegalAccessException("can't found any web runtime container!");
		}
		WebServer webServer = webServers.get(0);

		int port = 0;
		String portString = null;
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (arg.startsWith("--server.port=")) {
					portString = arg.split("=")[1];
				}
			}
		}
		if (portString == null) {
			portString = System.getProperty("server.port");
		}
		if (portString != null) {
			if (!portString.matches("\\d+")) {
				throw new IllegalArgumentException("server.port must be a positive integer!");
			}
			port = Integer.parseInt(portString);
			if (port < 1 || port > 65535)
				throw new IllegalArgumentException("server.port must be between 1 and 65535!");
		}

		if (port > 0) {
			webServer.setPort(port);
		}
		webServer.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					webServer.stop();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		long end = JVMTimeProvider.currentTimeMillis();
		System.out.println("Server startup in " + (end - start) + " ms,Listening port: " + webServer.getPort() + "!");
	}
}

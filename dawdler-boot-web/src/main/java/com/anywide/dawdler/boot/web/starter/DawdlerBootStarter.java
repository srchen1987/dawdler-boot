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
package com.anywide.dawdler.boot.web.starter;

import java.lang.reflect.Field;
import java.util.List;

import com.anywide.dawdler.boot.web.server.WebServer;
import com.anywide.dawdler.boot.web.server.WebServerProvider;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DawdlerBootStarter.java
 * @Description boot启动器
 * @date 2023年11月17日
 * @email suxuan696@gmail.com
 */
public class DawdlerBootStarter {
	public static void run(Class<?> startClass, String... args) throws Throwable {
		DawdlerTool.printServerBaseInformation();
		long start = JVMTimeProvider.currentTimeMillis();
		Field field = DawdlerTool.class.getDeclaredField("startClass");
		field.setAccessible(true);
		field.set(null, startClass);

		List<WebServer> webServers = WebServerProvider.getWebServers();
		if (webServers.isEmpty()) {
			throw new java.lang.IllegalAccessException("can't found any web runtime container!");
		}
		WebServer webServer = webServers.get(0);
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

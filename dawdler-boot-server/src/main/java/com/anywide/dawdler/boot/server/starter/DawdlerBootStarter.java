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
package com.anywide.dawdler.boot.server.starter;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.anywide.dawdler.boot.server.deploys.ServiceRoot;
import com.anywide.dawdler.core.health.Status;
import com.anywide.dawdler.server.bootstrap.DawdlerServer;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfigParser;
import com.anywide.dawdler.util.DawdlerTool;

/**
 * @author jackson.song
 * @version V1.0
 * boot启动器
 */
public class DawdlerBootStarter {
	public static void run(Class<?> startClass, String... args) throws Exception {
		int port = 0;
		String portString = null;
		int shutdownPort = 0;
		String shutdownPortString = null;
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (arg.startsWith("--server.port=")) {
					portString = arg.split("=")[1];
				}
				if (arg.startsWith("--shutdown.port=")) {
					shutdownPortString = arg.split("=")[1];
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
		if (shutdownPortString == null) {
			shutdownPortString = System.getProperty("shutdown.port");
		}
		if (shutdownPortString != null) {
			if (!shutdownPortString.matches("\\d+")) {
				throw new IllegalArgumentException("shutdown.port must be a positive integer!");
			}
			shutdownPort = Integer.parseInt(shutdownPortString);
			if (shutdownPort < 1 || shutdownPort > 65535)
				throw new IllegalArgumentException("shutdown.port must be between 1 and 65535!");
		}

		InputStream input = startClass.getResourceAsStream("/server-conf.xml");
		if (input == null) {
			throw new FileNotFoundException("not found server-conf.xml in classPath!");
		}
		ServerConfigParser serverConfigParser;
		try {
			serverConfigParser = new ServerConfigParser(input);
		} finally {
			input.close();
		}
		ServerConfig serverConfig = serverConfigParser.getServerConfig();
		if (port > 0) {
			serverConfig.getServer().setTcpPort(port);
		}
		if (shutdownPort > 0) {
			serverConfig.getServer().setTcpShutdownPort(shutdownPort);
		}
		if (serverConfig.getServer().getTcpPort() == serverConfig.getServer().getTcpShutdownPort()) {
			throw new IllegalArgumentException("server.port and shutdown.port must be different!");
		}
		DawdlerTool.printServerBaseInformation();
		ServiceRoot serviceRoot = new ServiceRoot(startClass);
		DawdlerServer dawdlerServer = new DawdlerServer(serverConfig,
				serviceRoot);
		if (Status.UP.equals(serviceRoot.getStatus())) {
			dawdlerServer.start();
		}
	}

}

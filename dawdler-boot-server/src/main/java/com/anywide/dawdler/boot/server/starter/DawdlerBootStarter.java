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
 * @Title DawdlerBootStarter.java
 * @Description boot启动器
 * @date 2023年11月16日
 * @email suxuan696@gmail.com
 */
public class DawdlerBootStarter {
	public static void run(Class<?> startClass, String... args) throws Exception {
		InputStream input = startClass.getResourceAsStream("/server-conf.xml");
		if (input == null) {
			throw new FileNotFoundException("not found server-conf.xml in classPath!");
		}
		ServerConfigParser serverConfigParser = new ServerConfigParser(input);
		input.close();
		DawdlerTool.printServerBaseInformation();
		ServerConfig serverConfig = serverConfigParser.getServerConfig();
		ServiceRoot serviceRoot = new ServiceRoot(startClass);
		DawdlerServer dawdlerServer = new DawdlerServer(serverConfig, serviceRoot);
		if (Status.UP.equals(serviceRoot.getStatus())) {
			dawdlerServer.start();
		}
	}
}

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
package com.anywide.dawdler.boot.web.server;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author jackson.song
 * @version V1.0
 * web器提供者通过SPI接入
 */
public class WebServerProvider {
	private static final List<WebServer> WEB_SERVERS = new ArrayList<>();
	static {
		ServiceLoader.load(WebServer.class).forEach(webServer -> {
			WEB_SERVERS.add(webServer);
		});
	}

	public static List<WebServer> getWebServers() {
		return WEB_SERVERS;
	}

}

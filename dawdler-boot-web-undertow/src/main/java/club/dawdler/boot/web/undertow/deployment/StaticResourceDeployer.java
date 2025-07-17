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
package club.dawdler.boot.web.undertow.deployment;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import club.dawdler.boot.web.undertow.config.UndertowConfig;
import club.dawdler.boot.web.undertow.resource.JarResourceManager;
import club.dawdler.util.DawdlerTool;

import io.undertow.server.handlers.cache.DirectBufferCache;
import io.undertow.server.handlers.resource.CachingResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.api.DeploymentInfo;

/**
 * @author jackson.song
 * @version V1.0
 * 静态资源文件
 */
public class StaticResourceDeployer implements UndertowDeployer {

	@Override
	public void deploy(UndertowConfig undertowConfig, DeploymentInfo deploymentInfo) throws URISyntaxException {
		String staticLocation = undertowConfig.getStaticLocation();
		if (staticLocation != null && !staticLocation.trim().equals("")) {
			URL staticLocationURL = DawdlerTool.getStartClass().getResource(staticLocation);
			if (staticLocationURL != null) {
				ResourceManager resourceManager;
				if (staticLocationURL.getProtocol().equals("jar")) {
					resourceManager = new JarResourceManager(staticLocationURL);
				} else {
					resourceManager = new PathResourceManager(Paths.get(staticLocationURL.toURI()));
				}
				DirectBufferCache directBufferCache = new DirectBufferCache(16, 32, 1024);
				CachingResourceManager cachingResourceManager = new CachingResourceManager(16, 2048, directBufferCache,
						resourceManager, -1);
				deploymentInfo.setResourceManager(cachingResourceManager);
			}
		}

	}

}

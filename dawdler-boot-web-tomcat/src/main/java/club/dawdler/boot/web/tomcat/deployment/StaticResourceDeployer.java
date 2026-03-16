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
package club.dawdler.boot.web.tomcat.deployment;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import club.dawdler.boot.web.tomcat.config.TomcatConfig;
import club.dawdler.boot.web.tomcat.resource.JarResourceManager;
import club.dawdler.util.DawdlerTool;

/**
 * @author jackson.song
 * @version V1.0
 * 静态资源文件
 */
public class StaticResourceDeployer implements TomcatDeployer {

	@Override
	public void deploy(TomcatConfig tomcatConfig, Context context) throws Exception {
		String staticLocation = tomcatConfig.getStaticLocation();
		if (staticLocation != null && !staticLocation.trim().equals("")) {
			if (!staticLocation.startsWith("/")) {
				staticLocation = "/" + staticLocation;
			}
			URL staticLocationURL = DawdlerTool.getStartClass().getResource(staticLocation);
			if (staticLocationURL != null) {
				WebResourceRoot resources = context.getResources();
				if (resources == null) {
					resources = new StandardRoot(context);
				}
				StandardRoot standardRoot = (StandardRoot) resources;
				URI uri = DawdlerTool.getStartClass().getResource(staticLocation).toURI();
				if (uri.getScheme().equals("file")) {
					File staticDir = new File(uri);
					standardRoot.addPreResources(new DirResourceSet(standardRoot, "/", staticDir.getAbsolutePath(), "/"));
				} else if (uri.getScheme().equals("jar")) {
					String jarPath = uri.toString();
					String internalPath = "/";
					int separatorIndex = jarPath.indexOf("!/");
					if (separatorIndex != -1) {
						internalPath = jarPath.substring(separatorIndex + 1);
						jarPath = jarPath.substring(0, separatorIndex + 2);
					}
					standardRoot.addJarResources(new JarResourceManager(standardRoot,jarPath, internalPath, staticLocation));
				}

				context.setResources(resources);
				resources.start();
			}
		}
	}

}
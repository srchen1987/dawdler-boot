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

import org.apache.catalina.Context;

import club.dawdler.boot.web.tomcat.config.TomcatConfig;

/**
 * @author jackson.song
 * @version V1.0
 * 基础配置
 */
public class BaseConfigDeployer implements TomcatDeployer {

	@Override
	public void deploy(TomcatConfig tomcatConfig, Context context) throws Exception {
		String contextPath = tomcatConfig.getContextPath();
		if (contextPath != null && !contextPath.isEmpty()) {
			context.setPath(contextPath);
		}
		String deployName = tomcatConfig.getDeployName();
		if (deployName != null) {
			context.setDisplayName(deployName);
		}
	}

}
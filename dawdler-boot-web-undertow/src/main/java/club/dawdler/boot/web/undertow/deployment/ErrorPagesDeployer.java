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

import java.util.Map;

import club.dawdler.boot.web.undertow.config.UndertowConfig;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ErrorPage;

/**
 * @author jackson.song
 * @version V1.0
 * 错误页面
 */
public class ErrorPagesDeployer implements UndertowDeployer {

	@Override
	public void deploy(UndertowConfig undertowConfig, DeploymentInfo deploymentInfo) {
		Map<Integer, String> errorPages = undertowConfig.getErrorPages();
		if (errorPages != null) {
			errorPages.forEach((k, v) -> {
				deploymentInfo.addErrorPage(new ErrorPage(v, k));
			});
		}
	}

}

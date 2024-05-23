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
package com.anywide.dawdler.boot.web.undertow.deployment;

import java.util.List;

import com.anywide.dawdler.boot.web.undertow.config.UndertowConfig;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

/**
 * @author jackson.song
 * @version V1.0
 * servlet
 */
public class ServletDeployer implements UndertowDeployer {

	@Override
	public void deploy(UndertowConfig undertowConfig, DeploymentInfo deploymentInfo) {
		List<Class<Servlet>> httpServletList = getScanServletComponent().getHttpServletList();
		httpServletList.forEach(servlet -> {
			WebServlet webServlet = servlet.getAnnotation(WebServlet.class);
			String servletName = webServlet.name();
			ServletInfo servletInfo;
			if (servletName.equals("")) {
				servletName = servlet.getName();
			}
			servletInfo = Servlets.servlet(servletName, servlet);
			String[] values = webServlet.value();
			String[] patterns = webServlet.urlPatterns();
			for (String mapping : values) {
				servletInfo.addMapping(mapping);
			}
			for (String mapping : patterns) {
				servletInfo.addMapping(mapping);
			}
			servletInfo.setAsyncSupported(webServlet.asyncSupported());

			WebInitParam[] webInitParams = webServlet.initParams();
			for (WebInitParam webInitParam : webInitParams) {
				servletInfo.addInitParam(webInitParam.name(), webInitParam.value());
			}
			servletInfo.setLoadOnStartup(webServlet.loadOnStartup());
			servletInfo.setEnabled(true);
			deploymentInfo.addServlet(servletInfo);
		});
	}

}

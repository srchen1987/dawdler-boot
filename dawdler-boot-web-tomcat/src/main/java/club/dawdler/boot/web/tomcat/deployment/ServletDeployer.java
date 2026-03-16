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

import java.util.List;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;

import club.dawdler.boot.web.tomcat.config.TomcatConfig;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

/**
 * @author jackson.song
 * @version V1.0
 *          servlet
 */
public class ServletDeployer implements TomcatDeployer {

	@Override
	public void deploy(TomcatConfig tomcatConfig, Context context) throws Exception {
		List<Class<Servlet>> httpServletList = getScanServletComponent().getHttpServletList();
		for (Class<Servlet> servlet : httpServletList) {
			WebServlet webServlet = servlet.getAnnotation(WebServlet.class);
			String servletName = webServlet.name();
			if (servletName.equals("")) {
				servletName = servlet.getName();
			}

			Wrapper wrapper = context.createWrapper();
			wrapper.setServletClass(servlet.getName());
			wrapper.setName(servletName);
			wrapper.setLoadOnStartup(webServlet.loadOnStartup());
			wrapper.setAsyncSupported(webServlet.asyncSupported());

			context.addChild(wrapper);

			String[] values = webServlet.value();
			String[] patterns = webServlet.urlPatterns();

			String[] allMappings = new String[values.length + patterns.length];
			System.arraycopy(values, 0, allMappings, 0, values.length);
			System.arraycopy(patterns, 0, allMappings, values.length, patterns.length);

			for (String mapping : allMappings) {
				if (mapping != null && !mapping.trim().isEmpty()) {
					context.addServletMappingDecoded(mapping, servletName);
				}
			}

			WebInitParam[] webInitParams = webServlet.initParams();
			for (WebInitParam webInitParam : webInitParams) {
				wrapper.addInitParameter(webInitParam.name(), webInitParam.value());
			}

		}
	}

}
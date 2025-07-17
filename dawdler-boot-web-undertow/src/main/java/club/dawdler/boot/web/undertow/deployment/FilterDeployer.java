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

import java.util.List;

import club.dawdler.boot.web.undertow.config.UndertowConfig;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * @author jackson.song
 * @version V1.0
 * 过滤器
 */
public class FilterDeployer implements UndertowDeployer {

	@Override
	public void deploy(UndertowConfig undertowConfig, DeploymentInfo deploymentInfo) {
		List<Class<Filter>> httpFilterList = getScanServletComponent().getWebFilterList();
		httpFilterList.forEach(filter -> {
			WebFilter webFilter = filter.getAnnotation(WebFilter.class);
			String filterName = webFilter.filterName();
			if (filterName.trim().equals("")) {
				filterName = filter.getSimpleName();
			}
			FilterInfo filterInfo = Servlets.filter(filterName, filter).setAsyncSupported(webFilter.asyncSupported());
			WebInitParam[] webInitParams = webFilter.initParams();
			for (WebInitParam webInitParam : webInitParams) {
				filterInfo.addInitParam(webInitParam.name(), webInitParam.value());
			}
			deploymentInfo.addFilter(filterInfo);
			for (DispatcherType dispatcherType : webFilter.dispatcherTypes()) {
				for (String urlPattern : webFilter.urlPatterns()) {
					deploymentInfo.addFilterUrlMapping(filterName, urlPattern, dispatcherType);
				}
				for (String urlPattern : webFilter.value()) {
					deploymentInfo.addFilterUrlMapping(filterName, urlPattern, dispatcherType);
				}
				for (String servletName : webFilter.servletNames()) {
					deploymentInfo.addFilterServletNameMapping(filterName, servletName, dispatcherType);
				}
			}
		});

	}

}

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
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import club.dawdler.boot.web.tomcat.config.TomcatConfig;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * @author jackson.song
 * @version V1.0
 * 过滤器
 */
public class FilterDeployer implements TomcatDeployer {

	@Override
	public void deploy(TomcatConfig tomcatConfig, Context context) throws Exception {
		List<Class<Filter>> httpFilterList = getScanServletComponent().getWebFilterList();
		for (Class<Filter> filter : httpFilterList) {
			WebFilter webFilter = (WebFilter) filter.getAnnotation(WebFilter.class);
			String filterName = webFilter.filterName();
			if (filterName.trim().equals("")) {
				filterName = filter.getSimpleName();
			}
			FilterDef filterDef = new FilterDef();
			filterDef.setFilterClass(filter.getName());
			filterDef.setFilterName(filterName);
			filterDef.setAsyncSupported(webFilter.asyncSupported() ? "true" : "false");

			WebInitParam[] webInitParams = webFilter.initParams();
			for (WebInitParam webInitParam : webInitParams) {
				filterDef.addInitParameter(webInitParam.name(), webInitParam.value());
			}

			context.addFilterDef(filterDef);
			
			FilterMap filterMap = new FilterMap();
			filterMap.setFilterName(filterName);
			
			for (DispatcherType dispatcherType : webFilter.dispatcherTypes()) {
				filterMap.setDispatcher(dispatcherType.name());
			}
			
			for (String urlPattern : webFilter.urlPatterns()) {
				filterMap.addURLPattern(urlPattern);
			}
			for (String urlPattern : webFilter.value()) {
				filterMap.addURLPattern(urlPattern);
			}
			
			for (String servletName : webFilter.servletNames()) {
				filterMap.addServletName(servletName);
			}
			
			if (filterMap.getURLPatterns() != null && filterMap.getURLPatterns().length > 0 ||
				filterMap.getServletNames() != null && filterMap.getServletNames().length > 0) {
				context.addFilterMap(filterMap);
			}
		}
	}

}

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
package club.dawdler.boot.web.server.component;

import club.dawdler.clientplug.web.classloader.ClientPlugClassLoader;
import club.dawdler.core.scan.DawdlerComponentScanner;
import club.dawdler.core.scan.component.reader.ClassStructureParser;
import club.dawdler.core.scan.component.reader.ClassStructureParser.ClassStructure;
import club.dawdler.util.DawdlerTool;
import club.dawdler.util.spring.antpath.Resource;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author jackson.song
 * @version V1.0
 * web组件提供者 支持 servlet filter listener
 * ServletContainerInitializer实现类的@HandlesTypes
 */
public class ServletComponentProvider {
	private static final ServletComponentProvider INSTANCE = new ServletComponentProvider();
	private final List<Class<Servlet>> HTTP_SERVLET_LIST = new ArrayList<>();
	private final List<Class<Filter>> WEB_FILTER_LIST = new ArrayList<>();
	private final List<Class<EventListener>> EVENT_LISTENER_LIST = new ArrayList<>();
	private final List<Class<?>> END_POINT_LIST = new ArrayList<>();

	private ServletComponentProvider() {
	}

	public static ServletComponentProvider getInstance() {
		return INSTANCE;
	}

	public void scanComponent(String[] packagePaths,
							  Map<ServletContainerInitializer, ServletContainerInitializerData> servletContainerInitializerMap)
			throws IOException {
		ClientPlugClassLoader clientPlugClassLoader = ClientPlugClassLoader.newInstance(DawdlerTool.getCurrentPath());
		Map<String, Resource> removeDuplicates = new LinkedHashMap<>();
		if (packagePaths != null) {
			for (String packageInClasses : packagePaths) {
				Resource[] resources = DawdlerComponentScanner.getClasses(packageInClasses);
				for (Resource resource : resources) {
					removeDuplicates.putIfAbsent(resource.getURL().toString(), resource);
				}
			}
		}

		Collection<Resource> resources = removeDuplicates.values();
		for (Resource resource : resources) {
			InputStream input = null;
			try {
				input = resource.getInputStream();
				ClassStructure classStructure = ClassStructureParser.parser(input);
				if (classStructure != null) {
					Set<String> interfaces = classStructure.getInterfaces();
					Set<String> annotationNames = classStructure.getAnnotationNames();
					if (annotationNames.contains(WebServlet.class.getName())
							&& classStructure.getSuperClasses().contains(HttpServlet.class.getName())) {
						Class<Servlet> clazz = (Class<Servlet>) clientPlugClassLoader
								.defineClass(classStructure.getClassName(), resource, true);
						HTTP_SERVLET_LIST.add(clazz);
					} else if (annotationNames.contains(WebFilter.class.getName())
							&& interfaces.contains(Filter.class.getName())) {
						Class<Filter> clazz = (Class<Filter>) clientPlugClassLoader
								.defineClass(classStructure.getClassName(), resource, true);
						WEB_FILTER_LIST.add(clazz);
					} else if (annotationNames.contains(WebListener.class.getName())
							&& (interfaces.contains(ServletContextListener.class.getName())
							|| interfaces.contains(ServletContextAttributeListener.class.getName())
							|| interfaces.contains(HttpSessionListener.class.getName())
							|| interfaces.contains(HttpSessionAttributeListener.class.getName())
							|| interfaces.contains(HttpSessionBindingListener.class.getName())
							|| interfaces.contains(HttpSessionActivationListener.class.getName())
							|| interfaces.contains(ServletRequestListener.class.getName())
							|| interfaces.contains(ServletRequestAttributeListener.class.getName()))) {
						Class<EventListener> clazz = (Class<EventListener>) clientPlugClassLoader
								.defineClass(classStructure.getClassName(), resource, true);
						EVENT_LISTENER_LIST.add(clazz);
					} else if (annotationNames.contains(ServerEndpoint.class.getName())) {
						Class<?> clazz = clientPlugClassLoader.defineClass(classStructure.getClassName(), resource,
								true);
						END_POINT_LIST.add(clazz);
					} else {
						for (ServletContainerInitializerData data : servletContainerInitializerMap.values()) {
							for (Class<?> type : data.getHandlesTypesInterfaceSet()) {
								if (classStructure.getInterfaces().contains(type.getName())
										|| classStructure.getSuperClasses().contains(type.getName())) {
									Class<?> clazz = clientPlugClassLoader.defineClass(classStructure.getClassName(),
											resource, true);
									Set<Class<?>> impls = data.getHandlesTypesImplMap().get(type);
									if (impls == null) {
										impls = new LinkedHashSet<>();
										Set<Class<?>> preImpls = data.getHandlesTypesImplMap().putIfAbsent(type, impls);
										if (preImpls != null) {
											impls = preImpls;
										}
									}
									impls.add(clazz);
								}
							}
						}
					}
				}
			} finally {
				if (input != null) {
					input.close();
				}
			}
		}
	}

	public List<Class<Servlet>> getHttpServletList() {
		return HTTP_SERVLET_LIST;
	}

	public List<Class<Filter>> getWebFilterList() {
		return WEB_FILTER_LIST;
	}

	public List<Class<EventListener>> getEventListenerList() {
		return EVENT_LISTENER_LIST;
	}

	public List<Class<?>> getEndPointList() {
		return END_POINT_LIST;
	}

}

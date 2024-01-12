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
package com.anywide.dawdler.boot.server.deploys;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.boot.server.annotation.DawdlerBootApplication;
import com.anywide.dawdler.core.health.Status;
import com.anywide.dawdler.fatjar.loader.launcher.LaunchedURLClassLoader;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.conf.ServerConfig.Server;
import com.anywide.dawdler.server.context.DawdlerServerContext;
import com.anywide.dawdler.server.deploys.AbstractServiceRoot;
import com.anywide.dawdler.server.deploys.Service;
import com.anywide.dawdler.server.deploys.loader.DeployClassLoader;
import com.anywide.dawdler.server.loader.DawdlerClassLoader;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.JVMTimeProvider;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ServiceRoot.java
 * @Description boot模块的根实现(改造后继承AbstractServiceRoot)
 * @date 2023年11月15日
 * @email suxuan696@gmail.com
 */
public class ServiceRoot extends AbstractServiceRoot {
	public ServiceRoot(Class<?> startClass) throws Exception {
		Field field = DawdlerTool.class.getDeclaredField("startClass");
		field.setAccessible(true);
		field.set(null, startClass);
	}

	private static final Logger logger = LoggerFactory.getLogger(ServiceRoot.class);
	private Service service;

	@Override
	public void initApplication(DawdlerServerContext dawdlerServerContext) throws Exception {
		DawdlerBootApplication dawdlerBootServerApplication = DawdlerTool.getStartClass()
				.getAnnotation(DawdlerBootApplication.class);
		ServerConfig serverConfig = dawdlerServerContext.getServerConfig();
		Server server = serverConfig.getServer();
		boolean healthCheck = serverConfig.getHealthCheck().isCheck();
		if (healthCheck) {
			servicesHealth = new ConcurrentHashMap<>(16);
		}
		initWorkPool(server.getMaxThreads(), server.getQueueCapacity(), server.getKeepAliveMilliseconds());
		String deployName = dawdlerBootServerApplication.serviceName();
		long start = JVMTimeProvider.currentTimeMillis();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			if (classLoader instanceof LaunchedURLClassLoader launchedURLClassLoader) {
				service = new ServiceBase(deployName, serverConfig, launchedURLClassLoader,
						dawdlerServerContext.getStartSemaphore(), dawdlerServerContext.getStarted());
			} else {
				service = new ProjectServiceBase(deployName, new URL[0], serverConfig, classLoader,
						dawdlerServerContext.getStartSemaphore(), dawdlerServerContext.getStarted());
			}
			if (healthCheck) {
				servicesHealth.put(deployName, service);
			}
			service.start();
			service.status(Status.UP);
			SERVICES.put(deployName, service);
			long end = JVMTimeProvider.currentTimeMillis();
			System.out.println(deployName + " startup in " + (end - start) + " ms,Listening port: "
					+ dawdlerServerContext.getServerConfig().getServer().getTcpPort() + "!");
			if (healthCheck) {
				startHttpServer(serverConfig);
			}
		} catch (Throwable e) {
			logger.error("", e);
			System.err.println(deployName + " startup failed!");
			if (service != null) {
				service.status(Status.DOWN);
				service.cause(e);
				service.prepareStop();
				service.stop();
			}
		}

	}

	@Override
	public void prepareDestroyedApplication() {
		service.prepareStop();
	}

	@Override
	public void destroyedApplication() {
		service.stop();
		releaseResource();
	}

	public String getStatus() {
		if (service != null) {
			return service.getStatus();
		}
		return null;
	}

	public ClassLoader createServerClassLoader() {
		return DawdlerClassLoader.createLoader(null, Thread.currentThread().getContextClassLoader());
	}

	public void closeClassLoader() {
		try {
			((DeployClassLoader) service.getClassLoader()).close();
		} catch (IOException e) {
		}
	}

}

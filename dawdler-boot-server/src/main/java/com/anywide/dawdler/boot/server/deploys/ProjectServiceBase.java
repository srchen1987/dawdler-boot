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

import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.anywide.dawdler.boot.server.deploys.loader.DawdlerProjectClassLoader;
import com.anywide.dawdler.server.conf.ServerConfig;
import com.anywide.dawdler.server.deploys.AbstractService;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ProjectServiceBase.java
 * @Description main方法运行项目中的服务模块具体实现类
 * @date 2023年11月15日
 * @email suxuan696@gmail.com
 */
public class ProjectServiceBase extends AbstractService {

	public ProjectServiceBase(String deployName, URL[] urls, ServerConfig serverConfig, ClassLoader parent,
			Semaphore startSemaphore, AtomicBoolean started) throws Exception {
		super(serverConfig, deployName, startSemaphore, started);
		this.deployName = deployName;
		classLoader = new DawdlerProjectClassLoader(dawdlerContext, urls, parent);
		resetContextClassLoader();
		dawdlerContext.initServicesConfig();
	}

}

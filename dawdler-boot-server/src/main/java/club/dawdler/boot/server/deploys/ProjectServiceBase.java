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
package club.dawdler.boot.server.deploys;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import club.dawdler.boot.core.loader.DawdlerProjectClassLoader;
import club.dawdler.server.conf.ServerConfig;
import club.dawdler.server.deploys.AbstractService;

/**
 * @author jackson.song
 * @version V1.0
 * main方法运行项目中的服务模块具体实现类
 */
public class ProjectServiceBase extends AbstractService {

	public ProjectServiceBase(String deployName, ServerConfig serverConfig, ClassLoader parent,
			Semaphore startSemaphore, AtomicBoolean started) throws Exception {
		super(serverConfig, deployName, startSemaphore, started);
		this.deployName = deployName;
		classLoader = new DawdlerProjectClassLoader(dawdlerContext, parent);
		resetContextClassLoader();
		dawdlerContext.initServicesConfig();
	}

}

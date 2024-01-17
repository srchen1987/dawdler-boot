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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.anywide.dawdler.boot.web.undertow.config.UndertowConfig;
import com.anywide.dawdler.util.DawdlerTool;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletStackTraces;

/**
 * @author jackson.song
 * @version V1.0
 * @Title BaseConfigDeployer.java
 * @Description 基础配置
 * @date 2023年11月18日
 * @email suxuan696@gmail.com
 */
public class BaseConfigDeployer implements UndertowDeployer {

	@Override
	public void deploy(UndertowConfig undertowConfig, DeploymentInfo deploymentInfo) throws IOException {
		String contextPath = undertowConfig.getContextPath();
		String deployName = undertowConfig.getDeployName();
		deploymentInfo.setClassLoader(DawdlerTool.getStartClass().getClassLoader());
		deploymentInfo.setContextPath(contextPath == null ? "" : contextPath);
		deploymentInfo.setDisplayName(deployName == null ? "" : deployName);
		deploymentInfo.setDeploymentName("dawdler-boot-web");
		deploymentInfo.setServletStackTraces(ServletStackTraces.NONE);
		deploymentInfo.setTempDir(createTempDir("undertow", undertowConfig.getServer().getPort()));
	}

	private File createTempDir(String prefix, int port) throws IOException {
		try {
			File tempDir = Files.createTempDirectory(prefix + "." + port + ".").toFile();
			tempDir.deleteOnExit();
			return tempDir;
		} catch (IOException ex) {
			throw new IOException(
					"Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex);
		}
	}
}

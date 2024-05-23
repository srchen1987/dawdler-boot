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

import java.util.EventListener;
import java.util.List;

import com.anywide.dawdler.boot.web.undertow.config.UndertowConfig;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;

/**
 * @author jackson.song
 * @version V1.0
 * 监听器
 */
public class ListenerDeployer implements UndertowDeployer {

	@Override
	public void deploy(UndertowConfig undertowConfig, DeploymentInfo deploymentInfo) {
		List<Class<EventListener>> eventListenerList = getScanServletComponent().getEventListenerList();
		eventListenerList.forEach(listener -> {
			deploymentInfo.addListener(Servlets.listener(listener));
		});
	}

}

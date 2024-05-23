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
package com.anywide.dawdler.boot.web.server.component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * 存放ServletContainerInitializer实现类的@HandlesTypes关系
 */
public class ServletContainerInitializerData {

	private Set<Class<?>> HANDLES_TYPES_INTERFACE_SET = new HashSet<Class<?>>();

	private Map<Class<?>, Set<Class<?>>> HANDLES_TYPES_IMPL_MAP = new ConcurrentHashMap<>();

	public Set<Class<?>> getHandlesTypesInterfaceSet() {
		return HANDLES_TYPES_INTERFACE_SET;
	}

	public Map<Class<?>, Set<Class<?>>> getHandlesTypesImplMap() {
		return HANDLES_TYPES_IMPL_MAP;
	}

}

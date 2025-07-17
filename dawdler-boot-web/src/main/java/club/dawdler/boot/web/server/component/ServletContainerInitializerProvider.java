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

import club.dawdler.core.annotation.Order;
import club.dawdler.core.order.OrderComparator;
import club.dawdler.core.order.OrderData;

import javax.servlet.ServletContainerInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author jackson.song
 * @version V1.0
 * ServletContainerInitializer提供者SPI方式接入
 */
public class ServletContainerInitializerProvider {
	private final static List<OrderData<ServletContainerInitializer>> SERVLET_CONTAINER_INITIALIZERS = new ArrayList<>();

	static {
		ServiceLoader.load(ServletContainerInitializer.class).forEach(initializer -> {
			OrderData<ServletContainerInitializer> orderData = new OrderData<>();
			Order order = initializer.getClass().getAnnotation(Order.class);
			if (order != null) {
				orderData.setOrder(order.value());
			}
			orderData.setData(initializer);
			SERVLET_CONTAINER_INITIALIZERS.add(orderData);
		});
		OrderComparator.sort(SERVLET_CONTAINER_INITIALIZERS);
	}

	public static List<OrderData<ServletContainerInitializer>> getServletcontainerinitializers() {
		return SERVLET_CONTAINER_INITIALIZERS;
	}

}

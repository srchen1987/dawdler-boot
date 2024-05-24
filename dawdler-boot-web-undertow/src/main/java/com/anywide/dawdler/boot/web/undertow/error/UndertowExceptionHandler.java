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
package com.anywide.dawdler.boot.web.undertow.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @author jackson.song
 * @version V1.0
 * 异常处理器
 */
public class UndertowExceptionHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(UndertowExceptionHandler.class);
	private final HttpHandler handler;

	public UndertowExceptionHandler(HttpHandler handler) {
		this.handler = handler;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		try {
			handler.handleRequest(exchange);
		} catch (Throwable throwable) {
			logger.error("", throwable);
			throw throwable;
		}

	}

}

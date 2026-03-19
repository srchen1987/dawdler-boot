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
package club.dawdler.boot.web.undertow.handlers;

import java.util.concurrent.Semaphore;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 * @author jackson.song
 * @version V1.0
 * 基于Semaphore实现的简单限流Handler，限制同时处理的请求数量，超过限制时返回503状态码
 */
public class RateLimitHandler implements HttpHandler {
	private final Semaphore requestLimiter;
	private final HttpHandler next;

	public RateLimitHandler(HttpHandler next, int maxConcurrentRequests) {
		this.next = next;
		this.requestLimiter = new Semaphore(maxConcurrentRequests);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (!requestLimiter.tryAcquire()) {
			exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
			exchange.getResponseSender().send("Service Unavailable");
			return;
		}

		exchange.addExchangeCompleteListener((exchange1, nextListener) -> {
			requestLimiter.release();
			nextListener.proceed();
		});

		next.handleRequest(exchange);
	}
}
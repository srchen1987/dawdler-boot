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
package club.dawdler.boot.web.tomcat.valves;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import javax.servlet.ServletException;

/**
 * @author jackson.song
 * @version V1.0
 * 基于Semaphore实现的简单限流Valve，限制同时处理的请求数量，超过限制时返回503状态码
 */
public class RateLimitValve extends ValveBase {
	private final Semaphore requestLimiter;

	public RateLimitValve(int maxConcurrentRequests) {
		this.requestLimiter = new Semaphore(maxConcurrentRequests);
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		if (!requestLimiter.tryAcquire()) {
			response.sendError(503, "Service Unavailable");
			return;
		}

		try {
			getNext().invoke(request, response);
		} finally {
			requestLimiter.release();
		}
	}
}
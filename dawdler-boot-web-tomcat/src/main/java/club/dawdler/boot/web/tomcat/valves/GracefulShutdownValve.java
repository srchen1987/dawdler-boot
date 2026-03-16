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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import jakarta.servlet.ServletException;

/**
 * @author jackson.song
 * @version V1.0
 * 优雅停机的Valve，在停机时返回503状态码，提示服务不可用
 */
public class GracefulShutdownValve extends ValveBase {
	private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		if (shuttingDown.get()) {
			response.setStatus(503);
			response.getWriter().write("Service Unavailable");
			return;
		}
		getNext().invoke(request, response);
	}

	public void setShuttingDown(boolean shuttingDown) {
		this.shuttingDown.set(shuttingDown);
	}

}
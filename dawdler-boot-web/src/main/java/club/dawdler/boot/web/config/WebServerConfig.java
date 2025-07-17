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
package club.dawdler.boot.web.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jackson.song
 * @version V1.0
 * web服务的配置类
 */
public abstract class WebServerConfig {

	protected Server server = new Server();

	protected Compression compression = new Compression();

	@JsonProperty("context-path")
	protected String contextPath;

	@JsonProperty("deploy-name")
	protected String deployName;

	@JsonProperty("static-location")
	protected String staticLocation;

	@JsonProperty("component-package-paths")
	protected String[] componentPackagePaths;

	@JsonProperty("error-pages")
	private Map<Integer, String> errorPages;

	public String getDeployName() {
		return deployName;
	}

	public void setDeployName(String deployName) {
		this.deployName = deployName;
	}

	public String getStaticLocation() {
		return staticLocation;
	}

	public void setStaticLocation(String staticLocation) {
		this.staticLocation = staticLocation;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public Map<Integer, String> getErrorPages() {
		return errorPages;
	}

	public void setErrorPages(Map<Integer, String> errorPages) {
		this.errorPages = errorPages;
	}

	public String[] getComponentPackagePaths() {
		return componentPackagePaths;
	}

	public void setComponentPackagePaths(String[] componentPackagePaths) {
		this.componentPackagePaths = componentPackagePaths;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Compression getCompression() {
		return compression;
	}

	public void setCompression(Compression compression) {
		this.compression = compression;
	}

	public static class Server {

		protected String host;

		protected int port = 8080;

		protected boolean http2;

		@JsonProperty("graceful-shutdown")
		protected boolean gracefulShutdown = true;

		@JsonProperty("graceful-shutdown-timeout")
		protected long gracefulShutdownTimeout = 30000;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public boolean isHttp2() {
			return http2;
		}

		public void setHttp2(boolean http2) {
			this.http2 = http2;
		}

		public boolean isGracefulShutdown() {
			return gracefulShutdown;
		}

		public void setGracefulShutdown(boolean gracefulShutdown) {
			this.gracefulShutdown = gracefulShutdown;
		}

		public long getGracefulShutdownTimeout() {
			return gracefulShutdownTimeout;
		}

		public void setGracefulShutdownTimeout(long gracefulShutdownTimeout) {
			this.gracefulShutdownTimeout = gracefulShutdownTimeout;
		}

	}

	public static class Compression {

		private boolean enabled = false;

		@JsonProperty("mime-types")
		private Set<String> mimeTypes = new HashSet<>();
		@JsonProperty("min-response-size")
		private long minResponseSize = 1024 * 64;

		public Compression() {
			mimeTypes.add("text/html");
			mimeTypes.add("text/xml");
			mimeTypes.add("text/plain");
			mimeTypes.add("text/css");
			mimeTypes.add("text/javascript");
			mimeTypes.add("application/javascript");
			mimeTypes.add("application/json");
			mimeTypes.add("application/xml");
		}

		public boolean getEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Set<String> getMimeTypes() {
			return this.mimeTypes;
		}

		public void setMimeTypes(Set<String> mimeTypes) {
			this.mimeTypes = mimeTypes;
		}

		public long getMinResponseSize() {
			return this.minResponseSize;
		}

		public void setMinResponseSize(long minSize) {
			this.minResponseSize = minSize;
		}
	}
}

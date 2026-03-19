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
package club.dawdler.boot.web.tomcat.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import club.dawdler.boot.web.config.WebServerConfig;

/**
 * @author jackson.song
 * @version V1.0
 * tomcat服务的配置类
 */
public class TomcatConfig extends WebServerConfig {

	private TomcatSettings tomcat = new TomcatSettings();

	@JsonProperty("access-log")
	private AccessLog accessLog;

	public TomcatSettings getTomcat() {
		return tomcat;
	}

	public void setTomcat(TomcatSettings tomcat) {
		this.tomcat = tomcat;
	}

	public AccessLog getAccessLog() {
		return accessLog;
	}

	public void setAccessLog(AccessLog accessLog) {
		this.accessLog = accessLog;
	}

	public static class TomcatSettings {

		@JsonProperty("tomcat-options")
		private Map<String, Object> tomcatOptions = new HashMap<>();

		@JsonProperty("max-threads")
		private Integer maxThreads;

		@JsonProperty("min-spare-threads")
		private Integer minSpareThreads;

		@JsonProperty("accept-count")
		private Integer acceptCount;

		@JsonProperty("max-connections")
		private Integer maxConnections;

		@JsonProperty("max-header-count")
		private Integer maxHeaderCount;

		@JsonProperty("max-queue-size")
		private Integer maxQueueSize;

		@JsonProperty("connection-timeout")
		private Integer connectionTimeout;

		@JsonProperty("keep-alive-timeout")
		private Integer keepAliveTimeout;

		@JsonProperty("graceful-shutdown-response-timeout")
		private long gracefulShutdownResponseTimeout = 3000L;

		@JsonProperty("virtual-thread")
		private Boolean virtualThread;

		@JsonProperty("max-concurrent-requests")
		private Integer maxConcurrentRequests;

		public Map<String, Object> getTomcatOptions() {
			return tomcatOptions;
		}

		public void setTomcatOptions(Map<String, Object> tomcatOptions) {
			this.tomcatOptions = tomcatOptions;
		}

		public Integer getMaxThreads() {
			return maxThreads;
		}

		public void setMaxThreads(Integer maxThreads) {
			this.maxThreads = maxThreads;
		}

		public Integer getMinSpareThreads() {
			return minSpareThreads;
		}

		public void setMinSpareThreads(Integer minSpareThreads) {
			this.minSpareThreads = minSpareThreads;
		}

		public Integer getAcceptCount() {
			return acceptCount;
		}

		public void setAcceptCount(Integer acceptCount) {
			this.acceptCount = acceptCount;
		}

		public Integer getMaxConnections() {
			return maxConnections;
		}

		public void setMaxConnections(Integer maxConnections) {
			this.maxConnections = maxConnections;
		}

		public Integer getMaxHeaderCount() {
			return maxHeaderCount;
		}

		public void setMaxHeaderCount(Integer maxHeaderCount) {
			this.maxHeaderCount = maxHeaderCount;
		}

		public Integer getMaxQueueSize() {
			return maxQueueSize;
		}

		public void setMaxQueueSize(Integer maxQueueSize) {
			this.maxQueueSize = maxQueueSize;
		}

		public Integer getConnectionTimeout() {
			return connectionTimeout;
		}

		public void setConnectionTimeout(Integer connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
		}

		public Integer getKeepAliveTimeout() {
			return keepAliveTimeout;
		}

		public void setKeepAliveTimeout(Integer keepAliveTimeout) {
			this.keepAliveTimeout = keepAliveTimeout;
		}

		public long getGracefulShutdownResponseTimeout() {
			return gracefulShutdownResponseTimeout;
		}

		public void setGracefulShutdownResponseTimeout(long gracefulShutdownResponseTimeout) {
			this.gracefulShutdownResponseTimeout = gracefulShutdownResponseTimeout;
		}

		public Boolean getVirtualThread() {
			return virtualThread;
		}

		public void setVirtualThread(Boolean virtualThread) {
			this.virtualThread = virtualThread;
		}

		public Integer getMaxConcurrentRequests() {
			return maxConcurrentRequests;
		}

		public void setMaxConcurrentRequests(Integer maxConcurrentRequests) {
			this.maxConcurrentRequests = maxConcurrentRequests;
		}
	}

	public static class AccessLog {

		private boolean enabled = false;

		private String pattern = "common";

		protected String prefix = "access_log.";

		private String suffix = "log";

		private String dir = "logs";

		private boolean rotate = true;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getPattern() {
			return this.pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}

		public String getPrefix() {
			return this.prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getSuffix() {
			return this.suffix;
		}

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		public String getDir() {
			return this.dir;
		}

		public void setDir(String dir) {
			this.dir = dir;
		}

		public boolean isRotate() {
			return this.rotate;
		}

		public void setRotate(boolean rotate) {
			this.rotate = rotate;
		}

	}

}
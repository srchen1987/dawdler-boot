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
package club.dawdler.boot.web.undertow.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import club.dawdler.boot.web.config.WebServerConfig;

/**
 * @author jackson.song
 * @version V1.0
 * undertow服务的配置类
 */
public class UndertowConfig extends WebServerConfig {

	private Undertow undertow = new Undertow();

	@JsonProperty("access-log")
	private AccessLog accessLog;

	@JsonProperty("web-socket-byte-buffer-pool")
	private WebSocketByteBufferPool webSocketByteBufferPool = new WebSocketByteBufferPool();

	public Undertow getUndertow() {
		return undertow;
	}

	public void setUndertow(Undertow undertow) {
		this.undertow = undertow;
	}

	public AccessLog getAccessLog() {
		return accessLog;
	}

	public void setAccessLog(AccessLog accessLog) {
		this.accessLog = accessLog;
	}

	public WebSocketByteBufferPool getWebSocketByteBufferPool() {
		return webSocketByteBufferPool;
	}

	public void setWebSocketByteBufferPool(WebSocketByteBufferPool webSocketByteBufferPool) {
		this.webSocketByteBufferPool = webSocketByteBufferPool;
	}

	public static class Undertow {

		@JsonProperty("undertow-options")
		private Map<String, Object> undertowOptions = new HashMap<>();

		@JsonProperty("socket-options")
		private Map<String, Object> socketOptions = new HashMap<>();

		@JsonProperty("buffer-size")
		private Integer bufferSize;

		@JsonProperty("io-threads")
		private Integer ioThreads;

		@JsonProperty("worker-threads")
		private Integer workerThreads;

		@JsonProperty("direct-buffers")
		private Boolean directBuffers;

		@JsonProperty("max-concurrent-requests")
		private Integer maxConcurrentRequests;

		public Integer getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(Integer bufferSize) {
			this.bufferSize = bufferSize;
		}

		public Integer getIoThreads() {
			return ioThreads;
		}

		public void setIoThreads(Integer ioThreads) {
			this.ioThreads = ioThreads;
		}

		public Integer getWorkerThreads() {
			return workerThreads;
		}

		public void setWorkerThreads(Integer workerThreads) {
			this.workerThreads = workerThreads;
		}

		public Boolean getDirectBuffers() {
			return directBuffers;
		}

		public void setDirectBuffers(Boolean directBuffers) {
			this.directBuffers = directBuffers;
		}

		public Map<String, Object> getUndertowOptions() {
			return undertowOptions;
		}

		public void setUndertowOptions(Map<String, Object> serverOptions) {
			this.undertowOptions = serverOptions;
		}

		public Map<String, Object> getSocketOptions() {
			return socketOptions;
		}

		public void setSocketOptions(Map<String, Object> socketOptions) {
			this.socketOptions = socketOptions;
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

		/**
		 * private static String handleCommonNames(String formatString) {
		 * if(formatString.equals("common")) { return "%h %l %u %t \"%r\" %s %b"; } else
		 * if (formatString.equals("combined")) { return "%h %l %u %t \"%r\" %s %b
		 * \"%{i,Referer}\" \"%{i,User-Agent}\""; } else
		 * if(formatString.equals("commonobf")) { return "%o %l %u %t \"%r\" %s %b"; }
		 * else if (formatString.equals("combinedobf")) { return "%o %l %u %t \"%r\" %s
		 * %b \"%{i,Referer}\" \"%{i,User-Agent}\""; } return formatString; }
		 */
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

	public static class WebSocketByteBufferPool {
		private boolean direct;
		@JsonProperty("buffer-size")
		private int bufferSize = 2048;
		@JsonProperty("maximum-pool-size")
		private int maximumPoolSize = -1;
		@JsonProperty("thread-local-cache-size")
		private int threadLocalCacheSize = 12;
		@JsonProperty("leak-detection-percent")
		private int leakDecetionPercent = 0;

		public boolean isDirect() {
			return direct;
		}

		public void setDirect(boolean direct) {
			this.direct = direct;
		}

		public int getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
		}

		public int getMaximumPoolSize() {
			return maximumPoolSize;
		}

		public void setMaximumPoolSize(int maximumPoolSize) {
			this.maximumPoolSize = maximumPoolSize;
		}

		public int getThreadLocalCacheSize() {
			return threadLocalCacheSize;
		}

		public void setThreadLocalCacheSize(int threadLocalCacheSize) {
			this.threadLocalCacheSize = threadLocalCacheSize;
		}

		public int getLeakDecetionPercent() {
			return leakDecetionPercent;
		}

		public void setLeakDecetionPercent(int leakDecetionPercent) {
			this.leakDecetionPercent = leakDecetionPercent;
		}

	}

}

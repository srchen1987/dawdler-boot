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
package club.dawdler.boot.web.tomcat.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import club.dawdler.boot.web.config.WebServerConfig.Compression;
import club.dawdler.boot.web.config.WebServerConfig.Ssl;
import club.dawdler.boot.web.server.WebServer;
import club.dawdler.boot.web.server.component.ServletComponentProvider;
import club.dawdler.boot.web.server.component.ServletContainerInitializerData;
import club.dawdler.boot.web.server.component.ServletContainerInitializerProvider;
import club.dawdler.boot.web.tomcat.config.TomcatConfig;
import club.dawdler.boot.web.tomcat.config.TomcatConfig.AccessLog;
import club.dawdler.boot.web.tomcat.deployment.TomcatDeployer;
import club.dawdler.boot.web.tomcat.deployment.TomcatDeployerProvider;
import club.dawdler.boot.web.tomcat.loader.BootWebappClassLoader;
import club.dawdler.boot.web.tomcat.valves.GracefulShutdownValve;
import club.dawdler.boot.web.tomcat.valves.RateLimitValve;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFireHolder;
import club.dawdler.core.order.OrderData;
import club.dawdler.fatjar.loader.archive.jar.NestedJarURLStreamHandler;
import club.dawdler.util.DawdlerTool;
import club.dawdler.util.YAMLMapperFactory;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletException;

/**
 * @author jackson.song
 * @version V1.0
 * tomcat实现的web服务器
 */
public class TomcatWebServer implements WebServer {
	static {
		TomcatURLStreamHandlerFactory.getInstance().addUserFactory(new CustomURLStreamHandlerFactory());
	}

	public static class CustomURLStreamHandlerFactory implements URLStreamHandlerFactory {
		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
			if ("jar".equals(protocol)) {
				return new NestedJarURLStreamHandler();
			}
			return null;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TomcatWebServer.class);
	private final AtomicBoolean started = new AtomicBoolean();
	private Tomcat tomcat;
	private Class<?> startClass;
	private TomcatConfig tomcatConfig;
	private final ServletComponentProvider scanServletComponent = ServletComponentProvider.getInstance();
	private final List<OrderData<RemoteClassLoaderFire>> fireList = RemoteClassLoaderFireHolder.getInstance()
			.getRemoteClassLoaderFire();
	private Integer port;
	private Integer sslPort;
	private long gracefulShutdownTimeout;
	private club.dawdler.boot.web.config.WebServerConfig.Server serverConfig;
	private StandardContext context;
	private AccessLogValve accessLogValve;
	private GracefulShutdownValve shutdownValve;
	private Boolean virtualThread;

	@Override
	public void start() throws Throwable {
		startClass = DawdlerTool.getStartClass();
		if (started.compareAndSet(false, true)) {

			YAMLMapper yamlMapper = YAMLMapperFactory.getYAMLMapper();
			InputStream input = startClass.getResourceAsStream("/tomcat.yml");
			try {
				if (input != null) {
					tomcatConfig = yamlMapper.readValue(input, TomcatConfig.class);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
			if (tomcatConfig == null) {
				tomcatConfig = new TomcatConfig();
				logger.info("Using default Tomcat configuration");
			}

			Map<ServletContainerInitializer, ServletContainerInitializerData> servletContainerInitializerMap = new LinkedHashMap<>();
			ServletContainerInitializerProvider.getServletcontainerinitializers().forEach(initializer -> {
				ServletContainerInitializerData data = new ServletContainerInitializerData();
				jakarta.servlet.annotation.HandlesTypes handlesTypes = initializer.getClass()
						.getAnnotation(jakarta.servlet.annotation.HandlesTypes.class);
				if (handlesTypes != null && handlesTypes.value().length > 0) {
					for (Class<?> clazz : handlesTypes.value()) {
						data.getHandlesTypesInterfaceSet().add(clazz);
					}
				}
				servletContainerInitializerMap.put(initializer.getData(), data);
			});

			if (tomcatConfig.getComponentPackagePaths() != null) {
				scanServletComponent.scanComponent(tomcatConfig.getComponentPackagePaths(),
						servletContainerInitializerMap);
			}

			serverConfig = tomcatConfig.getServer();
			if (port == null) {
				port = serverConfig.getPort();
			}
			if (sslPort == null) {
				sslPort = tomcatConfig.getSsl().getSslPort();
			}
			tomcat = new Tomcat();
			File baseDir = createTempDir("tomcat", port);
			baseDir.deleteOnExit();
			tomcat.setBaseDir(baseDir.getAbsolutePath());
			tomcat.setPort(port);
			Host tomcatHost = tomcat.getHost();
			String host = serverConfig.getHost();
			tomcat.setHostname(host == null ? "0.0.0.0" : host);
			shutdownValve = new GracefulShutdownValve();
			tomcat.getHost().getPipeline().addValve(shutdownValve);

			Integer maxConcurrentRequests = tomcatConfig.getTomcat().getMaxConcurrentRequests();
			if (maxConcurrentRequests != null && maxConcurrentRequests > 0) {
				RateLimitValve rateLimitValve = new RateLimitValve(maxConcurrentRequests);
				tomcat.getHost().getPipeline().addValve(rateLimitValve);
			}

			context = (StandardContext) tomcat.addContext("", baseDir.getAbsolutePath());
			context.setWorkDir(baseDir.getAbsolutePath());
			context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
			WebappLoader webappLoader = new WebappLoader();
			BootWebappClassLoader bootWebappClassLoader = new BootWebappClassLoader(
					Thread.currentThread().getContextClassLoader());
			webappLoader.setLoaderInstance(bootWebappClassLoader);
			context.setLoader(webappLoader);
			Wrapper defaultServlet = context.createWrapper();
			defaultServlet.setName("defaultServlet");
			defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
			defaultServlet.addInitParameter("debug", "0");
			defaultServlet.addInitParameter("listings", "false");
			defaultServlet.setLoadOnStartup(Integer.MAX_VALUE);
			context.addChild(defaultServlet);
			addServletContainerInitializers(context, servletContainerInitializerMap);
			List<OrderData<TomcatDeployer>> deployers = TomcatDeployerProvider.getInstance().getDeployers();
			for (OrderData<TomcatDeployer> orderData : deployers) {
				orderData.getData().deploy(tomcatConfig, context);
			}
			context.addServletMappingDecoded("/", "defaultServlet");

			TomcatConfig.TomcatSettings tomcatSettings = tomcatConfig.getTomcat();
			this.virtualThread = tomcatSettings.getVirtualThread();
			Connector httpConnector = null;

			if (tomcatConfig.getServer().isHttpEnabled()) {
				httpConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
				httpConnector.setPort(port);
				httpConnector.setSecure(false);
				httpConnector.setScheme("http");
				configureConnector(httpConnector);
				tomcat.getService().addConnector(httpConnector);
			}

			Connector httpsConnector = null;
			if (tomcatConfig.getSsl().isSslEnabled()) {
				httpsConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
				httpsConnector.setPort(sslPort);
				configureSSL(httpsConnector);
				configureConnector(httpsConnector);
				tomcat.getService().addConnector(httpsConnector);
			}

			if (httpsConnector != null) {
				tomcat.setConnector(httpsConnector);
			} else if (httpConnector != null) {
				tomcat.setConnector(httpConnector);
			} else {
				Connector defaultConnector = tomcat.getConnector();
				defaultConnector.setPort(port);
				configureConnector(defaultConnector);
				tomcat.setConnector(defaultConnector);
			}

			configureAccessLog(tomcatHost);
			injectServlet(context);
			injectListener(context);
			injectFilter(context);
			tomcat.getServer().setPort(-1);
			tomcat.start();
			System.out.println("Tomcat Version: " + ServerInfo.getServerInfo());
		}
	}

	@SuppressWarnings("unchecked")
	private void addServletContainerInitializers(Context context,
			Map<ServletContainerInitializer, ServletContainerInitializerData> servletContainerInitializerMap) {
		servletContainerInitializerMap.forEach((k, v) -> {
			Set<Class<?>> handlesTypesImplSet = null;
			Class<ServletContainerInitializer> servletContainerInitializerClass = (Class<ServletContainerInitializer>) k
					.getClass();
			jakarta.servlet.annotation.HandlesTypes handlesTypes = servletContainerInitializerClass
					.getAnnotation(jakarta.servlet.annotation.HandlesTypes.class);
			if (handlesTypes != null && handlesTypes.value().length > 0) {
				handlesTypesImplSet = new LinkedHashSet<Class<?>>();
				for (Class<?> handlesType : handlesTypes.value()) {
					Set<Class<?>> impls = v.getHandlesTypesImplMap().get(handlesType);
					if (impls != null) {
						handlesTypesImplSet.addAll(impls);
					}
				}
			} else {
				handlesTypesImplSet = Collections.emptySet();
			}
			context.addServletContainerInitializer(k, handlesTypesImplSet);
		});
	}

	private void configureSSL(Connector connector) {
		Ssl ssl = tomcatConfig.getSsl();
		connector.setScheme("https");
		connector.setSecure(true);
		connector.setProperty("SSLEnabled", "true");

		SSLHostConfig sslHostConfig = new SSLHostConfig();

		SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(
				sslHostConfig,
				SSLHostConfigCertificate.Type.UNDEFINED);

		if (ssl.getSslKeystoreFile() != null) {
			try {
				String resolvedKeystorePath = resolveKeystorePath(ssl.getSslKeystoreFile());
				certificate.setCertificateKeystoreFile(resolvedKeystorePath);
			} catch (Exception e) {
				logger.error("Failed to resolve keystore path: " + ssl.getSslKeystoreFile(), e);
				certificate.setCertificateKeystoreFile(ssl.getSslKeystoreFile());
			}
		}
		if (ssl.getSslKeystorePassword() != null) {
			certificate.setCertificateKeystorePassword(ssl.getSslKeystorePassword());
		}
		if (ssl.getSslKeystoreType() != null) {
			certificate.setCertificateKeystoreType(ssl.getSslKeystoreType());
		}
		if (ssl.getSslKeyAlias() != null) {
			certificate.setCertificateKeyAlias(ssl.getSslKeyAlias());
		}
		if (ssl.getSslKeyPassword() != null) {
			certificate.setCertificateKeyPassword(ssl.getSslKeyPassword());
		}

		sslHostConfig.addCertificate(certificate);

		if (ssl.getSslProtocol() != null) {
			sslHostConfig.setSslProtocol(ssl.getSslProtocol());
		}

		connector.addSslHostConfig(sslHostConfig);
	}

	private String resolveKeystorePath(String keystoreFile) {
		if (keystoreFile == null) {
			return null;
		}
		if (!keystoreFile.startsWith("/")) {
			keystoreFile = "/" + keystoreFile;
		}

		try (InputStream keystoreInput = startClass.getResourceAsStream(keystoreFile)) {
			if (keystoreInput != null) {
				File tempKeystore = File.createTempFile("keystore-" + keystoreFile, ".tmp");
				tempKeystore.deleteOnExit();
				try (FileOutputStream fos = new FileOutputStream(tempKeystore)) {
					byte[] buffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = keystoreInput.read(buffer)) != -1) {
						fos.write(buffer, 0, bytesRead);
					}
				}
				return tempKeystore.getAbsolutePath();
			}
		} catch (Exception e) {
			logger.debug("Failed to load keystore from classpath: " + keystoreFile, e);
		}

		try {
			Path keystorePath = Paths.get(keystoreFile);
			if (Files.exists(keystorePath)) {
				return keystorePath.toAbsolutePath().toString();
			}
		} catch (Exception e) {
			logger.debug("Failed to resolve keystore as file path: " + keystoreFile, e);
		}

		return keystoreFile;
	}

	private void configureConnector(Connector connector) {
		TomcatConfig.TomcatSettings tomcatSettings = tomcatConfig.getTomcat();
		if (serverConfig.isHttp2()) {
			connector.addUpgradeProtocol(new Http2Protocol());
		}
		ProtocolHandler protocolHandler = connector.getProtocolHandler();
		if (protocolHandler instanceof AbstractProtocol) {
			AbstractProtocol<?> abstractProtocol = (AbstractProtocol<?>) protocolHandler;
			if (tomcatSettings.getAcceptCount() != null) {
				abstractProtocol.setAcceptCount(tomcatSettings.getAcceptCount());
			}
			if (tomcatSettings.getMaxConnections() != null) {
				abstractProtocol.setMaxConnections(tomcatSettings.getMaxConnections());
			}
			if (tomcatSettings.getMaxHeaderCount() != null) {
				abstractProtocol.setMaxHeaderCount(tomcatSettings.getMaxHeaderCount());
			}
			if (tomcatSettings.getConnectionTimeout() != null) {
				abstractProtocol.setConnectionTimeout(tomcatSettings.getConnectionTimeout());
			}
			if (tomcatSettings.getKeepAliveTimeout() != null) {
				abstractProtocol.setKeepAliveTimeout(tomcatSettings.getKeepAliveTimeout());
			}
			if (this.virtualThread != null && this.virtualThread) {
				ThreadFactory factory = Thread.ofVirtual().name("tomcat-virtual-executor@", 1).factory();
				abstractProtocol.setExecutor(Executors.newThreadPerTaskExecutor(factory));
			} else {
				if (tomcatSettings.getMaxQueueSize() != null) {
					abstractProtocol.setMaxQueueSize(tomcatSettings.getMaxQueueSize());
				}
				if (tomcatSettings.getMaxThreads() != null) {
					abstractProtocol.setMaxThreads(tomcatSettings.getMaxThreads());
				}
				if (tomcatSettings.getMinSpareThreads() != null) {
					abstractProtocol.setMinSpareThreads(tomcatSettings.getMinSpareThreads());
				}
			}

		}

		Compression compression = tomcatConfig.getCompression();
		if (compression != null && compression.getEnabled()) {
			if (protocolHandler instanceof Http11NioProtocol) {
				Http11NioProtocol http11NioProtocol = (Http11NioProtocol) protocolHandler;
				http11NioProtocol.setCompression("on");
				http11NioProtocol.setCompressionMinSize((int) compression.getMinResponseSize());
				StringBuilder mimeTypes = new StringBuilder();
				for (String mimeType : compression.getMimeTypes()) {
					mimeTypes.append(mimeType).append(",");
				}
				if (mimeTypes.length() > 0) {
					mimeTypes.setLength(mimeTypes.length() - 1);
				}
				http11NioProtocol.setCompressibleMimeType(mimeTypes.toString());
			}
		}

		Map<String, Object> tomcatOptions = tomcatSettings.getTomcatOptions();
		if (tomcatOptions != null) {
			tomcatOptions.forEach((k, v) -> {
				if (v != null) {
					try {
						if (!setProtocolHandlerAttribute(protocolHandler, k, v)) {
							setConnectorAttribute(connector, k, v);
						}
					} catch (Exception e) {
						logger.error("Failed to set connector attribute: {} = {}", k, v, e);
					}
				}
			});
		}
	}

	private boolean setProtocolHandlerAttribute(ProtocolHandler protocolHandler, String name, Object value) {
		try {
			String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
			Method setter = protocolHandler.getClass().getMethod(setterName, value.getClass());
			setter.invoke(protocolHandler, value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void setConnectorAttribute(Connector connector, String name, Object value) {
		try {
			String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
			Method setter = connector.getClass().getMethod(setterName, Object.class);
			setter.invoke(connector, value);
		} catch (Exception ex) {
			logger.warn("Cannot set connector attribute: {} - method not found", name);
		}
	}

	private void configureAccessLog(Host host) {
		AccessLog accessLog = tomcatConfig.getAccessLog();
		if (accessLog != null && accessLog.isEnabled()) {
			accessLogValve = new AccessLogValve();
			accessLogValve.setPattern(accessLog.getPattern());
			accessLogValve.setPrefix(accessLog.getPrefix());
			accessLogValve.setSuffix("." + accessLog.getSuffix());
			accessLogValve.setDirectory(accessLog.getDir());
			accessLogValve.setRotatable(accessLog.isRotate());
			accessLogValve.setEnabled(true);
			host.getPipeline().addValve(accessLogValve);
		}
	}

	private void injectServlet(Context context) throws Throwable {
		Container[] children = context.findChildren();
		for (Container child : children) {
			if (child instanceof Wrapper) {
				Wrapper wrapper = (Wrapper) child;
				Servlet servlet = wrapper.getServlet();
				if (servlet != null) {
					componentLoadFire(servlet.getClass(), servlet);
				}
			}
		}
	}

	private void injectFilter(org.apache.catalina.Context context) throws Throwable {
		FilterMap[] filterMaps = context.findFilterMaps();

		java.util.Set<String> processedFilters = new java.util.HashSet<>();

		for (FilterMap filterMap : filterMaps) {
			String filterName = filterMap.getFilterName();

			if (processedFilters.contains(filterName)) {
				continue;
			}

			processedFilters.add(filterName);

			FilterDef filterDef = context.findFilterDef(filterName);
			if (filterDef != null) {
				Filter filter = (Filter) filterDef.getFilter();
				if (filter != null) {
					componentLoadFire(filter.getClass(), filter);
				}
			}
		}
	}

	private void injectListener(Context context) throws Throwable {
		Object[] listeners = context.getApplicationEventListeners();
		for (Object listener : listeners) {
			if (listener instanceof EventListener) {
				componentLoadFire(listener.getClass(), listener);
			}
		}
	}

	private void componentLoadFire(Class<?> type, Object target) throws Throwable {
		for (OrderData<RemoteClassLoaderFire> rf : fireList) {
			rf.getData().onLoadFire(type, target);
		}
	}

	private File createTempDir(String prefix, int port) throws IOException {
		try {
			File tempDir = Files.createTempDirectory(prefix + "." + port + ".").toFile();
			tempDir.deleteOnExit();
			return tempDir;
		} catch (IOException ex) {
			throw new IOException(
					"Unable to create tempDir. tmpdir is set to " + System.getProperty("tmpdir"), ex);
		}
	}

	@Override
	public void stop() throws ServletException, LifecycleException {
		if (started.compareAndSet(true, false)) {
			shutdownValve.setShuttingDown(true);

			if (serverConfig.isGracefulShutdown()) {
				this.gracefulShutdownTimeout = serverConfig.getGracefulShutdownTimeout();
				if (gracefulShutdownTimeout > 0 && context != null) {
					context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
				}

				Executor executor = tomcat.getConnector().getProtocolHandler().getExecutor();

				if (executor instanceof ExecutorService executorService) {
					try {
						long gracefulShutdownResponseTimeout = tomcatConfig.getTomcat()
								.getGracefulShutdownResponseTimeout();
						if (gracefulShutdownResponseTimeout > 0) {
							Thread.sleep(gracefulShutdownResponseTimeout);
						}
						logger.info("Starting graceful shutdown of executor service.");
						executorService.shutdown();

						if (!executorService.awaitTermination(gracefulShutdownTimeout, TimeUnit.MILLISECONDS)) {
							logger.warn("Executor service did not terminate gracefully within {} milliseconds.",
									gracefulShutdownTimeout);
							executorService.shutdownNow();
						} else {
							logger.info("Executor service terminated gracefully.");
						}
					} catch (InterruptedException e) {
						logger.error("Interrupted while waiting for executor service termination.", e);
						Thread.currentThread().interrupt();
					}
				} else {
					logger.warn("Unknown executor type: {}", executor.getClass().getName());
				}
			}

			try {
				tomcat.stop();
				tomcat.destroy();
			} catch (Exception e) {
				logger.error("Error while stopping Tomcat", e);
			}
		}
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int getSslPort() {
		return sslPort;
	}

	@Override
	public void setSslPort(int sslPort) {
		this.sslPort = sslPort;
	}

	@Override
	public void await() throws Throwable {
		tomcat.getServer().await();
	}

}
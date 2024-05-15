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
package com.anywide.dawdler.boot.web.undertow.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import com.anywide.dawdler.boot.web.config.WebServerConfig.Compression;
import com.anywide.dawdler.boot.web.config.WebServerConfig.Server;
import com.anywide.dawdler.boot.web.server.WebServer;
import com.anywide.dawdler.boot.web.server.component.ServletComponentProvider;
import com.anywide.dawdler.boot.web.server.component.ServletContainerInitializerData;
import com.anywide.dawdler.boot.web.server.component.ServletContainerInitializerProvider;
import com.anywide.dawdler.boot.web.undertow.compression.CompressibleMimeTypePredicate;
import com.anywide.dawdler.boot.web.undertow.config.UndertowConfig;
import com.anywide.dawdler.boot.web.undertow.config.UndertowConfig.AccessLog;
import com.anywide.dawdler.boot.web.undertow.deployment.UndertowDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.UndertowDeployerProvider;
import com.anywide.dawdler.boot.web.undertow.error.UndertowExceptionHandler;
import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFireHolder;
import com.anywide.dawdler.core.order.OrderData;
import com.anywide.dawdler.core.shutdown.ContainerGracefulShutdown;
import com.anywide.dawdler.util.DawdlerTool;
import com.anywide.dawdler.util.YAMLMapperFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.predicate.MaxContentSizePredicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.GracefulShutdownHandler.ShutdownListener;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.DefaultAccessLogReceiver;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.core.ApplicationListeners;
import io.undertow.servlet.core.ManagedFilter;
import io.undertow.servlet.core.ManagedListener;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.jsr.Bootstrap;

/**
 * @author jackson.song
 * @version V1.0
 * @param <T>
 * @Title UndertowWebServer.java
 * @Description undertow实现的web服务器
 * @date 2023年11月19日
 * @email suxuan696@gmail.com
 */
public class UndertowWebServer implements WebServer {
	private static final Logger logger = LoggerFactory.getLogger(UndertowWebServer.class);
	private AtomicBoolean started = new AtomicBoolean();
	private Undertow undertow;
	private Class<?> startClass;
	private UndertowConfig undertowConfig;
	private ServletComponentProvider scanServletComponent = ServletComponentProvider.getInstance();
	private final List<OrderData<RemoteClassLoaderFire>> fireList = RemoteClassLoaderFireHolder.getInstance()
			.getRemoteClassLoaderFire();
	private Integer port;
	private GracefulShutdownHandler gracefulShutdownHandler;
	private CountDownLatch countDownLatch;
	private long gracefulShutdownTimeout;
	private XnioWorker logXnioWorker;
	private ServletContext servletContext;
	DeploymentManager manager;

	@Override
	public void start() throws Throwable {
		startClass = DawdlerTool.getStartClass();
		if (started.compareAndSet(false, true)) {
			YAMLMapper YAMLMapper = YAMLMapperFactory.getYAMLMapper();
			InputStream input = startClass.getResourceAsStream("/undertow.yml");
			if (input != null) {
				try {
					undertowConfig = YAMLMapper.readValue(input, UndertowConfig.class);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
						}
					}
				}
			}
			if (undertowConfig == null) {
				undertowConfig = new UndertowConfig();
			}
			Map<ServletContainerInitializer, ServletContainerInitializerData> servletContainerInitializerMap = new LinkedHashMap<>();
			ServletContainerInitializerProvider.getServletcontainerinitializers().forEach(initializer -> {
				ServletContainerInitializerData data = new ServletContainerInitializerData();
				HandlesTypes handlesTypes = initializer.getClass().getAnnotation(HandlesTypes.class);
				if (handlesTypes != null && handlesTypes.value().length > 0) {
					for (Class<?> clazz : handlesTypes.value()) {
						data.getHandlesTypesInterfaceSet().add(clazz);
					}
				}
				servletContainerInitializerMap.put(initializer.getData(), data);
			});

			if (undertowConfig.getComponentPackagePaths() != null) {
				scanServletComponent.scanComponent(undertowConfig.getComponentPackagePaths(),
						servletContainerInitializerMap);
			}
			boolean useWebsocket = !scanServletComponent.getEndPointList().isEmpty();
			Server server = undertowConfig.getServer();
			port = server.getPort();
			DeploymentInfo deployment = Servlets.deployment();
			deployment.setClassLoader(Thread.currentThread().getContextClassLoader());
			addServletContainerInitializers(deployment, servletContainerInitializerMap);
			ServletContainer container = Servlets.newContainer();
			List<OrderData<UndertowDeployer>> deployers = UndertowDeployerProvider.getInstance().getDeployers();
			for (OrderData<UndertowDeployer> orderData : deployers) {
				orderData.getData().deploy(undertowConfig, deployment);
			}
			manager = container.addDeployment(deployment);
			manager.deploy();
			HttpHandler httpHandler = manager.start();
			httpHandler = setGracefulShutdownHandler(httpHandler);
			httpHandler = setAccessLogHandler(httpHandler);
			httpHandler = setCompressionHandler(httpHandler);
			httpHandler = new UndertowExceptionHandler(httpHandler);
			PathHandler servletPath = new PathHandler();
			servletPath.addPrefixPath(deployment.getContextPath(), httpHandler);
			Builder builder = Undertow.builder();
			if (undertowConfig.getUndertow() != null) {
				configSocketOptions(builder);
				configUndertowServerOption(builder);
				configUndertow(builder);
			}
			builder.setHandler(servletPath);
			bindServer(builder);
			undertow = builder.build();
			injectServlet(manager);
			injectFilter(manager);
			injectLister(manager);
			servletContext = manager.getDeployment().getServletContext();
			if (useWebsocket) {
				Bootstrap bootstrap = new Bootstrap();
				bootstrap.handleDeployment(deployment, servletContext);
			}
			undertow.start();
		}
	}

	private void injectServlet(DeploymentManager manager) throws Throwable {
		Map<String, io.undertow.servlet.handlers.ServletHandler> servlets = manager.getDeployment().getServlets()
				.getServletHandlers();
		Collection<io.undertow.servlet.handlers.ServletHandler> servletCollection = servlets.values();
		for (io.undertow.servlet.handlers.ServletHandler servletHandler : servletCollection) {
			Servlet servlet = servletHandler.getManagedServlet().getServlet().getInstance();
			componentLoadFire(servlet.getClass(), servlet);
		}
	}

	private void injectFilter(DeploymentManager manager) throws Throwable {
		Map<String, ManagedFilter> filters = manager.getDeployment().getFilters().getFilters();
		Collection<ManagedFilter> filterCollection = filters.values();
		for (ManagedFilter managedFilter : filterCollection) {
			Method filterMethod = managedFilter.getClass().getDeclaredMethod("getFilter");
			filterMethod.setAccessible(true);
			Object filter = filterMethod.invoke(managedFilter);
			componentLoadFire(filter.getClass(), filter);
		}
	}

	private void injectLister(DeploymentManager manager) throws Throwable {
		ApplicationListeners listeners = manager.getDeployment().getApplicationListeners();
		Field allListenersField = listeners.getClass().getDeclaredField("allListeners");
		allListenersField.setAccessible(true);
		List<ManagedListener> allListeners = (List<ManagedListener>) allListenersField.get(listeners);
		for (ManagedListener managedListener : allListeners) {
			EventListener listener = managedListener.instance();
			componentLoadFire(listener.getClass(), listener);
		}
	}

	private void addServletContainerInitializers(DeploymentInfo deployment,
			Map<ServletContainerInitializer, ServletContainerInitializerData> servletContainerInitializerMap) {
		servletContainerInitializerMap.forEach((k, v) -> {
			Set<Class<?>> handlesTypesImplSet = null;
			Class<ServletContainerInitializer> servletContainerInitializerClass = (Class<ServletContainerInitializer>) k
					.getClass();
			HandlesTypes handlesTypes = servletContainerInitializerClass.getAnnotation(HandlesTypes.class);
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
			InstanceFactory<ServletContainerInitializer> instanceFactory = new ImmediateInstanceFactory<ServletContainerInitializer>(
					k);
			ServletContainerInitializerInfo servletContainerInitializerInfo = new ServletContainerInitializerInfo(
					servletContainerInitializerClass, instanceFactory, handlesTypesImplSet);
			deployment.addServletContainerInitializer(servletContainerInitializerInfo);
		});
	}

	private HttpHandler setCompressionHandler(HttpHandler httpHandler) {
		Compression compression = undertowConfig.getCompression();
		if (compression == null || !compression.getEnabled()) {
			return httpHandler;
		}
		MaxContentSizePredicate.Builder builder = new MaxContentSizePredicate.Builder();
		Map<String, Object> config = new HashMap<String, Object>();
		config.put(builder.defaultParameter(), compression.getMinResponseSize());
		ContentEncodingRepository repository = new ContentEncodingRepository();
		repository.addEncodingHandler("gzip", new GzipEncodingProvider(), 250,
				Predicates.and(builder.build(config), new CompressibleMimeTypePredicate(compression.getMimeTypes())));
		return new EncodingHandler(repository).setNext(httpHandler);
	}

	private void componentLoadFire(Class<?> type, Object target) throws Throwable {
		for (OrderData<RemoteClassLoaderFire> rf : fireList) {
			rf.getData().onLoadFire(type, target);
		}
	}

	private void bindServer(Builder builder) {
		Server server = undertowConfig.getServer();
		String host = server.getHost();
		boolean http2 = server.isHttp2();
		if (http2) {
			builder.setServerOption(UndertowOptions.ENABLE_HTTP2, http2);
		}
		builder.setServerOption(UndertowOptions.SHUTDOWN_TIMEOUT, 0);
		builder.addHttpListener(port, host == null ? "0.0.0.0" : host);
	}

	private void configUndertowServerOption(Builder builder) {
		Map<String, Object> options = undertowConfig.getUndertow().getUndertowOptions();
		if (options != null) {
			options.forEach((k, v) -> {
				if (v != null) {
					try {
						Field field = UndertowOptions.class.getDeclaredField(k.toUpperCase().replace("-", "_"));
						if (field.getType().isAssignableFrom(Option.class)) {
							Option option = (Option) field.get(null);
							try {
								option.cast(v);
								builder.setServerOption(option, v);
							} catch (Exception e) {
								throw new IllegalArgumentException("unknown undertow-options " + k + " : " + v + " !",
										e);
							}
						} else {
							logger.error("unknown  undertow-options " + k + "!");
							throw new IllegalArgumentException("unknown undertow-options " + k + "!");
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e) {
						logger.error("unknown undertow-options " + k + "!");
						throw new IllegalArgumentException("unknown undertow-options " + k + "!", e);
					}
				}
			});
		}
	}

	private void configSocketOptions(Builder builder) {
		Map<String, Object> options = undertowConfig.getUndertow().getSocketOptions();
		if (options != null) {
			options.forEach((k, v) -> {
				if (v != null) {
					try {
						Field field = Options.class.getDeclaredField(k.toUpperCase().replace("-", "_"));
						if (field.getType().isAssignableFrom(Option.class)) {
							Option option = (Option) field.get(null);
							try {
								builder.setSocketOption(option, v);
							} catch (Exception e) {
								throw new IllegalArgumentException("unknown socket-options " + k + " : " + v + " !", e);
							}
						} else {
							logger.error("unknown socket-options " + k + "!");
							throw new IllegalArgumentException("unknown socket-options " + k + "!");
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e) {
						logger.error("unknown socket-options " + k + "!");
						throw new IllegalArgumentException("unknown socket-options " + k + "!", e);
					}
				}
			});
		}
	}

	private void configUndertow(Builder builder) {
		com.anywide.dawdler.boot.web.undertow.config.UndertowConfig.Undertow undertow = undertowConfig.getUndertow();
		Integer bufferSize = undertow.getBufferSize();
		if (bufferSize != null) {
			builder.setBufferSize(bufferSize);
		}
		Integer ioThreads = undertow.getIoThreads();
		if (ioThreads != null) {
			builder.setIoThreads(ioThreads);
		}
		Integer workerThreads = undertow.getWorkerThreads();
		if (workerThreads != null) {
			builder.setWorkerThreads(workerThreads);
		}
		Boolean directBuffers = undertow.getDirectBuffers();
		if (directBuffers != null) {
			builder.setDirectBuffers(directBuffers);
		}
	}

	public HttpHandler setGracefulShutdownHandler(HttpHandler httpHandler) {
		if (undertowConfig.getServer().isGracefulShutdown()) {
			this.gracefulShutdownTimeout = undertowConfig.getServer().getGracefulShutdownTimeout();
			gracefulShutdownHandler = Handlers.gracefulShutdown(httpHandler);
			httpHandler = gracefulShutdownHandler;
			countDownLatch = new CountDownLatch(1);
		}
		return httpHandler;
	}

	public HttpHandler setAccessLogHandler(HttpHandler httpHandler) throws IllegalArgumentException, IOException {
		AccessLog accessLog = undertowConfig.getAccessLog();
		if (accessLog != null && accessLog.isEnabled()) {
			Xnio xnio = Xnio.getInstance(Undertow.class.getClassLoader());
			this.logXnioWorker = xnio.createWorker(OptionMap.builder().set(Options.THREAD_DAEMON, true).getMap());
			DefaultAccessLogReceiver accessLogReceiver = new DefaultAccessLogReceiver(logXnioWorker,
					new File(accessLog.getDir()), accessLog.getPrefix(), accessLog.getSuffix(), accessLog.isRotate());
			httpHandler = new AccessLogHandler(httpHandler, accessLogReceiver, accessLog.getPattern(),
					startClass.getClassLoader());
		}
		return httpHandler;
	}

	@Override
	public void stop() throws ServletException {
		if (started.compareAndSet(true, false)) {
			if (gracefulShutdownHandler != null) {
				servletContext.setAttribute(ContainerGracefulShutdown.class.getName(), true);
				gracefulShutdownHandler.shutdown();
				gracefulShutdownHandler.addShutdownListener(new ShutdownListener() {
					@Override
					public void shutdown(boolean shutdownSuccessful) {
						if (shutdownSuccessful) {
							countDownLatch.countDown();
						}
					}
				});
				try {
					countDownLatch.await(gracefulShutdownTimeout, TimeUnit.MILLISECONDS);
					if (countDownLatch.getCount() > 0) {
						logger.info("Undertow Failed to shutdown within timeout of " + this.gracefulShutdownTimeout
								+ "ms !");
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			try {
				manager.stop();
				manager.undeploy();
			} finally {
				undertow.stop();
			}
			if (logXnioWorker != null) {
				logXnioWorker.shutdown();
			}
		}
	}

	@Override
	public int getPort() {
		return port;
	}

}

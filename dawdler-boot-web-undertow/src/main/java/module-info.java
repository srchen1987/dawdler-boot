import club.dawdler.boot.web.server.WebServer;
import club.dawdler.boot.web.undertow.deployment.BaseConfigDeployer;
import club.dawdler.boot.web.undertow.deployment.ErrorPagesDeployer;
import club.dawdler.boot.web.undertow.deployment.FilterDeployer;
import club.dawdler.boot.web.undertow.deployment.ListenerDeployer;
import club.dawdler.boot.web.undertow.deployment.ServletDeployer;
import club.dawdler.boot.web.undertow.deployment.StaticResourceDeployer;
import club.dawdler.boot.web.undertow.deployment.UndertowDeployer;
import club.dawdler.boot.web.undertow.deployment.WebSocketDeployer;
import club.dawdler.boot.web.undertow.server.UndertowWebServer;

module dawdler.boot.web.undertow {
	requires java.base;
	requires org.slf4j;
	requires transitive dawdler.client.plug.web;
	requires transitive undertow.core;
	requires dawdler.util;
	requires transitive dawdler.boot.web;
	requires dawdler.boot.classloader;
	requires transitive undertow.websockets.jsr;
	requires transitive xnio.api;
	requires wildfly.common;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires com.fasterxml.jackson.databind;
	requires jakarta.servlet;
	requires transitive dawdler.core;
	requires transitive undertow.servlet;

	opens club.dawdler.boot.web.undertow.error to dawdler.boot.web;
	opens club.dawdler.boot.web.undertow.config;

	exports club.dawdler.boot.web.undertow.config;
	exports club.dawdler.boot.web.undertow.deployment;

	uses WebServer;

	provides WebServer with UndertowWebServer;

	uses UndertowDeployer;

	provides UndertowDeployer with BaseConfigDeployer, StaticResourceDeployer, ErrorPagesDeployer, ServletDeployer,
			FilterDeployer, ListenerDeployer, WebSocketDeployer;

}

import com.anywide.dawdler.boot.web.server.WebServer;
import com.anywide.dawdler.boot.web.undertow.deployment.BaseConfigDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.ErrorPagesDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.FilterDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.ListenerDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.ServletDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.StaticResourceDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.UndertowDeployer;
import com.anywide.dawdler.boot.web.undertow.deployment.WebSocketDeployer;
import com.anywide.dawdler.boot.web.undertow.server.UndertowWebServer;

module dawdler.boot.web.undertow {
	requires java.base;
	requires org.slf4j;
	requires transitive dawdler.client.plug.web;
	requires transitive undertow.core;
	requires dawdler.util;
	requires transitive dawdler.boot.web;
	requires transitive undertow.websockets.jsr;
	requires transitive xnio.api;
	requires wildfly.common;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires com.fasterxml.jackson.databind;
	requires jakarta.servlet;
	requires transitive dawdler.core;
	requires transitive undertow.servlet;

	opens com.anywide.dawdler.boot.web.undertow.error to dawdler.boot.web;
	opens com.anywide.dawdler.boot.web.undertow.config;

	exports com.anywide.dawdler.boot.web.undertow.config;
	exports com.anywide.dawdler.boot.web.undertow.deployment;

	uses WebServer;

	provides WebServer with UndertowWebServer;

	uses UndertowDeployer;

	provides UndertowDeployer with BaseConfigDeployer, StaticResourceDeployer, ErrorPagesDeployer, ServletDeployer,
			FilterDeployer, ListenerDeployer, WebSocketDeployer;

}
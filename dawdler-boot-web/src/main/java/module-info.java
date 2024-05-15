module dawdler.boot.web {
	requires java.base;
	requires org.slf4j;
	requires dawdler.client.plug.web;
	requires dawdler.util;
	requires com.fasterxml.jackson.databind;
	requires dawdler.core;
	requires jakarta.servlet;
	requires jakarta.websocket;
	requires dawdler.boot.classloader;
	requires dawdler.boot.classloader.core;

	exports com.anywide.dawdler.boot.web.annotation;
	exports com.anywide.dawdler.boot.web.starter;
	exports com.anywide.dawdler.boot.web.server;
	exports com.anywide.dawdler.boot.web.config;
	exports com.anywide.dawdler.boot.web.server.component;

	opens com.anywide.dawdler.boot.web.config;

	uses com.anywide.dawdler.boot.web.server.WebServer;

	uses jakarta.servlet.ServletContainerInitializer;
}
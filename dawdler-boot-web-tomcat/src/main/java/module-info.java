import club.dawdler.boot.web.server.WebServer;
import club.dawdler.boot.web.tomcat.deployment.BaseConfigDeployer;
import club.dawdler.boot.web.tomcat.deployment.ErrorPagesDeployer;
import club.dawdler.boot.web.tomcat.deployment.FilterDeployer;
import club.dawdler.boot.web.tomcat.deployment.ListenerDeployer;
import club.dawdler.boot.web.tomcat.deployment.ServletDeployer;
import club.dawdler.boot.web.tomcat.deployment.StaticResourceDeployer;
import club.dawdler.boot.web.tomcat.deployment.TomcatDeployer;
import club.dawdler.boot.web.tomcat.server.TomcatWebServer;
module dawdler.boot.web.tomcat {
	requires java.base;
	requires java.management;
	requires org.slf4j;
	requires dawdler.client.plug.web;
	requires dawdler.util;
	requires dawdler.boot.web;
	requires dawdler.boot.classloader;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires com.fasterxml.jackson.databind;
	requires transitive dawdler.core;
	requires dawdler.boot.web.tomcat.source;
	requires jakarta.servlet;

	opens club.dawdler.boot.web.tomcat.server to dawdler.boot.web.tomcat.source;

	exports club.dawdler.boot.web.tomcat.config;
	exports club.dawdler.boot.web.tomcat.deployment;

	opens club.dawdler.boot.web.tomcat.deployment to dawdler.boot.web.tomcat.websocket;

	uses WebServer;

	provides WebServer with TomcatWebServer;

	uses TomcatDeployer;

	provides TomcatDeployer with BaseConfigDeployer, StaticResourceDeployer, ErrorPagesDeployer, ServletDeployer,
			FilterDeployer, ListenerDeployer;
}
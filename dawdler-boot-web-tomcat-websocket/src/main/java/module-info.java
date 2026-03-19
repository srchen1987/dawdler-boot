import club.dawdler.boot.web.tomcat.deployment.TomcatDeployer;
import club.dawdler.boot.web.tomcat.websocket.deployment.WebSocketDeployer;

module dawdler.boot.web.tomcat.websocket {
	requires java.base;
	requires dawdler.boot.web.tomcat.source;
	requires dawdler.boot.web.tomcat;
	requires dawdler.boot.web.tomcat.websocket.source;
	requires jakarta.servlet;
	requires dawdler.boot.web;

	uses TomcatDeployer;

	provides TomcatDeployer with WebSocketDeployer;
}
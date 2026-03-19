module dawdler.boot.web.tomcat.websocket.source {
	requires java.base;
	requires jakarta.websocket;
	requires jakarta.servlet;
	requires dawdler.boot.web.tomcat.source;
	requires java.naming;
	requires biz.aQute.bndlib;

	exports org.apache.tomcat.websocket.server;
}
import club.dawdler.boot.web.undertow.deployment.UndertowDeployer;

module dawdler.boot.web.undertow.websocket {
	requires java.base;
	requires transitive dawdler.boot.web;
	requires dawdler.boot.classloader;
	requires transitive dawdler.boot.web.undertow;
	requires undertow.servlet;
	requires undertow.websockets.jsr;

	uses UndertowDeployer;

	provides UndertowDeployer with club.dawdler.boot.web.undertow.websocket.deployment.WebSocketDeployer;

}

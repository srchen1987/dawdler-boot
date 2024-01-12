module dawdler.boot.server {
	requires java.base;
	requires java.sql;
	requires dawdler.boot.classloader;
	requires dawdler.server;
	requires org.slf4j;

	exports com.anywide.dawdler.boot.server.annotation;
	exports com.anywide.dawdler.boot.server.starter;
	exports com.anywide.dawdler.boot.server.deploys.loader;
	exports com.anywide.dawdler.boot.server.deploys;
}
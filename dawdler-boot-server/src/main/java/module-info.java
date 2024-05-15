module dawdler.boot.server {
	requires java.base;
	requires java.sql;
	requires dawdler.boot.classloader;
	requires transitive dawdler.server;
	requires org.slf4j;
	requires dawdler.boot.classloader.core;

	exports com.anywide.dawdler.boot.server.annotation;
	exports com.anywide.dawdler.boot.server.starter;
	exports com.anywide.dawdler.boot.server.deploys;
}
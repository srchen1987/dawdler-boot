module dawdler.boot.server {
	requires java.base;
	requires java.sql;
	requires dawdler.boot.classloader;
	requires transitive dawdler.server;
	requires org.slf4j;
	requires dawdler.boot.classloader.core;

	exports club.dawdler.boot.server.annotation;
	exports club.dawdler.boot.server.starter;
	exports club.dawdler.boot.server.deploys;
}
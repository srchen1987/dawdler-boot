module dawdler.boot.classloader.core {
	requires java.base;
	requires java.sql;
	requires jdk.httpserver;
	requires jdk.unsupported;
	requires dawdler.core;
	requires dawdler.boot.classloader;

	exports club.dawdler.boot.core.loader;
}
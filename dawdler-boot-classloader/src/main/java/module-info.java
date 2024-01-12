module dawdler.boot.classloader {
	requires java.base;
	requires java.sql;
	requires jdk.httpserver;
	requires jdk.unsupported;

	exports com.anywide.dawdler.fatjar.loader.launcher;
}
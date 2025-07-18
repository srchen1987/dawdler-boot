module dawdler.boot.classloader {
	requires java.base;
	requires java.sql;
	requires jdk.httpserver;
	requires jdk.unsupported;
	
	exports club.dawdler.fatjar.loader.launcher;
	exports club.dawdler.fatjar.loader.archive.jar;
}
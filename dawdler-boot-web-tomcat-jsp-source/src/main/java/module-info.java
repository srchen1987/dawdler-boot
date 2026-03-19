
module dawdler.boot.web.tomcat.jsp.source {
	requires dawdler.boot.web;
	requires dawdler.boot.web.tomcat;
	requires jakarta.servlet;
	requires jakarta.servlet.jsp;
	requires dawdler.boot.web.tomcat.source;
	requires ant;
	requires java.compiler;
	requires org.eclipse.jdt.core.compiler.batch;

	exports org.apache.jasper;
	exports org.apache.jasper.compiler;
	exports org.apache.jasper.compiler.tagplugin;
	exports org.apache.jasper.el;
	exports org.apache.jasper.optimizations;
	exports org.apache.jasper.runtime;
	exports org.apache.jasper.servlet;
	exports org.apache.jasper.tagplugins.jstl;
	exports org.apache.jasper.tagplugins.jstl.core;
	exports org.apache.jasper.util;
	exports org.apache.tomcat.util.descriptor.tld;

}

import club.dawdler.boot.web.undertow.deployment.UndertowDeployer;
import club.dawdler.boot.web.undertow.jsp.deployment.JspDeployer;

module dawdler.boot.web.undertow.jsp {
	requires jastow;
	requires dawdler.boot.web.undertow;
	requires dawdler.boot.classloader;
	requires undertow.servlet;
	requires jakarta.servlet;
	requires jakarta.servlet.jsp;
	requires org.slf4j;

	uses UndertowDeployer;

	provides UndertowDeployer with JspDeployer;

}

import club.dawdler.boot.web.tomcat.deployment.TomcatDeployer;
import club.dawdler.boot.web.tomcat.jsp.deployment.JspDeployer;

module dawdler.boot.web.tomcat.jsp {
	requires dawdler.boot.web;
	requires dawdler.boot.web.tomcat;
	requires dawdler.boot.web.tomcat.source;
	requires dawdler.boot.web.tomcat.jsp.source;
	requires org.glassfish.expressly;
	requires jakarta.servlet;
	requires jakarta.servlet.jsp;

	uses TomcatDeployer;

	provides TomcatDeployer with JspDeployer;

}

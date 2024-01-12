import com.anywide.dawdler.boot.web.undertow.deployment.UndertowDeployer;
import com.anywide.dawdler.boot.web.undertow.jsp.deployment.JspDeployer;

module dawdler.boot.web.undertow.jsp {
	requires jastow;
	requires dawdler.boot.web.undertow;
	requires undertow.servlet;

	uses UndertowDeployer;

	provides UndertowDeployer with JspDeployer;

}
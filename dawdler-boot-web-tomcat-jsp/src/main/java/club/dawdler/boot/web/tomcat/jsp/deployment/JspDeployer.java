/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.boot.web.tomcat.jsp.deployment;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.jasper.compiler.JarScannerFactory;
import org.apache.jasper.runtime.JspFactoryImpl;
import org.apache.tomcat.util.scan.StandardJarScanner;

import club.dawdler.boot.web.tomcat.config.TomcatConfig;
import club.dawdler.boot.web.tomcat.deployment.TomcatDeployer;
import jakarta.servlet.jsp.JspFactory;

/**
 * @author jackson.song
 * @version V1.0
 * jsp deployer for tomcat
 */
public class JspDeployer implements TomcatDeployer {

	@Override
	public void deploy(TomcatConfig tomcatConfig, Context context) throws Exception {
		StandardJarScanner standardJarScanner = (StandardJarScanner) JarScannerFactory
				.getJarScanner(context.getServletContext());
		standardJarScanner.setScanClassPath(true);
		if (JspFactory.getDefaultFactory() == null) {
			JspFactory.setDefaultFactory(new JspFactoryImpl());
		}
		context.setJarScanner(standardJarScanner);
		Wrapper jspServlet = context.createWrapper();
		jspServlet.setName("jsp");
		jspServlet.setServletClass("org.apache.jasper.servlet.JspServlet");
		jspServlet.addInitParameter("fork", "false");
		jspServlet.addInitParameter("xpoweredBy", "false");
		jspServlet.addInitParameter("development", "true");
		jspServlet.addInitParameter("classdebuginfo", "true");
		jspServlet.addInitParameter("mappedfile", "true");
		jspServlet.addInitParameter("compilerClassName", "org.apache.jasper.compiler.JDTCompiler");
		jspServlet.setLoadOnStartup(1);
		context.addChild(jspServlet);
		context.addServletMappingDecoded("*.jsp", "jsp");
	}
}
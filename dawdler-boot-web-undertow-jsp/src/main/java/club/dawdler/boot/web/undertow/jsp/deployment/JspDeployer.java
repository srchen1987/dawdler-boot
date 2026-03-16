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
package club.dawdler.boot.web.undertow.jsp.deployment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.xml.sax.SAXException;

import club.dawdler.boot.web.undertow.config.UndertowConfig;
import club.dawdler.boot.web.undertow.deployment.UndertowDeployer;
import club.dawdler.boot.web.undertow.jsp.tld.TldScannerTomcat;
import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.servlet.api.DeploymentInfo;

/**
 * @author jackson.song
 * @version V1.0
 *          jsp部署
 */
public class JspDeployer implements UndertowDeployer {
	@Override
	public void deploy(UndertowConfig undertowConfig, DeploymentInfo deploymentInfo)
			throws IOException, URISyntaxException, SAXException {
		HashMap<String, JspPropertyGroup> propertyGroups = new HashMap<>();
		JspPropertyGroup group = new JspPropertyGroup();
		group.addUrlPattern("*.jsp");
		group.setPageEncoding("UTF-8");
		propertyGroups.put("default", group);
		HashMap<String, TagLibraryInfo> tagLibraries = new HashMap<>();
		TldScannerTomcat tldScanner = new TldScannerTomcat(false, false, false);
		tldScanner.scan();
		tldScanner.getTagLibraries().forEach((k, v) -> {
			tagLibraries.put(k, v);
		});
		JspServletBuilder.setupDeployment(
				deploymentInfo,
				propertyGroups,
				tagLibraries,
				new HackInstanceManager());

		deploymentInfo.addServlet(JspServletBuilder.createServlet("jsp", "*.jsp")
				.addInitParam("development", "true")
				.addInitParam("fork", "false")
				.addInitParam("trimSpaces", "false")
				.addInitParam("displaySourceFragment", "true"));
	}

}

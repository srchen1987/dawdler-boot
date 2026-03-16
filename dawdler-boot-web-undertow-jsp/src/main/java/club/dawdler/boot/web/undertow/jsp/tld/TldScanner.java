/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package club.dawdler.boot.web.undertow.jsp.tld;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jasper.deploy.TagLibraryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.boot.web.undertow.jsp.tld.internal.Jar;
import club.dawdler.boot.web.undertow.jsp.tld.internal.JarScanType;
import club.dawdler.boot.web.undertow.jsp.tld.internal.JarScanner;
import club.dawdler.boot.web.undertow.jsp.tld.internal.JarScannerCallback;
import club.dawdler.boot.web.undertow.jsp.tld.internal.JarScannerFactory;
import club.dawdler.boot.web.undertow.jsp.tld.internal.TagXml;
import club.dawdler.boot.web.undertow.jsp.tld.internal.TaglibXml;
import club.dawdler.boot.web.undertow.jsp.tld.internal.TldResourcePath;
import club.dawdler.boot.web.undertow.jsp.tld.tomcat.TldParser;

/**
 * TLD scanner that uses Tomcat's Digester-based TLD parser for better performance.
 */
public class TldScanner {
	private static final String TLD_EXT = ".tld";
	private final TldParser tldParser;
	private final Map<String, TagLibraryInfo> tagLibraries = new HashMap<>();
	private final List<String> listeners = new ArrayList<>();
	Logger logger = LoggerFactory.getLogger(TldScanner.class);

	public TldScanner() {
		this.tldParser = new TldParser(true, true, false);
	}

	public void scan() throws IOException, URISyntaxException {
		scanJars();
		scanClasspathMetaInf();
	}

	public Map<String, TagLibraryInfo> getTagLibraries() {
		return tagLibraries;
	}

	public List<String> getListeners() {
		return listeners;
	}

	protected void scanClasspathMetaInf() throws IOException, URISyntaxException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		java.util.Enumeration<java.net.URL> urls = classLoader.getResources("META-INF");
		while (urls.hasMoreElements()) {
			java.net.URL url = urls.nextElement();
			if ("file".equals(url.getProtocol())) {
				Path metaInfPath = Paths.get(url.toURI());
				if (Files.exists(metaInfPath) && Files.isDirectory(metaInfPath)) {
					try (Stream<Path> files = Files.list(metaInfPath)) {
						files.filter(Files::isRegularFile)
								.filter(p -> p.toString().endsWith(TLD_EXT))
								.forEach(p -> {
									try {
										String fileName = p.getFileName().toString();
										String resourcePath = "META-INF/" + fileName;
										parseTld(resourcePath);
									} catch (IOException e) {
										logger.error("Failed to parse TLD: " + p + " - " + e.getMessage());
									}
								});
					}
				}
			}
		}
	}

	public void scanJars() throws IOException {
		JarScanner scanner = JarScannerFactory.getJarScanner();
		TldScannerCallback callback = new TldScannerCallback();
		scanner.scan(JarScanType.TLD, callback);
	}

	public void parseTld(String resourcePath) throws IOException {
		try {
			URL resourceUrl = Thread.currentThread().getContextClassLoader()
					.getResource(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
			if (resourceUrl == null) {
				logger.error("Resource not found: " + resourcePath);
				return;
			}
			TldResourcePath tldResourcePath = new TldResourcePath(resourceUrl, resourcePath);
			TaglibXml tld = tldParser.parse(tldResourcePath);
			String uri = tld.getUri();
			if (uri != null) {
				if (tagLibraries.containsKey(uri)) {
					logger.info("Skipping duplicate TLD URI: " + uri);
					return;
				}
				TagLibraryInfo tldInfo = new TagLibraryInfo();
				tldInfo.setUri(uri);
				tldInfo.setPrefix("");
				tldInfo.setShortname(tld.getShortName());
				tldInfo.setInfo(tld.getInfo());
				tldInfo.setTlibversion(tld.getTlibVersion());
				tldInfo.setJspversion(tld.getTlibVersion());

				for (TagXml tagXml : tld.getTags()) {
					org.apache.jasper.deploy.TagInfo tagInfo = new org.apache.jasper.deploy.TagInfo();
					tagInfo.setTagName(tagXml.getName());
					tagInfo.setTagClassName(tagXml.getTagClass());
					tagInfo.setBodyContent(tagXml.getBodyContent());
					tagInfo.setInfoString(tagXml.getInfo());

					for (jakarta.servlet.jsp.tagext.TagAttributeInfo attrInfo : tagXml.getAttributes()) {
						org.apache.jasper.deploy.TagAttributeInfo deployAttrInfo = new org.apache.jasper.deploy.TagAttributeInfo();
						deployAttrInfo.setName(attrInfo.getName());
						deployAttrInfo.setRequired(String.valueOf(attrInfo.isRequired()));
						deployAttrInfo.setType(attrInfo.getTypeName());
						deployAttrInfo.setReqTime(String.valueOf(attrInfo.canBeRequestTime()));
						deployAttrInfo.setFragment(String.valueOf(attrInfo.isFragment()));
						tagInfo.addTagAttributeInfo(deployAttrInfo);
					}

					tldInfo.addTagInfo(tagInfo);
				}

				for (TaglibXml.FunctionInfo function : tld.getFunctions()) {
					org.apache.jasper.deploy.FunctionInfo functionInfo = new org.apache.jasper.deploy.FunctionInfo();
					functionInfo.setName(function.getName());
					functionInfo.setFunctionClass(function.getFunctionClass());
					functionInfo.setFunctionSignature(function.getFunctionSignature());
					tldInfo.addFunctionInfo(functionInfo);
				}

				tagLibraries.put(uri, tldInfo);
			}

			if (tld.getListeners() != null) {
				listeners.addAll(tld.getListeners());
			}
		} catch (Exception e) {
			throw new IOException("Failed to parse TLD: " + resourcePath, e);
		}
	}

	class TldScannerCallback implements JarScannerCallback {

		@Override
		public void scan(Jar jar, String webappPath, boolean isWebapp) throws IOException {
			jar.nextEntry();
			for (String entryName = jar.getEntryName(); entryName != null; jar
					.nextEntry(), entryName = jar.getEntryName()) {
				if (!(entryName.startsWith("META-INF/") && entryName.endsWith(".tld"))) {
					continue;
				}
				TldScanner.this.parseTld(entryName);
			}
		}

		@Override
		public void scan(File file, String webappPath, boolean isWebapp) throws IOException {
		}

		@Override
		public void scanWebInfClasses() throws IOException {
		}
	}
}
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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jasper.deploy.TagLibraryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
 * Scans for and loads Tag Library Descriptors contained in a web application.
 */
public class TldScannerTomcat {
	private final Logger logger = LoggerFactory.getLogger(TldScannerTomcat.class); // must not be static
	private static final String TLD_EXT = ".tld";
	private final Map<String, TagLibraryInfo> tagLibraries = new HashMap<>();
	private final TldParser tldParser;
	private final Map<String, TldResourcePath> uriTldResourcePathMap = new HashMap<>();
	private final Map<TldResourcePath, TaglibXml> tldResourcePathTaglibXmlMap = new HashMap<>();
	private final List<String> listeners = new ArrayList<>();
	private TldScannerCallback callback = new TldScannerCallback();
	/**
	 * Initialise with the application's ServletContext.
	 *
	 * @param context        the application's servletContext
	 * @param namespaceAware should the XML parser used to parse TLD files be
	 *                       configured to be name space aware
	 * @param validation     should the XML parser used to parse TLD files be
	 *                       configured to use validation
	 * @param blockExternal  should the XML parser used to parse TLD files be
	 *                       configured to be block references to
	 *                       external entities
	 */
	public TldScannerTomcat(boolean namespaceAware, boolean validation, boolean blockExternal) {
		this.tldParser = new TldParser(namespaceAware, validation, blockExternal);
	}

	/**
	 * Scan for TLDs in all places defined by the specification:
	 * <ol>
	 * <li>Tag libraries defined by the platform</li>
	 * <li>Entries from &lt;jsp-config&gt; in web.xml</li>
	 * <li>A resources under /WEB-INF</li>
	 * <li>In jar files from /WEB-INF/lib</li>
	 * <li>Additional entries from the container</li>
	 * </ol>
	 *
	 * @throws IOException  if there was a problem scanning for or loading a TLD
	 * @throws SAXException if there was a problem parsing a TLD
	 * @throws URISyntaxException 
	 */
	public void scan() throws IOException, SAXException, URISyntaxException {
		scanJars();
		callback.scanWebInfClasses();
	}

	/**
	 * Returns the map of URI to TldResourcePath built by this scanner.
	 *
	 * @return the map of URI to TldResourcePath
	 */
	public Map<String, TldResourcePath> getUriTldResourcePathMap() {
		return uriTldResourcePathMap;
	}

	/**
	 * Returns the map of TldResourcePath to parsed XML files built by this scanner.
	 *
	 * @return the map of TldResourcePath to parsed XML files
	 */
	public Map<TldResourcePath, TaglibXml> getTldResourcePathTaglibXmlMap() {
		return tldResourcePathTaglibXmlMap;
	}

	/**
	 * Returns a list of all listeners declared by scanned TLDs.
	 *
	 * @return a list of listener class names
	 */
	public List<String> getListeners() {
		return listeners;
	}

	public Map<String, TagLibraryInfo> getTagLibraries() {
		return tagLibraries;
	}

	/**
	 * Set the class loader used by the digester to create objects as a result of
	 * this scan. Normally this only needs to
	 * be set when using JspC.
	 *
	 * @param classLoader Class loader to use when creating new objects while
	 *                    parsing TLDs
	 */
	public void setClassLoader(ClassLoader classLoader) {
		tldParser.setClassLoader(classLoader);
	}

	/**
	 * Scan for TLDs defined in &lt;jsp-config&gt;.
	 *
	 * @throws IOException  Error reading resources
	 * @throws SAXException XML parsing error
	 */

	/**
	 * Scan for TLDs in JARs in /WEB-INF/lib.
	 */
	public void scanJars() {
		JarScanner scanner = JarScannerFactory.getJarScanner();
		scanner.scan(JarScanType.TLD, callback);
	}

	protected void parseTld(String resourcePath) throws IOException, SAXException {
		URL resourceUrl = Thread.currentThread().getContextClassLoader()
				.getResource(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
		if (resourceUrl == null) {
			logger.error("Resource not found: " + resourcePath);
			return;
		}
		TldResourcePath tldResourcePath = new TldResourcePath(resourceUrl, resourcePath);
		parseTld(tldResourcePath);
	}

	protected void parseTld(TldResourcePath path) throws IOException, SAXException {
		TaglibXml tld = tldParser.parse(path);
		String uri = tld.getUri();
		if (uri != null) {
			if (!uriTldResourcePathMap.containsKey(uri)) {
				uriTldResourcePathMap.put(uri, path);
			}
		}

		if (tldResourcePathTaglibXmlMap.containsKey(path)) {
			// TLD has already been parsed as a result of processing web.xml
			return;
		}
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

				for (javax.servlet.jsp.tagext.TagAttributeInfo attrInfo : tagXml.getAttributes()) {
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
		tldResourcePathTaglibXmlMap.put(path, tld);
		if (tld.getListeners() != null) {
			listeners.addAll(tld.getListeners());
		}
	}

	class TldScannerCallback implements JarScannerCallback {
		private boolean foundJarWithoutTld = false;

		@Override
		public void scan(Jar jar, String webappPath, boolean isWebapp) throws IOException, SAXException {
			jar.nextEntry();
			for (String entryName = jar.getEntryName(); entryName != null; jar
					.nextEntry(), entryName = jar.getEntryName()) {
				if (!(entryName.startsWith("META-INF/") && entryName.endsWith(".tld"))) {
					continue;
				}
				TldScannerTomcat.this.parseTld(entryName);
			}
		}

		@Override
		public void scan(File file, final String webappPath, boolean isWebapp) throws IOException {
			File metaInf = new File(file, "META-INF");
			if (!metaInf.isDirectory()) {
				return;
			}
			final Path filePath = file.toPath();
			Files.walkFileTree(metaInf.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path fileName = file.getFileName();
					if (fileName == null || !fileName.toString().toLowerCase(Locale.ENGLISH).endsWith(TLD_EXT)) {
						return FileVisitResult.CONTINUE;
					}

					String resourcePath;
					if (webappPath == null) {
						resourcePath = null;
					} else {
						String subPath = file.subpath(filePath.getNameCount(), file.getNameCount()).toString();
						if ('/' != File.separatorChar) {
							subPath = subPath.replace(File.separatorChar, '/');
						}
						resourcePath = webappPath + "/" + subPath;
					}

					try {
						URL url = file.toUri().toURL();
						TldResourcePath path = new TldResourcePath(url, resourcePath);
						parseTld(path);
					} catch (SAXException e) {
						throw new IOException(e);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		@Override
		public void scanWebInfClasses() throws IOException, URISyntaxException {
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
										} catch (Exception e) {
											logger.error("Failed to parse TLD: " + p + " - " + e.getMessage());
										}
									});
						}
					}
				}
			}
		}

		boolean scanFoundNoTLDs() {
			return foundJarWithoutTld;
		}
	}
}

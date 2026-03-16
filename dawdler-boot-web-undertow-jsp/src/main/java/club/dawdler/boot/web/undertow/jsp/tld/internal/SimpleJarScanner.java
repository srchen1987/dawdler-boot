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
package club.dawdler.boot.web.undertow.jsp.tld.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.fatjar.loader.archive.jar.NestedJarURLStreamHandler;

/**
 * @author jackson.song
 * @version V1.0
 * jar扫描器
 */
public class SimpleJarScanner implements JarScanner {
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleJarScanner.class);

	@Override
	public void scan(JarScanType scanType, JarScannerCallback callback) {
		if (scanType != JarScanType.TLD) {
			return;
		}

		try {
			scanClasspathForJars(callback);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void scanJar(URL jarUrl, String webappPath, JarScannerCallback callback) throws Exception {
		Jar jar = new SimpleJar(jarUrl);
		try {
			callback.scan(jar, webappPath, true);
		} finally {
			jar.close();
		}
	}

	private void scanClasspathForJars(JarScannerCallback callback) throws Exception {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader().getParent();
			if (classLoader instanceof URLClassLoader) {
				URL[] urls = ((URLClassLoader) classLoader).getURLs();
				for (URL url : urls) {
					if (url.getProtocol().equals("jar") || url.getFile().endsWith(".jar")) {
						scanJar(url, url.getPath(), callback);
					}
				}
			} else {
				Enumeration<URL> resources = classLoader.getResources("");
				while (resources.hasMoreElements()) {
					URL url = resources.nextElement();
					if (url.getProtocol().equals("file") && url.getFile().endsWith(".jar")) {
						scanJar(url, url.getPath(), callback);
					} else if (url.getProtocol().equals("jar")) {
						scanJar(url, url.getPath(), callback);
					}
				}
			}
	}

	private static class SimpleJar implements Jar {
		private final URL jarUrl;
		private JarInputStream jarInputStream;
		private JarEntry currentEntry;

		public SimpleJar(URL jarUrl) {
			URL processedUrl = jarUrl;
			if (jarUrl.toString().startsWith("jar:")) {
				try {
					processedUrl = new URL(null, jarUrl.toString(), new NestedJarURLStreamHandler());
				} catch (Exception e) {
					processedUrl = jarUrl;
				}
			}
			this.jarUrl = processedUrl;
		}

		@Override
		public URL getJarFileURL() {
			return jarUrl;
		}

		@Override
		public java.io.InputStream getInputStream(String name) throws IOException {
			try {
				String entryUrl = "jar:" + jarUrl.toExternalForm() + "!/" + name;
				URL url = new URL(null, entryUrl, new NestedJarURLStreamHandler());
				return url.openConnection().getInputStream();
			} catch (Exception e) {
				// If we can't create a nested URL, return null
				return null;
			}
		}

		@Override
		public long getLastModified(String name) throws IOException {
			try {
				String entryUrl = "jar:" + jarUrl.toExternalForm() + "!/" + name;
				URL url = new URL(null, entryUrl, new NestedJarURLStreamHandler());
				return url.openConnection().getLastModified();
			} catch (Exception e) {
				return -1;
			}
		}

		@Override
		public boolean exists(String name) throws IOException {
			try {
				String entryUrl = "jar:" + jarUrl.toExternalForm() + "!/" + name;
				URL url = new URL(null, entryUrl, new NestedJarURLStreamHandler());
				return url.openConnection().getContentLength() >= 0;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public void close() {
			if (jarInputStream != null) {
				try {
					jarInputStream.close();
				} catch (IOException e) {
				}
				jarInputStream = null;
			}
		}

		@Override
		public void nextEntry() {
			try {
				if (jarInputStream == null) {
					jarInputStream = new JarInputStream(jarUrl.openConnection().getInputStream());
				}
				currentEntry = jarInputStream.getNextJarEntry();
			} catch (IOException e) {
				currentEntry = null;
			}
		}

		@Override
		public String getEntryName() {
			return currentEntry != null ? currentEntry.getName() : null;
		}

		@Override
		public java.io.InputStream getEntryInputStream() throws IOException {
			if (jarInputStream != null) {
				return jarInputStream;
			}
			if (currentEntry != null) {
				return getInputStream(currentEntry.getName());
			}
			return null;
		}

		@Override
		public String getURL(String entry) {
			return "jar:" + jarUrl.toExternalForm() + "!/" + entry;
		}

		@Override
		public java.util.jar.Manifest getManifest() throws IOException {
			if (jarInputStream == null) {
				jarInputStream = new JarInputStream(jarUrl.openConnection().getInputStream());
			}
			return jarInputStream.getManifest();
		}

		@Override
		public void reset() throws IOException {
			close();
			jarInputStream = new JarInputStream(jarUrl.openConnection().getInputStream());
		}
	}
}
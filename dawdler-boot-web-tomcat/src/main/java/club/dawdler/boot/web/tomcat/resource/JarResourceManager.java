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
package club.dawdler.boot.web.tomcat.resource;

import java.net.URL;
import java.util.Set;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;

import club.dawdler.fatjar.loader.archive.jar.NestedJarURLStreamHandler;

/**
 * @author jackson.song
 * @version V1.0
 * jar中的资源管理器用于读取jar包中的资源文件
 */
public class JarResourceManager implements WebResourceSet {
	private WebResourceRoot root;
	private URL jarUrl;
	private String base;
	private String internalPath = "";
	private boolean classLoaderOnly = false;
	private boolean staticOnly = false;
	private boolean readOnly = true;
	private boolean allowLinking = false;
	private String staticBase = null;

	public JarResourceManager() {
	}

	public JarResourceManager(URL jarUrl) {
		this.jarUrl = jarUrl;
	}

	public JarResourceManager(WebResourceRoot root, String base, String internalPath, String staticBase)
			throws IllegalArgumentException {
		setRoot(root);
		setBase(base);
		setInternalPath(internalPath);
		this.staticBase = staticBase.substring(1);
	}
	
	@Override
	public WebResource getResource(String path) {
		try {
			if (this.base != null && !this.base.isEmpty()) {
				String fullPath = this.base;
				if (fullPath.contains("!/")) {
					String resourcePath = path;
					if (resourcePath.startsWith("/")) {
						resourcePath = resourcePath.substring(1);
					}

					String internalBase = this.internalPath;
					if (internalBase.startsWith("/")) {
						internalBase = internalBase.substring(1);
					}

					if (!internalBase.isEmpty() && !resourcePath.startsWith(internalBase)) {
						resourcePath = internalBase + (internalBase.endsWith("/") ? "" : "/") + resourcePath;
					}

					if (!fullPath.endsWith("/")) {
						fullPath = fullPath + "/";
					}
					 fullPath = fullPath + internalBase;
				} else {
					String basePath = fullPath;
					String resourcePath = path;
					if (resourcePath.startsWith("/")) {
						resourcePath = resourcePath.substring(1);
					}
					if (!basePath.endsWith("/")) {
						basePath = basePath + "/";
					}
				}
				URL url;
				if (fullPath.startsWith("jar:")) {
					URL tempUrl = new URL(null, fullPath, new NestedJarURLStreamHandler());
					url = tempUrl;
				} else {
					URL tempUrl = new URL("jar", "", -1, fullPath, new NestedJarURLStreamHandler());
					url = tempUrl;
				}
				JarURLResource resource = new JarURLResource(url, path, staticBase);
				return resource;
			}
			else if (this.jarUrl != null) {
				String file = this.jarUrl.getFile().replaceAll("jar:", "") + path;
				file = file.replace(":////", "://");
				URL url = new URL("jar", "", -1, file, new NestedJarURLStreamHandler());
				JarURLResource resource = new JarURLResource(url, path, staticBase);
				return resource;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String[] list(String path) {
		return new String[0];
	}

	@Override
	public Set<String> listWebAppPaths(String path) {
		return Set.of();
	}

	@Override
	public boolean mkdir(String path) {
		return false;
	}

	@Override
	public boolean write(String path, java.io.InputStream is, boolean overwrite) {
		return false;
	}

	@Override
	public void setRoot(WebResourceRoot root) {
		this.root = root;
	}

	protected final WebResourceRoot getRoot() {
		return root;
	}

	protected final void setBase(String base) {
		this.base = base;
		try {
			if (base != null && base.startsWith("jar:")) {
				URL tempUrl = new URL(null, base, new NestedJarURLStreamHandler());
				this.jarUrl = tempUrl;
			} else if (base != null) {
				if (base.startsWith("file:")) {
					URL tempUrl = new URL("jar", "", -1, base, new NestedJarURLStreamHandler());
					this.jarUrl = tempUrl;
				} else {
					URL tempUrl = new URL(null, base, new NestedJarURLStreamHandler());
					this.jarUrl = tempUrl;
				}
			}
		} catch (Exception e) {
		}
	}

	protected final void setInternalPath(String internalPath) {
		this.internalPath = internalPath;
	}

	@Override
	public boolean getClassLoaderOnly() {
		return classLoaderOnly;
	}

	@Override
	public void setClassLoaderOnly(boolean classLoaderOnly) {
		this.classLoaderOnly = classLoaderOnly;
	}

	@Override
	public boolean getStaticOnly() {
		return staticOnly;
	}

	@Override
	public void setStaticOnly(boolean staticOnly) {
		this.staticOnly = staticOnly;
	}

	@Override
	public URL getBaseUrl() {
		if (jarUrl != null) {
			return jarUrl;
		}
		if (base != null) {
			try {
				if (base.startsWith("jar:")) {
					URL tempUrl = new URL(null, base, new NestedJarURLStreamHandler());
					return tempUrl;
				} else {
					URL tempUrl = new URL("jar", "", -1, base, new NestedJarURLStreamHandler());
					return tempUrl;
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void gc() {
	}

	@Override
	public void setAllowLinking(boolean allowLinking) {
		this.allowLinking = allowLinking;
	}

	@Override
	public boolean getAllowLinking() {
		return allowLinking;
	}

	@Override
	public void init() throws LifecycleException {
	}

	@Override
	public void start() throws LifecycleException {
	}

	@Override
	public void stop() throws LifecycleException {
	}

	@Override
	public void destroy() throws LifecycleException {
	}

	@Override
	public LifecycleState getState() {
		return LifecycleState.STARTED;
	}

	@Override
	public String getStateName() {
		return getState().toString();
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
	}

	@Override
	public LifecycleListener[] findLifecycleListeners() {
		return new LifecycleListener[0];
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
	}
}
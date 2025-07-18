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
package club.dawdler.boot.web.undertow.resource;

import java.io.IOException;
import java.net.URL;

import club.dawdler.fatjar.loader.archive.jar.NestedJarURLStreamHandler;

import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;

/**
 * @author jackson.song
 * @version V1.0
 * jar中的资源管理器用于读取jar包中的资源文件
 */
public class JarResourceManager implements ResourceManager {

	private final URL jarUrl;

	public JarResourceManager(URL jarUrl) {
		this.jarUrl = jarUrl;
	}

	@Override
	public Resource getResource(String path) throws IOException {
		String file = this.jarUrl.getFile().replaceAll("jar:", "") + "/" + path;
		file = file.replace(":////", "://");
		URL url = new URL("jar", "", -1, file, new NestedJarURLStreamHandler());
		JarURLResource resource = new JarURLResource(url, path);
		if (path != null && !"/".equals(path) && resource.getContentLength() < 0) {
			return null;
		}
		return resource;
	}

	@Override
	public boolean isResourceChangeListenerSupported() {
		return false;
	}

	@Override
	public void registerResourceChangeListener(ResourceChangeListener listener) {
	}

	@Override
	public void removeResourceChangeListener(ResourceChangeListener listener) {
	}

	@Override
	public void close() throws IOException {
	}

}

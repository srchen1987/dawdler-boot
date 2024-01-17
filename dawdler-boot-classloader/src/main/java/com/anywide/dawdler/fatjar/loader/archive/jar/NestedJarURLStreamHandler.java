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
package com.anywide.dawdler.fatjar.loader.archive.jar;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NestedJarURLStreamHandler extends URLStreamHandler {

	private static final String FILE_PROTOCOL = "file:";
	private static final String NESTED_SYMBOL = "!/";
	private final NestedJarFile nestedJarFile;

	private static SoftReference<Map<File, NestedJarFile>> rootFileCache;

	static {
		rootFileCache = new SoftReference<>(null);
	}

	public NestedJarURLStreamHandler() {
		this(null);
	}

	public NestedJarURLStreamHandler(NestedJarFile nestedJarFile) {
		this.nestedJarFile = nestedJarFile;
	}

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		if (this.nestedJarFile != null && isUrlInJarFile(url, this.nestedJarFile)) {
			return NestedJarURLConnection.get(url, nestedJarFile);
		} else {
			try {
				return NestedJarURLConnection.get(url, getRootJarFileFromUrl(url));
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}

	}

	private boolean isUrlInJarFile(URL url, NestedJarFile nestedJarFile) throws MalformedURLException {
		return url.getPath().startsWith(nestedJarFile.getUrl().getPath())
				&& url.toString().startsWith(nestedJarFile.getUrlString());
	}

	public NestedJarFile getRootJarFileFromUrl(URL url) throws IOException {
		String spec = url.getFile();
		int separatorIndex = spec.indexOf(NESTED_SYMBOL);
		if (separatorIndex == -1) {
			throw new MalformedURLException("Jar URL does not contain !/ separator");
		}
		String name = spec.substring(0, separatorIndex);
		return getRootJarFile(name);
	}

	private NestedJarFile getRootJarFile(String name) throws IOException {
		try {
			if (!name.startsWith(FILE_PROTOCOL)) {
				throw new IllegalStateException("Not a file URL");
			}
			File file = new File(URI.create(name));
			Map<File, NestedJarFile> cache = rootFileCache.get();
			NestedJarFile result = (cache != null) ? cache.get(file) : null;
			if (result == null) {
				result = new NestedJarFile(file);
				addToRootFileCache(file, result);
			}
			return result;
		} catch (Exception ex) {
			throw new IOException("Unable to open root Jar file '" + name + "'", ex);
		}
	}

	static void addToRootFileCache(File sourceFile, NestedJarFile jarFile) {
		Map<File, NestedJarFile> cache = rootFileCache.get();
		if (cache == null) {
			cache = new ConcurrentHashMap<>();
			rootFileCache = new SoftReference<>(cache);
		}
		cache.put(sourceFile, jarFile);
	}

}

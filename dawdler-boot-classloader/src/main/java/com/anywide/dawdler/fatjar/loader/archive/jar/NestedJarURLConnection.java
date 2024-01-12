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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.anywide.dawdler.fatjar.loader.archive.jar.NestedJarFile.JarFileType;

public class NestedJarURLConnection extends java.net.JarURLConnection {
	private static final URL IGNORE_URL;
	static {
		try {
			IGNORE_URL = new URL("jar:", null, 0, "file:!/", new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					return null;
				}
			});
		} catch (MalformedURLException ex) {
			throw new IllegalStateException(ex);
		}
	}
	private static final JarEntryName EMPTY_JAR_ENTRY_NAME = new JarEntryName("");
	private static final IllegalStateException NOT_FOUND_CONNECTION_EXCEPTION = new IllegalStateException(
			new FileNotFoundException("Jar file or entry not found!"));
	private static final String SEPARATOR = "!/";
	private NestedJarFile nestedJarFile;
	private JarEntry jarEntry;
	private JarEntryName jarEntryName;
	private Permission permission;

	public NestedJarURLConnection(URL url, NestedJarFile nestedJarFile) throws IOException {
		super(IGNORE_URL);
		this.nestedJarFile = nestedJarFile;
		String spec = url.getFile();
		int index = indexOfRootSpec(spec, nestedJarFile.getPathFromRoot());
		if (index == -1) {
			this.jarEntryName = EMPTY_JAR_ENTRY_NAME;
			this.nestedJarFile = null;
			return;
		}
		int separator;
		while ((separator = spec.indexOf(SEPARATOR, index)) > 0) {
			JarEntryName entryName = JarEntryName.get(spec.substring(index, separator));
			JarEntry jarEntry = nestedJarFile.getJarEntry(entryName.toCharSequence());
			if (jarEntry == null) {
				this.jarEntryName = EMPTY_JAR_ENTRY_NAME;
				this.nestedJarFile = null;
			}
			this.nestedJarFile = nestedJarFile.getNestedJarFile(jarEntry);
			index = separator + SEPARATOR.length();
		}
		jarEntryName = JarEntryName.get(spec, index);
		if (!jarEntryName.isEmpty() && !nestedJarFile.containsEntry(jarEntryName.toString())) {
			this.jarEntryName = EMPTY_JAR_ENTRY_NAME;
			this.nestedJarFile = null;
		}
	}

	public NestedJarURLConnection(URL url, NestedJarFile nestedJarFile, JarEntryName jarEntryName) throws IOException {
		super(IGNORE_URL);
		this.url = url;
		this.nestedJarFile = nestedJarFile;
		this.jarEntryName = jarEntryName;
		this.useCaches = true;
	}

	private static final NestedJarURLConnection NOT_FOUND_CONNECTION = NestedJarURLConnection.notFound();

	private static NestedJarURLConnection notFound() {
		try {
			return notFound(null, null);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static NestedJarURLConnection notFound(NestedJarFile jarFile, JarEntryName jarEntryName)
			throws IOException {
		return new NestedJarURLConnection(null, jarFile, jarEntryName);
	}

	public static NestedJarURLConnection get(URL url, NestedJarFile jarFile) throws IOException {
		String spec = url.getFile();
		int index = indexOfRootSpec(spec, jarFile.getPathFromRoot());
		if (index == -1) {
			new NestedJarURLConnection(url, null, EMPTY_JAR_ENTRY_NAME);
		}
		int separator;
		while ((separator = spec.indexOf(SEPARATOR, index)) > 0) {
			JarEntryName entryName = JarEntryName.get(spec.substring(index, separator));
			JarEntry jarEntry = jarFile.getJarEntry(entryName.toCharSequence());
			if (jarEntry == null) {
				return NestedJarURLConnection.notFound(jarFile, entryName);
			}
			jarFile = jarFile.getNestedJarFile(jarEntry);
			index = separator + SEPARATOR.length();
		}
		JarEntryName jarEntryName = JarEntryName.get(spec, index);
		if (!jarEntryName.isEmpty() && !jarFile.containsEntry(jarEntryName.toString())) {
			return NOT_FOUND_CONNECTION;
		}
		return new NestedJarURLConnection(url, jarFile, jarEntryName);
	}

	private static int indexOfRootSpec(String file, String pathFromRoot) {
		int separatorIndex = file.indexOf(SEPARATOR);
		if (separatorIndex < 0 || !file.startsWith(pathFromRoot, separatorIndex)) {
			return -1;
		}
		return separatorIndex + SEPARATOR.length() + pathFromRoot.length();
	}

	@Override
	public JarFile getJarFile() throws IOException {
		connect();
		return nestedJarFile;
	}

	@Override
	public void connect() throws IOException {
		if (this.nestedJarFile == null) {
			throw new FileNotFoundException("Jar file or entry not found!");
		}
		if (!this.jarEntryName.isEmpty() && this.jarEntry == null) {
			this.jarEntry = this.nestedJarFile.getJarEntry(getEntryName());
			if (this.jarEntry == null) {
				throw new FileNotFoundException(
						"JAR entry " + getEntryName() + " not found in " + nestedJarFile.getName());
			}
		}
		this.connected = true;

	}

	@Override
	public String getEntryName() {
		if (this.nestedJarFile == null) {
			throw NOT_FOUND_CONNECTION_EXCEPTION;
		}
		return this.jarEntryName.toString();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (this.nestedJarFile == null) {
			throw new FileNotFoundException("Jar file or entry not found!");
		}
		if (this.jarEntryName.isEmpty() && this.nestedJarFile.getType() == JarFileType.DIRECT) {
			throw new IOException("no entry name specified");
		}
		connect();
		InputStream inputStream = (this.jarEntryName.isEmpty() ? this.nestedJarFile.getInputStream()
				: this.nestedJarFile.getInputStream(this.jarEntry));
		if (inputStream == null) {
			throwFileNotFound(this.jarEntryName, this.nestedJarFile);
		}
		return inputStream;
	}

	private void throwFileNotFound(Object entry, NestedJarFile jarFile) throws FileNotFoundException {
		throw new FileNotFoundException("JAR entry " + entry + " not found in " + jarFile.getName());
	}

	@Override
	public int getContentLength() {
		long length = getContentLengthLong();
		if (length > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) length;
	}

	@Override
	public long getContentLengthLong() {
		if (this.nestedJarFile == null) {
			return -1;
		}
		try {
			if (this.jarEntryName.isEmpty()) {
				return this.nestedJarFile.size();
			}
			JarEntry entry = getJarEntry();
			return (entry != null) ? (int) entry.getSize() : -1;
		} catch (IOException ex) {
			return -1;
		}
	}

	@Override
	public Object getContent() throws IOException {
		connect();
		return this.jarEntryName.isEmpty() ? this.nestedJarFile : super.getContent();
	}

	@Override
	public String getContentType() {
		return (this.jarEntryName != null) ? this.jarEntryName.getContentType() : null;
	}

	@Override
	public Permission getPermission() throws IOException {
		if (this.nestedJarFile == null) {
			throw new FileNotFoundException("Jar file or entry not found!");
		}
		if (this.permission == null) {
			this.permission = this.nestedJarFile.getPermission();
		}
		return this.permission;
	}

	@Override
	public JarEntry getJarEntry() throws IOException {
		return jarEntry;
	}

	@Override
	public boolean getUseCaches() {
		return true;
	}

}

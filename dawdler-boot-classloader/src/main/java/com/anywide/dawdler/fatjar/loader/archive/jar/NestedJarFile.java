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
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;

import com.anywide.dawdler.fatjar.loader.file.RandomAccessData;
import com.anywide.dawdler.fatjar.loader.file.RandomAccessDataFile;

public class NestedJarFile extends JarFile {

	enum JarFileType {
		DIRECT, NESTED_DIRECTORY, NESTED_JAR
	}

	private URL url;
	private final RandomAccessDataFile rootFile;
	private String pathFromRoot;
	private String urlString;
	public static final String FILE_READ_ACTION = "read";
	private final JarFileEntries entries;

	private final Supplier<Manifest> manifestSupplier;

	private SoftReference<Manifest> manifest;

	private static final AsciiBytes META_INF = new AsciiBytes("META-INF/");
	private static final AsciiBytes SIGNATURE_FILE_EXTENSION = new AsciiBytes(".SF");

	private boolean signed;

	private String comment;

	private final JarFileType type;

	private volatile boolean closed;

	private final RandomAccessData data;

	public NestedJarFile(File file) throws IOException {
		this(new RandomAccessDataFile(file));
	}

	NestedJarFile(RandomAccessDataFile file) throws IOException {
		this(file, "", file, JarFileType.DIRECT);
	}

	private NestedJarFile(RandomAccessDataFile rootFile, String pathFromRoot, RandomAccessData data, JarFileType type)
			throws IOException {
		this(rootFile, pathFromRoot, data, null, type, null);
	}

	private NestedJarFile(RandomAccessDataFile rootFile, String pathFromRoot, RandomAccessData data,
			JarEntryFilter filter, JarFileType type, Supplier<Manifest> manifestSupplier) throws IOException {
		super(rootFile.getFile());
		super.close();
		this.rootFile = rootFile;
		this.pathFromRoot = pathFromRoot;
		CentralDirectoryParser parser = new CentralDirectoryParser();
		this.entries = parser.addVisitor(new JarFileEntries(this, filter));
		this.type = type;
		parser.addVisitor(centralDirectoryVisitor());
		try {
			this.data = parser.parse(data, filter == null);
		} catch (RuntimeException ex) {
			try {
				this.rootFile.close();
				super.close();
			} catch (IOException ioex) {
			}
			throw ex;
		}
		this.manifestSupplier = (manifestSupplier != null) ? manifestSupplier : () -> {
			try (InputStream inputStream = getInputStream(MANIFEST_NAME)) {
				if (inputStream == null) {
					return null;
				}
				return new Manifest(inputStream);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		};
	}

	private CentralDirectoryVisitor centralDirectoryVisitor() {
		return new CentralDirectoryVisitor() {

			@Override
			public void visitStart(CentralDirectoryEndRecord endRecord, RandomAccessData centralDirectoryData) {
				NestedJarFile.this.comment = endRecord.getComment();
			}

			@Override
			public void visitFileHeader(CentralDirectoryFileHeader fileHeader, long dataOffset) {
				AsciiBytes name = fileHeader.getName();
				if (name.startsWith(META_INF) && name.endsWith(SIGNATURE_FILE_EXTENSION)) {
					NestedJarFile.this.signed = true;
				}
			}

			@Override
			public void visitEnd() {
			}

		};
	}

	public URL getUrl() throws MalformedURLException {
		if (this.url == null) {
			String file = this.rootFile.getFile().toURI() + this.pathFromRoot + "!/";
			file = file.replace(":////", "://");
			this.url = new URL("jar", "", -1, file, new NestedJarURLStreamHandler(this));
		}
		return this.url;
	}

	public String getUrlString() throws MalformedURLException {
		if (this.urlString == null) {
			this.urlString = getUrl().toString();
		}
		return this.urlString;
	}

	public Permission getPermission() {
		return new FilePermission(this.rootFile.getFile().getPath(), FILE_READ_ACTION);
	}

	@Override
	public Manifest getManifest() throws IOException {
		Manifest manifest = (this.manifest != null) ? this.manifest.get() : null;
		if (manifest == null) {
			try {
				manifest = this.manifestSupplier.get();
			} catch (RuntimeException ex) {
				throw new IOException(ex);
			}
			this.manifest = new SoftReference<>(manifest);
		}
		return manifest;
	}

	@Override
	public Enumeration<JarEntry> entries() {
		return new JarEntryEnumeration(this.entries.iterator());
	}

	@Override
	public Stream<JarEntry> stream() {
		Spliterator<JarEntry> spliterator = Spliterators.spliterator(iterator(), size(),
				Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL);
		return StreamSupport.stream(spliterator, false);
	}

	public Iterator<NestedJarEntry> iterator() {
		return (Iterator<NestedJarEntry>) this.entries.iterator(this::ensureOpen);
	}

	public JarEntry getJarEntry(CharSequence name) {
		return this.entries.getEntry(name);
	}

	@Override
	public JarEntry getJarEntry(String name) {
		return (JarEntry) getEntry(name);
	}

	public boolean containsEntry(String name) {
		return this.entries.containsEntry(name);
	}

	@Override
	public ZipEntry getEntry(String name) {
		ensureOpen();
		return this.entries.getEntry(name);
	}

	InputStream getInputStream() throws IOException {
		return this.data.getInputStream();
	}

	public synchronized InputStream getInputStream(ZipEntry entry) throws IOException {
		ensureOpen();
		if (entry instanceof NestedJarEntry) {
			NestedJarEntry jarEntry = (NestedJarEntry) entry;
			return this.entries.getInputStream(jarEntry);
		}
		return getInputStream((entry != null) ? entry.getName() : null);
	}

	InputStream getInputStream(String name) throws IOException {
		return this.entries.getInputStream(name);
	}

	public synchronized NestedJarFile getNestedJarFile(ZipEntry entry) throws IOException {
		return getNestedJarFile((NestedJarEntry) entry);
	}

	public synchronized NestedJarFile getNestedJarFile(NestedJarEntry entry) throws IOException {
		try {
			return createJarFileFromEntry(entry);
		} catch (Exception ex) {
			throw new IOException("Unable to open nested jar file '" + entry.getName() + "'", ex);
		}
	}

	private NestedJarFile createJarFileFromEntry(NestedJarEntry entry) throws IOException {
		if (entry.isDirectory()) {
			return createJarFileFromDirectoryEntry(entry);
		}
		return createJarFileFromFileEntry(entry);
	}

	private NestedJarFile createJarFileFromDirectoryEntry(NestedJarEntry entry) throws IOException {
		AsciiBytes name = entry.getAsciiBytesName();
		JarEntryFilter filter = (candidate) -> {
			if (candidate.startsWith(name) && !candidate.equals(name)) {
				return candidate.substring(name.length());
			}
			return null;
		};
		return new NestedJarFile(this.rootFile,
				this.pathFromRoot + "!/" + entry.getName().substring(0, name.length() - 1), this.data, filter,
				JarFileType.NESTED_DIRECTORY, this.manifestSupplier);
	}

	private NestedJarFile createJarFileFromFileEntry(NestedJarEntry entry) throws IOException {
		if (entry.getMethod() != ZipEntry.STORED) {
			throw new IllegalStateException(
					"Unable to open nested entry '" + entry.getName() + "'. It has been compressed and nested "
							+ "jar files must be stored without compression. Please check the "
							+ "mechanism used to create your executable jar file");
		}
		RandomAccessData entryData = this.entries.getEntryData(entry.getName());
		return new NestedJarFile(this.rootFile, this.pathFromRoot + "!/" + entry.getName(), entryData,
				JarFileType.NESTED_JAR);
	}

	@Override
	public String getComment() {
		ensureOpen();
		return this.comment;
	}

	@Override
	public int size() {
		ensureOpen();
		return this.entries.getSize();
	}

	@Override
	public void close() throws IOException {
		if (this.closed) {
			return;
		}
		super.close();
		if (this.type == JarFileType.DIRECT) {
			this.rootFile.close();
		}
		this.closed = true;
	}

	public JarFileType getType() {
		return this.type;
	}

	private void ensureOpen() {
		if (this.closed) {
			throw new IllegalStateException("zip file closed!");
		}
	}

	boolean isClosed() {
		return this.closed;
	}

	boolean isSigned() {
		return this.signed;
	}

	public void clearCache() {
		this.entries.clearCache();
	}

	public RandomAccessData getData() {
		return data;
	}

	public String getPathFromRoot() {
		return pathFromRoot;
	}

	public void setPathFromRoot(String pathFromRoot) {
		this.pathFromRoot = pathFromRoot;
	}

	JarEntryCertification getCertification(NestedJarEntry entry) {
		try {
			return this.entries.getCertification(entry);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static class JarEntryEnumeration implements Enumeration<JarEntry> {

		private final Iterator<NestedJarEntry> iterator;

		JarEntryEnumeration(Iterator<NestedJarEntry> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasMoreElements() {
			return this.iterator.hasNext();
		}

		@Override
		public JarEntry nextElement() {
			return this.iterator.next();
		}

	}

}

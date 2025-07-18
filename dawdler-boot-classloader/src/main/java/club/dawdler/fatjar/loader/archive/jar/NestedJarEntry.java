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
package club.dawdler.fatjar.loader.archive.jar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

public class NestedJarEntry extends JarEntry implements FileHeader {

	private final int index;

	private final AsciiBytes name;

	private final AsciiBytes headerName;

	private final NestedJarFile jarFile;

	private final long localHeaderOffset;

	private volatile JarEntryCertification certification;

	NestedJarEntry(NestedJarFile jarFile, int index, CentralDirectoryFileHeader header, AsciiBytes nameAlias) {
		super((nameAlias != null) ? nameAlias.toString() : header.getName().toString());
		this.index = index;
		this.name = (nameAlias != null) ? nameAlias : header.getName();
		this.headerName = header.getName();
		this.jarFile = jarFile;
		this.localHeaderOffset = header.getLocalHeaderOffset();
		setCompressedSize(header.getCompressedSize());
		setMethod(header.getMethod());
		setCrc(header.getCrc());
		setComment(header.getComment().toString());
		setSize(header.getSize());
		setTime(header.getTime());
		if (header.hasExtra()) {
			setExtra(header.getExtra());
		}
	}

	int getIndex() {
		return this.index;
	}

	AsciiBytes getAsciiBytesName() {
		return this.name;
	}

	@Override
	public boolean hasName(CharSequence name, char suffix) {
		return this.headerName.matches(name, suffix);
	}

	URL getUrl() throws MalformedURLException {
		return new URL(this.jarFile.getUrl(), getName());
	}

	@Override
	public Attributes getAttributes() throws IOException {
		Manifest manifest = this.jarFile.getManifest();
		return (manifest != null) ? manifest.getAttributes(getName()) : null;
	}

	@Override
	public Certificate[] getCertificates() {
		return getCertification().getCertificates();
	}

	@Override
	public CodeSigner[] getCodeSigners() {
		return getCertification().getCodeSigners();
	}

	private JarEntryCertification getCertification() {
		if (!this.jarFile.isSigned()) {
			return JarEntryCertification.NONE;
		}
		JarEntryCertification certification = this.certification;
		if (certification == null) {
			certification = this.jarFile.getCertification(this);
			this.certification = certification;
		}
		return certification;
	}

	@Override
	public long getLocalHeaderOffset() {
		return this.localHeaderOffset;
	}

}

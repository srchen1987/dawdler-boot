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
package club.dawdler.fatjar.loader.launcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;

import club.dawdler.fatjar.loader.archive.Archive;

public abstract class ExecutableArchiveLauncher extends Launcher {

	private static final String START_CLASS_ATTRIBUTE = "Start-Class";

	private final Archive archive;

	public ExecutableArchiveLauncher() {
		try {
			this.archive = createArchive();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	protected ExecutableArchiveLauncher(Archive archive) {
		try {
			this.archive = archive;
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	protected String getMainClass() throws Exception {
		Manifest manifest = this.archive.getManifest();
		String mainClass = null;
		if (manifest != null) {
			mainClass = manifest.getMainAttributes().getValue(START_CLASS_ATTRIBUTE);
		}
		if (mainClass == null) {
			throw new IllegalStateException("No 'Start-Class' manifest entry specified in " + this);
		}
		return mainClass;
	}

	@Override
	protected ClassLoader createClassLoader(Iterator<Archive> archives) throws Exception {
		List<URL> urls = new ArrayList<>(30);
		while (archives.hasNext()) {
			URL url = archives.next().getUrl();
			urls.add(url);
		}
		return createClassLoader(urls.toArray(new URL[0]));
	}

	@Override
	protected Iterator<Archive> getClassPathArchivesIterator() throws Exception {
		Archive.EntryFilter searchFilter = this::isSearchCandidate;
		Iterator<Archive> archives = this.archive.getNestedArchives(searchFilter, (entry) -> isNestedArchive(entry));
		if (isPostProcessingClassPathArchives()) {
			archives = applyClassPathArchivePostProcessing(archives);
		}
		return archives;
	}

	private Iterator<Archive> applyClassPathArchivePostProcessing(Iterator<Archive> archives) throws Exception {
		List<Archive> list = new ArrayList<>();
		while (archives.hasNext()) {
			list.add(archives.next());
		}
		postProcessClassPathArchives(list);
		return list.iterator();
	}

	protected boolean isSearchCandidate(Archive.Entry entry) {
		if (getArchiveEntryPathPrefix() == null) {
			return true;
		}
		return entry.getName().startsWith(getArchiveEntryPathPrefix());
	}

	protected abstract boolean isNestedArchive(Archive.Entry entry);

	protected boolean isPostProcessingClassPathArchives() {
		return true;
	}

	protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
	}

	protected String getArchiveEntryPathPrefix() {
		return null;
	}

	@Override
	protected final Archive getArchive() {
		return this.archive;
	}

}

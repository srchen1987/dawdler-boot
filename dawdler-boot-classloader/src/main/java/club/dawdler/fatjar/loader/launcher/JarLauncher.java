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

import club.dawdler.fatjar.loader.archive.Archive;
import club.dawdler.fatjar.loader.archive.Archive.EntryFilter;

public class JarLauncher extends ExecutableArchiveLauncher {

	static final EntryFilter NESTED_ARCHIVE_ENTRY_FILTER = (entry) -> {
		return entry.getName().startsWith("BOOT-INF/main/lib/") || entry.getName().startsWith("BOOT-INF/lib/");
	};

	public JarLauncher() {
	}

	@Override
	protected boolean isPostProcessingClassPathArchives() {
		return false;
	}

	@Override
	protected boolean isNestedArchive(Archive.Entry entry) {
		return NESTED_ARCHIVE_ENTRY_FILTER.matches(entry);
	}

	@Override
	protected String getArchiveEntryPathPrefix() {
		return "BOOT-INF/";
	}

	public static void main(String[] args) throws Exception {
		new JarLauncher().launch(args);
	}

}

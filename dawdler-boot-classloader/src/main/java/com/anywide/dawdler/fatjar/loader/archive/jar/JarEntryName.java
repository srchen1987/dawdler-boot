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

public class JarEntryName {

	private static final JarEntryName EMPTY_JAR_ENTRY_NAME = new JarEntryName("");
	private final String name;

	private String contentType;

	JarEntryName(String name) {
		this.name = name;
	}

	String toCharSequence() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name.toString();
	}

	boolean isEmpty() {
		return this.name.isEmpty();
	}

	String getContentType() {
		if (this.contentType == null) {
			this.contentType = deduceContentType();
		}
		return this.contentType;
	}

	private String deduceContentType() {
		String type = isEmpty() ? "x-java/jar" : null;
		type = (type != null) ? type : "content/unknown";
		return type;
	}

	static JarEntryName get(String spec) {
		return get(spec, 0);
	}

	static JarEntryName get(String spec, int beginIndex) {
		if (spec.length() <= beginIndex) {
			return EMPTY_JAR_ENTRY_NAME;
		}
		return new JarEntryName(spec.substring(beginIndex));
	}

}

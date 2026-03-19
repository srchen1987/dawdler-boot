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
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * @author jackson.song
 * @version V1.0
 * tld资源路径
 */
public class TldResourcePath {
	private final URL url;
	private final String webappPath;
	private final String entryName;

	public TldResourcePath(URL url, String webappPath) {
		this(url, webappPath, null);
	}

	public TldResourcePath(URL url, String webappPath, String entryName) {
		this.url = url;
		this.webappPath = webappPath;
		this.entryName = entryName;
	}

	public URL getUrl() {
		return url;
	}

	public String getWebappPath() {
		return webappPath;
	}

	public String getEntryName() {
		return entryName;
	}

	public String toExternalForm() {
		if (entryName == null) {
			return url.toExternalForm();
		} else {
			return "jar:" + url.toExternalForm() + "!/" + entryName;
		}
	}

	public InputStream openStream() throws IOException {
		if (entryName == null) {
			return url.openStream();
		} else {
			URL entryUrl = new URL("jar:" + url.toExternalForm() + "!/" + entryName);
			return entryUrl.openStream();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TldResourcePath other = (TldResourcePath) o;

		return url.equals(other.url) && Objects.equals(webappPath, other.webappPath) &&
				Objects.equals(entryName, other.entryName);
	}

	@Override
	public int hashCode() {
		int result = url.hashCode();
		result = result * 31 + Objects.hashCode(webappPath);
		result = result * 31 + Objects.hashCode(entryName);
		return result;
	}
}
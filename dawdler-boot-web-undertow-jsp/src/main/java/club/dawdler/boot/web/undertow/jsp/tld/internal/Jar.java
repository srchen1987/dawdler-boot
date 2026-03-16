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
import java.util.jar.Manifest;

/**
 * @author jackson.song
 * @version V1.0
 * jar接口
 */
public interface Jar extends AutoCloseable {
	URL getJarFileURL();
	InputStream getInputStream(String name) throws IOException;
	long getLastModified(String name) throws IOException;
	boolean exists(String name) throws IOException;
	@Override
	void close();
	void nextEntry();
	String getEntryName();
	InputStream getEntryInputStream() throws IOException;
	String getURL(String entry);
	Manifest getManifest() throws IOException;
	void reset() throws IOException;
}
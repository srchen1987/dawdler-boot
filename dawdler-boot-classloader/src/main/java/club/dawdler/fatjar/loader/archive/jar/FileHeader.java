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

import java.util.zip.ZipEntry;

interface FileHeader {

	/**
	 * Returns {@code true} if the header has the given name.
	 * 
	 * @param name   the name to test
	 * @param suffix an additional suffix (or {@code 0})
	 * @return {@code true} if the header has the given name
	 */
	boolean hasName(CharSequence name, char suffix);

	/**
	 * Return the offset of the load file header within the archive data.
	 * 
	 * @return the local header offset
	 */
	long getLocalHeaderOffset();

	/**
	 * Return the compressed size of the entry.
	 * 
	 * @return the compressed size.
	 */
	long getCompressedSize();

	/**
	 * Return the uncompressed size of the entry.
	 * 
	 * @return the uncompressed size.
	 */
	long getSize();

	/**
	 * Return the method used to compress the data.
	 * 
	 * @return the zip compression method
	 * @see ZipEntry#STORED
	 * @see ZipEntry#DEFLATED
	 */
	int getMethod();

}

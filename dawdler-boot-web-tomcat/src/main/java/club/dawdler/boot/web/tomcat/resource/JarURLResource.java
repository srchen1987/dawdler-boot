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
package club.dawdler.boot.web.tomcat.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.catalina.WebResource;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import club.dawdler.fatjar.loader.archive.jar.NestedJarURLConnection;

/**
 * @author jackson.song
 * @version V1.0
 * jar资源中URL实现
 */
public class JarURLResource implements WebResource {
	private static final Log log = LogFactory.getLog(JarURLResource.class);

	private final URL url;
	private final String path;

	private boolean connectionOpened = false;
	private Date lastModified;
	private Long contentLength;
	private Boolean isDirectory;
	private Boolean exists;
	private String name;
	private String mimeType;
	private final String staticBase;

	public JarURLResource(final URL url, String path, String staticBase) {
		this.url = url;
		this.path = path;
		this.staticBase = staticBase;
	}

	@Override
	public long getLastModified() {
		openConnection();
		return lastModified != null ? lastModified.getTime() : 0;
	}

	@Override
	public String getLastModifiedHttp() {
		return new Date(getLastModified()).toString();
	}

	private void openConnection() {
		if (!connectionOpened) {
			connectionOpened = true;
			URLConnection connection = null;
			try {
				try {
					connection = url.openConnection();
				} catch (IOException e) {
					lastModified = null;
					contentLength = null;
					return;
				}
				if (url.getProtocol().equals("jar")) {
					connection.setUseCaches(false);
					try {
						JarFile jarFile = ((NestedJarURLConnection) connection).getJarFile();
						String entryPath = staticBase + path;
						ZipEntry entry = jarFile.getEntry(entryPath);
						if (entry != null) {
							lastModified = new Date(entry.getLastModifiedTime().toMillis());
							exists = true;
							contentLength = entry.getSize();
						} else {
							exists = false;
							contentLength = -1L;
						}
					} catch (Exception e) {
					}
				}

			} finally {
				if (connection != null) {
					try {
						InputStream is = connection.getInputStream();
						if (is != null) {
							is.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}

	@Override
	public boolean exists() {
		if (exists == null) {
			openConnection();
		}
		return exists != null ? exists : false;
	}

	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		if (isDirectory == null) {
			Path file = getFilePath();
			if (file != null) {
				isDirectory = Files.isDirectory(file);
			} else if (url.getProtocol().equals("jar")) {
				isDirectory = path.endsWith("/") || (exists() && getContentLength() == -1);
			} else if (url.getPath().endsWith("/")) {
				isDirectory = true;
			} else {
				isDirectory = false;
			}
		}
		return isDirectory;
	}

	@Override
	public boolean isFile() {
		return !isDirectory();
	}

	@Override
	public boolean delete() {
		return false;
	}

	@Override
	public String getName() {
		if (name == null) {
			String path = this.path;
			if (path == null || path.isEmpty()) {
				path = url.getPath();
			}
			int jarSeparatorIndex = path.indexOf("!/");
			if (jarSeparatorIndex != -1) {
				path = path.substring(jarSeparatorIndex + 2);
			}
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			int sepIndex = path.lastIndexOf("/");
			if (sepIndex != -1) {
				path = path.substring(sepIndex + 1);
			}
			name = path;
		}
		return name;
	}

	@Override
	public long getContentLength() {
		openConnection();
		return contentLength != null ? contentLength : -1;
	}

	@Override
	public String getCanonicalPath() {
		Path filePath = getFilePath();
		if (filePath != null) {
			try {
				return filePath.toRealPath().toString();
			} catch (IOException e) {
				return filePath.toString();
			}
		}
		return null;
	}

	@Override
	public boolean canRead() {
		return exists();
	}

	@Override
	public String getWebappPath() {
		return path;
	}

	@Override
	public String getETag() {
		return null;
	}

	@Override
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public String getMimeType() {
		if (mimeType != null) {
			return mimeType;
		}

		String fileName = getName();
		if (fileName != null && !fileName.isEmpty()) {
			int index = fileName.lastIndexOf('.');
			if (index != -1 && index != fileName.length() - 1) {
				// Simple MIME type mapping - in a real implementation, this would use Tomcat's
				// MIME mapping
				String extension = fileName.substring(index + 1).toLowerCase();
				switch (extension) {
					case "html":
					case "jsp":
					case "htm":
						return "text/html";
					case "css":
						return "text/css";
					case "js":
						return "application/javascript";
					case "json":
						return "application/json";
					case "png":
						return "image/png";
					case "jpg":
					case "jpeg":
						return "image/jpeg";
					case "gif":
						return "image/gif";
					case "txt":
						return "text/plain";
					case "xml":
						return "application/xml";
					case "svg":
						return "image/svg+xml";
					case "ico":
						return "image/x-icon";
					case "pdf":
						return "application/pdf";
					case "zip":
						return "application/zip";
					case "tar":
						return "application/x-tar";
					default:
						return "application/octet-stream";
				}
			}
		}
		return "application/octet-stream";
	}

	@Override
	public InputStream getInputStream() {
		URLConnection connection = null;
		try {
			connection = url.openConnection();
			connection.setUseCaches(false);
			JarFile jarFile = ((NestedJarURLConnection) connection).getJarFile();
			InputStream is = jarFile.getInputStream(jarFile.getEntry(staticBase + path));
			return is;
		} catch (IOException e) {
			log.warn("Failed to open input stream for URL: " + url, e);
			return null;
		}finally{
			if (connection != null) {
				try {
					InputStream is = connection.getInputStream();
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public byte[] getContent() {
		try (InputStream is = getInputStream()) {
			if (is == null) {
				return null;
			}
			byte[] bytes = new byte[is.available()];
				int read = is.read(bytes);
				if (read != bytes.length) {
					byte[] actualBytes = new byte[read];
					System.arraycopy(bytes, 0, actualBytes, 0, read);
					return actualBytes;
				}
			return bytes;
		} catch (IOException e) {
			log.warn("Failed to read content for URL: " + url, e);
			return null;
		}
	}

	@Override
	public long getCreation() {
		return getLastModified();
	}

	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public org.apache.catalina.WebResourceRoot getWebResourceRoot() {
		return null;
	}

	@Override
	public java.security.cert.Certificate[] getCertificates() {
		return null;
	}

	@Override
	public java.util.jar.Manifest getManifest() {
		return null;
	}

	public Path getFilePath() {
		if (url.getProtocol().equals("file")) {
			try {
				return Paths.get(url.toURI());
			} catch (URISyntaxException e) {
				return null;
			}
		}
		return null;
	}

	public String getCacheKey() {
		return url.toString();
	}

	@Override
	public URL getCodeBase() {
		return url;
	}
}
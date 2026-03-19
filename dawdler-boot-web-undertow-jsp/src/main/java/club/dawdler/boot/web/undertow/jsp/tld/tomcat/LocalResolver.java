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
package club.dawdler.boot.web.undertow.jsp.tld.tomcat;

import java.io.IOException;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

/**
 * Custom implementation of {@link EntityResolver2} that resolves entities against local files when possible.
 * <p>
 * This class is not thread safe.
 */
public class LocalResolver implements EntityResolver2 {

    private final Map<String, String> publicIds;
    private final Map<String, String> systemIds;
    private final boolean blockExternal;

    public LocalResolver(Map<String, String> publicIds, Map<String, String> systemIds, boolean blockExternal) {
        this.publicIds = publicIds;
        this.systemIds = systemIds;
        this.blockExternal = blockExternal;
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return resolveEntity(null, publicId, null, systemId);
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
            throws SAXException, IOException {

        // First try resolving against the public ID
        String resolvedSystemId = null;
        if (publicId != null) {
            resolvedSystemId = publicIds.get(publicId);
        }

        // If that doesn't work, try resolving against the system ID
        if (resolvedSystemId == null && systemId != null) {
            resolvedSystemId = systemIds.get(systemId);
        }

        // If that doesn't work, try resolving against the system ID as a URL
        if (resolvedSystemId == null && systemId != null) {
            resolvedSystemId = systemIds.get(systemId.substring(systemId.lastIndexOf('/') + 1));
        }

        // If that doesn't work, block external resolution if requested
        if (resolvedSystemId == null && blockExternal) {
            return new InputSource(
                    "data:," + "Could not resolve XML resource [{0}] with public ID [{1}], system ID [{2}] and base URI [{3}] to a known, local entity."
							.replace("{0}", name == null ? "" : name)
							.replace("{1}", publicId == null ? "" : publicId)
							.replace("{2}", systemId == null ? "" : systemId)
							.replace("{3}", baseURI == null ? "" : baseURI));
        }

        // If we have a resolved system ID, use it
        if (resolvedSystemId != null) {
            return new InputSource(resolvedSystemId);
        }

        return null;
    }
}
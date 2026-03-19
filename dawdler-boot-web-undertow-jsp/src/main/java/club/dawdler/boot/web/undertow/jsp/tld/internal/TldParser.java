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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author jackson.song
 * @version V1.0
 * tld解析器
 */
public class TldParser {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	public TldParser(boolean namespaceAware, boolean validation, boolean blockExternal) {
	}

	public TaglibXml parse(TldResourcePath path) throws IOException, SAXException {
		try (InputStream is = path.openStream()) {
			
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);

			TaglibXml taglibXml = new TaglibXml();

			Element root = document.getDocumentElement();

			NodeList uriNodes = root.getElementsByTagNameNS("*", "uri");
			if (uriNodes.getLength() > 0) {
				taglibXml.setUri(uriNodes.item(0).getTextContent());
			}

			NodeList shortNameNodes = root.getElementsByTagNameNS("*", "short-name");
			if (shortNameNodes.getLength() > 0) {
				taglibXml.setShortName(shortNameNodes.item(0).getTextContent());
			}

			NodeList tlibVersionNodes = root.getElementsByTagNameNS("*", "tlib-version");
			if (tlibVersionNodes.getLength() > 0) {
				taglibXml.setTlibVersion(tlibVersionNodes.item(0).getTextContent());
			}

			NodeList jspVersionNodes = root.getElementsByTagNameNS("*", "jsp-version");
			if (jspVersionNodes.getLength() > 0) {
				taglibXml.setJspVersion(jspVersionNodes.item(0).getTextContent());
			}

			NodeList infoNodes = root.getElementsByTagNameNS("*", "info");
			if (infoNodes.getLength() > 0) {
				taglibXml.setInfo(infoNodes.item(0).getTextContent());
			}

			NodeList listenerNodes = root.getElementsByTagNameNS("*", "listener");
			for (int i = 0; i < listenerNodes.getLength(); i++) {
				Element listenerElement = (Element) listenerNodes.item(i);
				NodeList listenerClassNodes = listenerElement.getElementsByTagNameNS("*", "listener-class");
				if (listenerClassNodes.getLength() > 0) {
					taglibXml.addListener(listenerClassNodes.item(0).getTextContent());
				}
			}

			NodeList tagNodes = root.getElementsByTagNameNS("*", "tag");
			for (int i = 0; i < tagNodes.getLength(); i++) {
				Element tagElement = (Element) tagNodes.item(i);
				TagXml tag = new TagXml();

				NodeList nameNodes = tagElement.getElementsByTagNameNS("*", "name");
				if (nameNodes.getLength() > 0) {
					tag.setName(nameNodes.item(0).getTextContent());
				}

				NodeList tagClassNodes = tagElement.getElementsByTagNameNS("*", "tag-class");
				if (tagClassNodes.getLength() > 0) {
					tag.setTagClass(tagClassNodes.item(0).getTextContent());
				}

				NodeList bodyContentNodes = tagElement.getElementsByTagNameNS("*", "body-content");
				if (bodyContentNodes.getLength() > 0) {
					tag.setBodyContent(bodyContentNodes.item(0).getTextContent());
				}

				NodeList infoNodes2 = tagElement.getElementsByTagNameNS("*", "info");
				if (infoNodes2.getLength() > 0) {
					tag.setInfo(infoNodes2.item(0).getTextContent());
				}

				NodeList attributeNodes = tagElement.getElementsByTagNameNS("*", "attribute");
				for (int j = 0; j < attributeNodes.getLength(); j++) {
					Element attributeElement = (Element) attributeNodes.item(j);
					TagXml.Attribute attribute = new TagXml.Attribute();

					NodeList attributeNameNodes = attributeElement.getElementsByTagNameNS("*", "name");
					if (attributeNameNodes.getLength() > 0) {
						attribute.setName(attributeNameNodes.item(0).getTextContent());
					}

					NodeList requiredNodes = attributeElement.getElementsByTagNameNS("*", "required");
					if (requiredNodes.getLength() > 0) {
						attribute.setRequired("true".equals(requiredNodes.item(0).getTextContent()));
					}

					NodeList rtexprvalueNodes = attributeElement.getElementsByTagNameNS("*", "rtexprvalue");
					if (rtexprvalueNodes.getLength() > 0) {
						attribute.setRtexprvalue("true".equals(rtexprvalueNodes.item(0).getTextContent()));
					}

					NodeList fragmentNodes = attributeElement.getElementsByTagNameNS("*", "fragment");
					if (fragmentNodes.getLength() > 0) {
						attribute.setFragment("true".equals(fragmentNodes.item(0).getTextContent()));
					}

					NodeList typeNodes = attributeElement.getElementsByTagNameNS("*", "type");
					if (typeNodes.getLength() > 0) {
						attribute.setType(typeNodes.item(0).getTextContent());
					}

					NodeList deferredValueNodes = attributeElement.getElementsByTagNameNS("*", "deferred-value");
					if (deferredValueNodes.getLength() > 0) {
						Element deferredValueElement = (Element) deferredValueNodes.item(0);
						NodeList deferredValueTypeNodes = deferredValueElement.getElementsByTagNameNS("*", "type");
						if (deferredValueTypeNodes.getLength() > 0) {
							attribute.setDeferredValueType(deferredValueTypeNodes.item(0).getTextContent());
						}
					}

					tag.addAttribute(attribute);
				}

				taglibXml.addTag(tag);
			}

			NodeList functionNodes = root.getElementsByTagNameNS("*", "function");
			for (int i = 0; i < functionNodes.getLength(); i++) {
				Element functionElement = (Element) functionNodes.item(i);

				String name = null;
				String klass = null;
				String signature = null;

				NodeList nameNodes = functionElement.getElementsByTagNameNS("*", "name");
				if (nameNodes.getLength() > 0) {
					name = nameNodes.item(0).getTextContent();
				}

				NodeList functionClassNodes = functionElement.getElementsByTagNameNS("*", "function-class");
				if (functionClassNodes.getLength() > 0) {
					klass = functionClassNodes.item(0).getTextContent();
				}

				NodeList functionSignatureNodes = functionElement.getElementsByTagNameNS("*", "function-signature");
				if (functionSignatureNodes.getLength() > 0) {
					signature = functionSignatureNodes.item(0).getTextContent();
				}

				if (name != null && klass != null && signature != null) {
					taglibXml.addFunction(name, klass, signature);
				}
			}

			return taglibXml;
		} catch (ParserConfigurationException e) {
			throw new IOException("Failed to create XML parser", e);
		}
	}

	public void setClassLoader(ClassLoader classLoader) {
	}
}
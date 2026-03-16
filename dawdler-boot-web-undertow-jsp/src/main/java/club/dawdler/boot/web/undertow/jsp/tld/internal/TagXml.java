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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.TagAttributeInfo;

/**
 * @author jackson.song
 * @version V1.0
 * 标签xml
 */
public class TagXml {
	private String name;
	private String tagClass;
	private String teiClass;
	private String bodyContent;
	private String info;
	private boolean dynamicAttributes;
	private final List<Attribute> attributes = new ArrayList<>();
	private List<TagAttributeInfo> tagAttributeInfos = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTagClass() {
		return tagClass;
	}

	public void setTagClass(String tagClass) {
		this.tagClass = tagClass;
	}

	public String getTeiClass() {
		return teiClass;
	}

	public void setTeiClass(String teiClass) {
		this.teiClass = teiClass;
	}

	public String getBodyContent() {
		return bodyContent;
	}

	public void setBodyContent(String bodyContent) {
		this.bodyContent = bodyContent;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean isDynamicAttributes() {
		return dynamicAttributes;
	}

	public void setDynamicAttributes(boolean dynamicAttributes) {
		this.dynamicAttributes = dynamicAttributes;
	}

	public void addAttribute(Attribute attribute) {
		attributes.add(attribute);
		tagAttributeInfos = null;
	}

	public List<TagAttributeInfo> getAttributes() {
		if (tagAttributeInfos == null) {
			tagAttributeInfos = new ArrayList<>();
			for (Attribute attr : attributes) {
				String type = attr.getType();
				if (!attr.isRtexprvalue() && type == null) {
					type = "java.lang.String";
				}
				TagAttributeInfo tagAttrInfo = new TagAttributeInfo(
						attr.getName(),
						attr.isRequired(),
						type,
						attr.isRtexprvalue());
				tagAttributeInfos.add(tagAttrInfo);
			}
		}
		return tagAttributeInfos;
	}

	public static class Attribute {
		private String name;
		private boolean required;
		private boolean rtexprvalue;
		private boolean fragment;
		private String type;
		private String deferredValueType;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public boolean isRtexprvalue() {
			return rtexprvalue;
		}

		public void setRtexprvalue(boolean rtexprvalue) {
			this.rtexprvalue = rtexprvalue;
		}

		public boolean isFragment() {
			return fragment;
		}

		public void setFragment(boolean fragment) {
			this.fragment = fragment;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getDeferredValueType() {
			return deferredValueType;
		}

		public void setDeferredValueType(String deferredValueType) {
			this.deferredValueType = deferredValueType;
		}
	}
}

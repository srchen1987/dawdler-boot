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

/**
 * @author jackson.song
 * @version V1.0
 * 验证器xml
 */
public class ValidatorXml {
	private String validatorClass;
	private final List<String> initParams = new ArrayList<>();

	public String getValidatorClass() {
		return validatorClass;
	}

	public void setValidatorClass(String validatorClass) {
		this.validatorClass = validatorClass;
	}

	public void addInitParam(String initParam) {
		initParams.add(initParam);
	}

	public List<String> getInitParams() {
		return initParams;
	}
}
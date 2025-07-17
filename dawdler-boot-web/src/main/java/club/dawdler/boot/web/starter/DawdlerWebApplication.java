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
package club.dawdler.boot.web.starter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import club.dawdler.boot.core.loader.DawdlerMainClassLoader;
import club.dawdler.fatjar.loader.launcher.LaunchedURLClassLoader;

/**
 * @author jackson.song
 * @version V1.0
 * dawdler-web服务入口程序(main方法调用此类)
 */
public class DawdlerWebApplication {
	public static void run(Class<?> startClass, String... args) throws Throwable {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (!(classLoader instanceof LaunchedURLClassLoader)) {
			DawdlerMainClassLoader dawdlerMainClassLoader = new DawdlerMainClassLoader(getURL(), classLoader);
			Class<?> mainClass = Class.forName("club.dawdler.boot.web.starter.DawdlerBootStarter", false,
					dawdlerMainClassLoader);
			Thread.currentThread().setContextClassLoader(dawdlerMainClassLoader);
			Method mainMethod = mainClass.getDeclaredMethod("run", Class.class, String[].class);
			mainMethod.setAccessible(true);
			mainMethod.invoke(null, new Object[] { startClass, args });
		} else {
			DawdlerBootStarter.run(startClass, args);
		}
	}

	public static URL[] getURL() {
		List<URL> urls = new ArrayList<URL>(64);
		String javaHome = System.getProperty("java.home");
		Optional<Object> optionalClassPath = Optional.ofNullable(System.getProperties().get("java.class.path"));
		optionalClassPath.ifPresent(paths -> {
			for (String path : paths.toString().split(File.pathSeparator)) {
				try {
					if (javaHome != null) {
						if (!path.startsWith(javaHome)) {
							urls.add(new File(path).toURI().toURL());
						}
					} else {
						urls.add(new File(path).toURI().toURL());
					}
				} catch (MalformedURLException e) {
				}
			}
		});

		Optional<Object> optionalModulePath = Optional.ofNullable(System.getProperties().get("jdk.module.path"));
		optionalModulePath.ifPresent(paths -> {
			for (String path : paths.toString().split(File.pathSeparator)) {
				try {
					urls.add(new File(path).toURI().toURL());
				} catch (MalformedURLException e) {
				}
			}
		});

		return urls.toArray(new URL[0]);
	}
}

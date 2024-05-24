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
package com.anywide.dawdler.boot.maven;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author jackson.song
 * @version V1.0
 * 查找boot中的启动类
 */
public class BootStrapFinder {
	public static final String DAWDLER_SERVER_BOOT_APPLICATION_NAME = "com.anywide.dawdler.boot.server.annotation.DawdlerBootApplication";
	public static final String DAWDLER_WEB_BOOT_APPLICATION_NAME = "com.anywide.dawdler.boot.web.annotation.DawdlerBootApplication";

	public static String getBootStrapClass(File classesDirectory) throws IOException {
		File[] files = classesDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(".class");
			}
		});
		for (File file : files) {
			if (file.isDirectory()) {
				String bootStrapClass = getBootStrapClass(file);
				if (bootStrapClass != null) {
					return bootStrapClass;
				}
				continue;
			}
			InputStream input = new FileInputStream(file);
			try {
				ClassReader classReader = new ClassReader(input);
				ClassNode classNode = new ClassNode();
				classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
				if (classNode.visibleAnnotations != null) {
					for (AnnotationNode an : classNode.visibleAnnotations) {
						String annoName = an.desc.replaceAll("/", ".").substring(1, an.desc.length() - 1);
						if (annoName.equals(DAWDLER_SERVER_BOOT_APPLICATION_NAME)
								|| annoName.equals(DAWDLER_WEB_BOOT_APPLICATION_NAME)) {
							return classNode.name.replaceAll("/", ".");
						}
					}
				}
			} finally {
				if (input != null) {
					input.close();
				}
			}

		}

		return null;

	}

}

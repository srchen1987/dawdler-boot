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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * @author jackson.song
 * @version V1.0
 * repackage打包
 */
@Mojo(name = "repackage", requiresProject = true, requiresDependencyResolution = ResolutionScope.RUNTIME, defaultPhase = LifecyclePhase.PACKAGE)
public class RepackageMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.artifacts}", readonly = true, required = true)
	private Collection<Artifact> artifacts;

	@Parameter(defaultValue = "${project.dependencies}", readonly = true, required = true)
	private Collection<Dependency> dependencies;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
	private File outputDirectory;

	@Parameter(defaultValue = "${project.build.finalName}.jar", readonly = true, required = true)
	private String mainJarFilename;

	@Parameter(defaultValue = "${project.version}", required = true)
	private String implementationVersion;

	private static final String LOADER_VERSION = "0.0.5-jdk17-RELEASES";

	@Parameter(defaultValue = "${project.build.finalName}-all.jar", required = true)
	private String filename;

	@Parameter(defaultValue = "false")
	private boolean attachToBuild;

	@Parameter(defaultValue = "dawdler-boot")
	private String classifier;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
	private File classesDirectory;

	@Component
	private MavenProjectHelper projectHelper;

	private String mainClass;

	public void execute() throws MojoExecutionException {

		displayPluginInfo();

		JarOutputStream out = null;
		JarInputStream template = null;

		File dawdlerBootApplicationFile;
		try {
			getLog().info("outputDirectory:" + outputDirectory);
			getLog().info("filename:" + filename);
			dawdlerBootApplicationFile = new File(outputDirectory, filename);

			out = new JarOutputStream(new FileOutputStream(dawdlerBootApplicationFile, false), getManifest());

			if (getLog().isDebugEnabled()) {
				getLog().debug("Adding main jar main/[" + mainJarFilename + "]");
			}
			addToZip(new File(outputDirectory, mainJarFilename), "BOOT-INF/main/lib/", out);

			List<File> dependencyJars = extractDependencyFiles(artifacts);
			if (getLog().isDebugEnabled()) {
				getLog().debug("Adding [" + dependencyJars.size() + "] dependency libraries...");
			}
			for (File jar : dependencyJars) {
				addToZip(jar, "BOOT-INF/lib/", out);
			}

			List<File> systemDependencyJars = extractSystemDependencyFiles(dependencies);
			if (getLog().isDebugEnabled()) {
				getLog().debug("Adding [" + systemDependencyJars.size() + "] system dependency libraries...");
			}
			for (File jar : systemDependencyJars) {
				addToZip(jar, "BOOT-INF/lib/", out);
			}

			template = openDawdlerBootTemplateArchive();
			ZipEntry entry;
			while ((entry = template.getNextEntry()) != null) {
				if (!"boot-manifest.mf".equals(entry.getName())) {
					addToZip(out, entry, template);
				}
			}

		} catch (IOException e) {
			getLog().error(e);
			throw new MojoExecutionException("", e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(template);
		}

		if (attachToBuild) {
			projectHelper.attachArtifact(project, "jar", classifier, dawdlerBootApplicationFile);
		}
	}

	private void displayPluginInfo() {
		getLog().info("DawdlerBoot file: " + outputDirectory.getAbsolutePath() + File.separator + filename);
		getLog().info("classesDirectory " + classesDirectory);
	}

	private String getOnejarArchiveName() {
		return "dawdler-boot-classloader-" + LOADER_VERSION + ".jar";
	}

	private JarInputStream openDawdlerBootTemplateArchive() throws IOException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(getOnejarArchiveName());
		if (input == null) {
			throw new FileNotFoundException(getOnejarArchiveName());
		}
		return new JarInputStream(input);
	}

	private Manifest getManifest() throws IOException {
		ZipInputStream zipIS = openDawdlerBootTemplateArchive();
		Manifest manifest = null;
		if (zipIS != null) {
			mainClass = BootStrapFinder.getBootStrapClass(classesDirectory);
			if (mainClass == null) {
				throw new IOException(classesDirectory + " not found @"
						+ BootStrapFinder.DAWDLER_SERVER_BOOT_APPLICATION_NAME + " in one class!");
			}
			manifest = new Manifest();
			manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
			manifest.getMainAttributes().putValue("Start-Class", mainClass);
			manifest.getMainAttributes().putValue("Main-Class",
					"com.anywide.dawdler.fatjar.loader.launcher.JarLauncher");
			IOUtils.closeQuietly(zipIS);
			if (implementationVersion != null) {
				manifest.getMainAttributes().putValue("ImplementationVersion", implementationVersion);
			}
		}
		return manifest;
	}

	private void addToZip(File sourceFile, String zipfilePath, JarOutputStream out) throws IOException {
		ZipEntry zip = new ZipEntry(zipfilePath + sourceFile.getName());
		zip.setMethod(ZipEntry.STORED);
		CRC32 crc = new CRC32();
		zip.setSize(sourceFile.length());
		crc.reset();
		crc.update(Files.readAllBytes(sourceFile.toPath()));
		zip.setCrc(crc.getValue());
		zip.setTime(sourceFile.lastModified());
		addToZip(out, zip, new FileInputStream(sourceFile));
	}

	private final AtomicInteger alternativeEntryCounter = new AtomicInteger(0);

	private void addToZip(JarOutputStream out, ZipEntry entry, InputStream in) throws IOException {
		try {
			out.putNextEntry(entry);
			IOUtils.copy(in, out);
			out.closeEntry();
		} catch (ZipException e) {
			if (e.getMessage().startsWith("duplicate entry")) {
				final ZipEntry alternativeEntry = new ZipEntry(
						entry.getName() + "-DUPLICATE-FILENAME-" + alternativeEntryCounter.incrementAndGet() + ".jar");
				addToZip(out, alternativeEntry, in);
			} else {
				throw e;
			}
		}
	}

	private List<File> extractDependencyFiles(Collection<Artifact> artifacts) {
		List<File> files = new ArrayList<File>();

		if (artifacts == null) {
			return files;
		}

		for (Artifact artifact : artifacts) {
			File file = artifact.getFile();

			if (file.isFile()) {
				files.add(file);
			}

		}
		return files;
	}

	private List<File> extractSystemDependencyFiles(Collection<Dependency> systemDependencies) {
		final ArrayList<File> files = new ArrayList<File>();

		if (systemDependencies == null) {
			return files;
		}

		for (Dependency systemDependency : systemDependencies) {
			if (systemDependency != null && "system".equals(systemDependency.getScope())) {
				files.add(new File(systemDependency.getSystemPath()));
			}
		}
		return files;
	}

}

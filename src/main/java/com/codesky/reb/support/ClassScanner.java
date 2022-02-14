/*
 * Copyright 2002-2022 CODESKY.COM Team Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codesky.reb.support;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

public class ClassScanner {

	private final Logger logger = LoggerFactory.getLogger(ClassScanner.class);

	private String pkgPrefix;

	public ClassScanner(String pkgPrefix) {
		this.pkgPrefix = pkgPrefix;
	}

	public Collection<Class<?>> scan() {
		return scan(null, null);
	}
	
	public Collection<Class<?>> scan(Class<?> classType) {
		return scan(classType, null);
	}
	
	public Collection<Class<?>> scan(Class<?> classType, Class<? extends Annotation> annotationType) {
		Collection<Class<?>> classes = null;
		Collection<String> classNames = new ArrayList<String>(256);

		try {
			classNames.addAll(getClassFolderClassNames());
			classNames.addAll(getJarsClassNames());
			if (!classNames.isEmpty()) {
				classes = new ArrayList<Class<?>>(classNames.size());
				for (String className : classNames) {
					Class<?> clazz = Class.forName(className);
					if (classType != null && !classType.isAssignableFrom(clazz))
						continue;
					if (annotationType != null && !clazz.isAnnotationPresent(annotationType))
						continue;
					classes.add(clazz);
				}
			}
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return classes;
	}

	private Collection<String> getClassFolderClassNames() throws IOException {
		Collection<String> classNames = new ArrayList<String>();
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);
		Resource[] resources = resolver.getResources("/**/*.class");

		for (Resource res : resources) {
			MetadataReader reader = readerFactory.getMetadataReader(res);
			String className = reader.getClassMetadata().getClassName();
			if (className.startsWith(pkgPrefix) && className.indexOf("$") == -1) {
				classNames.add(className);
			}
		}

		return classNames;
	}

	private Collection<String> getJarsClassNames() throws IOException {
		Collection<String> classNames = new ArrayList<String>();
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath*:");

		for (Resource res : resources) {
			URL url = res.getURL();
			if (ResourceUtils.isJarURL(url)) {
				String jarPath = URLDecoder.decode(url.getPath(), "UTF-8");
				jarPath = jarPath.startsWith("file:") ? jarPath.substring("file:".length(), jarPath.lastIndexOf('!'))
						: jarPath.substring(0, jarPath.lastIndexOf('!'));

				try (JarFile jar = new JarFile(jarPath)) {
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String resourcePath = entry.getName();
						if (resourcePath.endsWith(".class") && resourcePath.indexOf("$") == -1) {
							String className = ClassUtils.convertResourcePathToClassName(
									resourcePath.substring(0, resourcePath.indexOf(".class")));
							if (className.startsWith(pkgPrefix)) {
								classNames.add(className);
							}
						}
					}
				}
			}
		}

		return classNames;
	}

}

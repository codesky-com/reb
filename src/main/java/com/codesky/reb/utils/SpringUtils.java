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
 * 
 * Github:
 *	https://github.com/codesky-com/reb.git
 */

package com.codesky.reb.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class SpringUtils implements ApplicationContextAware {
	
	private static ApplicationContext appContext = null;
	
	private final Logger logger = LoggerFactory.getLogger(SpringUtils.class);
	
	public final static <T> T getBean(Class<T> requireType) {
		Assert.notNull(appContext, "");
		return appContext.getBean(requireType);
	}
	
	@SuppressWarnings("unchecked")
	public final static <T> T getBean(String name) {
		Assert.notNull(appContext, "");
		return (T) appContext.getBean(name);
	}
	
	public final static <T> Map<String, T> getBeansOfType(Class<T> type) {
		Assert.notNull(appContext, "");
		return appContext.getBeansOfType(type);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext = applicationContext;
		logger.info("Application context has been overrided.");
	}
}

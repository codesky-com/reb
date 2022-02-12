package com.codesky.reb.utils;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtils implements ApplicationContextAware {
	
	private static ApplicationContext appContext = null;
	
	private final Logger logger = LoggerFactory.getLogger(SpringUtils.class);
	
	public final static <T> T getBean(Class<T> requireType) {
		assertNotNull(appContext);
		return appContext.getBean(requireType);
	}
	
	@SuppressWarnings("unchecked")
	public final static <T> T getBean(String name) {
		assertNotNull(appContext);
		return (T) appContext.getBean(name);
	}
	
	public final static <T> Map<String, T> getBeansOfType(Class<T> type) {
		assertNotNull(appContext);
		return appContext.getBeansOfType(type);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext = applicationContext;
		logger.info("Application context has been overrided.");
	}
}

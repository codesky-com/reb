package com.codesky.reb.web;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.codesky.reb.utils.SpringUtils;

@Component
public class CmdManager implements InitializingBean {
	
	private final Logger logger = LoggerFactory.getLogger(CmdManager.class);
	
	private final Map<Long, Method> cmdMappings = new ConcurrentHashMap<Long, Method>();
	
	protected void registerCommands() {
		Map<String, Object> beansMap = SpringUtils.getBeansWithAnnotation(Controller.class);
		beansMap.forEach((k, v) -> {
			Method[] methods = v.getClass().getDeclaredMethods();
			for (Method m : methods) {
				CmdMapping value = m.getAnnotation(CmdMapping.class);
				if (value != null) {
					cmdMappings.putIfAbsent(value.cmd(), m);
				}
			}
		});
	}
	
	public Object execute(long cmd, Object args) {
		Method method = cmdMappings.get(cmd);
		if (method != null) {
			Object target = SpringUtils.getBean(method.getDeclaringClass());
			try {
				return method.invoke(target, args);
			} catch (Throwable ex) {
				if (logger.isErrorEnabled()) {
					logger.error(null, ex);
				}
			}
		}
		return null;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		registerCommands();
	}

}

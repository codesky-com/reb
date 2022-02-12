package com.codesky.reb;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.codesky.reb.interfaces.Service;
import com.codesky.reb.utils.SpringUtils;

@Component
public class Context {

	private final Logger logger = LoggerFactory.getLogger(Context.class);
	
	private final AtomicBoolean inited = new AtomicBoolean(false);
	
	private final ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<String, Service>();

	private void registerServices() {
		Map<String, Service> services = SpringUtils.getBeansOfType(Service.class);
		for (Map.Entry<String, Service> entry : services.entrySet()) {
			this.services.putIfAbsent(entry.getValue().getClass().getName(), entry.getValue());
			logger.info(String.format("[Service] %s has been registered.", entry.getKey()));
		}
	}
	
	public void init() {
		if (inited.compareAndSet(false, true)) {
			registerServices();
		}
	}
	
	public Collection<Service> getServices() {
		return services.values();
	}
	
	public Service getService(String name) {
		return services.get(name);
	}
	
	public void reset() {
		services.clear();
	}
}

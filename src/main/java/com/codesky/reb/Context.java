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

package com.codesky.reb;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

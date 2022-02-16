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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesky.reb.message.MessageHandler;
import com.codesky.reb.message.ProtosProperties;
import com.codesky.reb.message.ProtosProperties.ProtoDescriptor;
import com.codesky.reb.utils.SpringUtils;

@Component
public class Context {

	private final Logger logger = LoggerFactory.getLogger(Context.class);

	@Autowired
	private ProtosProperties protosProperties;

	private final AtomicBoolean inited = new AtomicBoolean(false);

	private final ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<String, Service>();
	private final ConcurrentHashMap<Long, MessageDescriptor> messages = new ConcurrentHashMap<Long, MessageDescriptor>();

	private void registerServices() {
		Map<String, Service> services = SpringUtils.getBeansOfType(Service.class);
		for (Map.Entry<String, Service> entry : services.entrySet()) {
			this.services.putIfAbsent(entry.getValue().getClass().getName(), entry.getValue());
			logger.info(String.format("[Service] %s has been registered.", entry.getKey()));
		}
	}

	private void registerMessages() {
		for (ProtoDescriptor descriptor : protosProperties.getDescriptors()) {
			List<Class<? extends MessageHandler>> handlerClasses = new ArrayList<Class<? extends MessageHandler>>();
			Collection<String> handlers = Arrays.asList(descriptor.getHandlers().split(","));
			handlers.forEach((s) -> {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends MessageHandler> clazz = (Class<? extends MessageHandler>) Class.forName(s);
					handlerClasses.add(clazz);
				} catch (ClassNotFoundException e) {
					logger.error(e.getMessage());
				}
			});
			messages.putIfAbsent(descriptor.getCmd(), new MessageDescriptor(descriptor.getFullName(), handlerClasses));
		}
	}

	public void init() {
		if (inited.compareAndSet(false, true)) {
			registerServices();
			registerMessages();
		}
	}

	public Collection<Service> getServices() {
		return services.values();
	}

	public Service getService(String name) {
		return services.get(name);
	}
	
	public String getMessagePackageNameByCmd(long cmd) {
		if (!messages.containsKey(cmd))
			return null;
		
		MessageDescriptor descriptor = messages.get(cmd);
		return descriptor.getFullName();
	}
	
	public Collection<Class<? extends MessageHandler>> getMessageHandlersByCmd(long cmd) {
		if (!messages.containsKey(cmd))
			return null;
		
		MessageDescriptor descriptor = messages.get(cmd);
		return Collections.unmodifiableCollection(descriptor.getHandlers());
	}

	public void reset() {
		services.clear();
		messages.clear();
	}

	private final static class MessageDescriptor {
		private String fullName;
		private List<Class<? extends MessageHandler>> handlers;

		public MessageDescriptor(String fullName, List<Class<? extends MessageHandler>> handlers) {
			this.fullName = fullName;
			this.handlers = handlers;
		}

		public String getFullName() {
			return fullName;
		}

		public List<Class<? extends MessageHandler>> getHandlers() {
			return handlers;
		}
	}
}

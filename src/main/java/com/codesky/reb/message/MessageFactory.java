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

package com.codesky.reb.message;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.codesky.reb.message.ProtosProperties.ProtoDescriptor;
import com.codesky.reb.support.ClassScanner;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

@Component
public class MessageFactory implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(MessageFactory.class);

	private final ConcurrentHashMap<String, Method> protoParsers = new ConcurrentHashMap<String, Method>(256);
	private final ConcurrentHashMap<Long, MessageDescriptor> messages = new ConcurrentHashMap<Long, MessageDescriptor>();
	
	@Autowired
	private ProtosProperties protosProperties;
	
	@Value("${reb.proto_base_packages}")
	private String protoBasePackages;

	@Override
	public void afterPropertiesSet() throws Exception {
		initFactory();
		initMessages();
	}

	private void initFactory() {
		if (StringUtils.isNotBlank(protoBasePackages)) {
			Collection<String> packages = Arrays.asList(protoBasePackages.split(","));
			packages.forEach((s) -> {
				registerProtocols(s);
			});
		}
	}

	private void initMessages() {
		for (ProtoDescriptor descriptor : protosProperties.getDescriptors()) {
			Method handler = null;
			if (StringUtils.isNotBlank(descriptor.getHandler())) {
				try {
					String handlerFullName = descriptor.getHandler();
					int index = handlerFullName.lastIndexOf(".");
					if (index == -1)
						continue;
					
					String className = handlerFullName.substring(0, index);
					String methodName = handlerFullName.substring(index + 1);
					
					Class<?> clazz = Class.forName(className);
					for (Method m : clazz.getDeclaredMethods()) {
						if (StringUtils.equals(m.getName(), methodName)) {
							handler = m;
							break;
						}
					}
				} catch (Throwable ex) {
					logger.error(ExceptionUtils.getStackTrace(ex));
				}
			}
			
			messages.putIfAbsent(descriptor.getCmd(), new MessageDescriptor(descriptor.getFullName(), handler));
		}
	}

	private void registerProtocols(String protoBasePackage) {
		ClassScanner scanner = new ClassScanner(protoBasePackage);
		Collection<Class<?>> classes = scanner.scan(Message.class);
		if (classes != null) {
			for (Class<?> clazz : classes) {
				Method method = ReflectionUtils.findMethod(clazz, "getDescriptor");
				if (method == null)
					continue;

				Descriptor descriptor = (Descriptor) ReflectionUtils.invokeMethod(method, null);
				if (descriptor == null)
					continue;

				method = ReflectionUtils.findMethod(clazz, "parseFrom", byte[].class);
				if (method == null)
					continue;

				protoParsers.putIfAbsent(descriptor.getFullName(), method);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T newMessage(String protoPkgName, byte[] data) {
		if (protoPkgName == null)
			return null;
		
		Method method = protoParsers.get(protoPkgName);
		if (method == null)
			return null;
		
		return (T) ReflectionUtils.invokeMethod(method, null, data);
	}

	public <T extends Message> T newMessage(long cmd, byte[] data) {
		String packageName = getMessagePackageNameByCmd(cmd);
		return newMessage(packageName, data);
	}
	
	public String getMessagePackageNameByCmd(long cmd) {
		if (!messages.containsKey(cmd))
			return null;

		MessageDescriptor descriptor = messages.get(cmd);
		return descriptor.getFullName();
	}
	
	public long findMessageCmdByPackageName(String packageName) {
		for (Map.Entry<Long, MessageDescriptor> entry : messages.entrySet()) {
			if (StringUtils.equals(packageName, entry.getValue().getFullName()))
				return entry.getKey();
		}
		return -1;
	}

	public Method getMessageHandlerByCmd(long cmd) {
		MessageDescriptor descriptor = messages.get(cmd);
		return (descriptor != null) ? descriptor.getHandler() : null;
	}

	private final static class MessageDescriptor {
		private final String fullName;
		private final Method handler;
		
		public MessageDescriptor(String fullName, Method handler) {
			this.fullName = fullName;
			this.handler = handler;
		}

		public String getFullName() {
			return fullName;
		}

		public Method getHandler() {
			return handler;
		}
	}
}

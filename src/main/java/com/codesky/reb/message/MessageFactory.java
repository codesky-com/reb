package com.codesky.reb.message;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
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

	private final ConcurrentHashMap<String, ProtoType> protoTypes = new ConcurrentHashMap<String, ProtoType>(128);
	private final ConcurrentHashMap<Long, MessageDescriptor> messages = new ConcurrentHashMap<Long, MessageDescriptor>();

	@Value("${reb.proto_base_packages}")
	private String protoBasePackages;
	
	@Autowired
	private ProtosProperties protosProperties;

	@Override
	public void afterPropertiesSet() throws Exception {
		initFactory();
		initMessages();
	}

	private void initFactory() {
		if (!StringUtils.isBlank(protoBasePackages)) {
			Collection<String> packages = Arrays.asList(protoBasePackages.split(","));
			packages.forEach((s) -> {
				registerProtoTypes(s);
			});
		}
	}

	private void initMessages() {
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

	private void registerProtoTypes(String protoBasePackage) {
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

				protoTypes.putIfAbsent(descriptor.getFullName(), new ProtoType(clazz, method));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T newMessage(String protoPkgName, byte[] data) {
		if (!protoTypes.containsKey(protoPkgName))
			return null;

		Method method = protoTypes.get(protoPkgName).getMethod();
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

	public Collection<Class<? extends MessageHandler>> getMessageHandlersByCmd(long cmd) {
		if (!messages.containsKey(cmd))
			return null;

		MessageDescriptor descriptor = messages.get(cmd);
		return Collections.unmodifiableCollection(descriptor.getHandlers());
	}

	private final static class ProtoType {
		private final Class<?> clazz;
		private final Method method;

		public ProtoType(Class<?> clazz, Method method) {
			this.clazz = clazz;
			this.method = method;
		}

		@SuppressWarnings("unused")
		public Class<?> getClazz() {
			return clazz;
		}

		public Method getMethod() {
			return method;
		}
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

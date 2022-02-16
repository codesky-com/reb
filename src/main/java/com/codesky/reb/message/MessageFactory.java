package com.codesky.reb.message;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.codesky.reb.Context;
import com.codesky.reb.support.ClassScanner;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

@Component
public class MessageFactory implements InitializingBean {

	private final ConcurrentHashMap<String, ProtoType> protoTypes = new ConcurrentHashMap<String, ProtoType>(128);

	@Autowired
	private Context context;

	@Value("${reb.proto_base_packages}")
	private String protoBasePackages;

	@Override
	public void afterPropertiesSet() throws Exception {
		initFactory();
	}

	private void initFactory() {
		if (!StringUtils.isBlank(protoBasePackages)) {
			Collection<String> packages = Arrays.asList(protoBasePackages.split(","));
			packages.forEach((s) -> {
				registerProtoTypes(s);
			});
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
		String packageName = context.getMessagePackageNameByCmd(cmd);
		return newMessage(packageName, data);
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
}

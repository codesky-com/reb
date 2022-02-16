package com.codesky.reb.message;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.codesky.reb.support.YamlPropertySourceFactory;

@Component
@PropertySource(value = { "file:./config/protos.yml" }, factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "protos")
public class ProtosProperties {

	private List<ProtoDescriptor> descriptors;

	public List<ProtoDescriptor> getDescriptors() {
		return descriptors;
	}

	public void setDescriptors(List<ProtoDescriptor> descriptors) {
		this.descriptors = descriptors;
	}

	public final static class ProtoDescriptor {
		private long cmd;
		private String fullName;
		private String handlers;

		public long getCmd() {
			return cmd;
		}

		public void setCmd(long cmd) {
			this.cmd = cmd;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getHandlers() {
			return handlers;
		}

		public void setHandlers(String handlers) {
			this.handlers = handlers;
		}
	}

}

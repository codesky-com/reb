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
		private String handler;

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

		public String getHandler() {
			return handler;
		}

		public void setHandler(String handler) {
			this.handler = handler;
		}
	}

}

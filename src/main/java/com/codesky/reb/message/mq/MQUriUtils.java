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

package com.codesky.reb.message.mq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQUriUtils {

	private final static Logger LOGGER = LoggerFactory.getLogger(MQUriUtils.class);

	public final static MQUriProperties parse(String uri) {
		URI u = null;
		try {
			u = new URI(uri);
		} catch (URISyntaxException e) {
			LOGGER.error("URI={}", uri);
			return null;
		}

		Collection<String> paramSplits = Arrays.asList(u.getQuery().split("&"));
		Map<String, String> parameters = new HashMap<String, String>(paramSplits.size());
		paramSplits.forEach((s) -> {
			String[] kv = s.split("=");
			parameters.put(kv[0].trim(), kv[1].trim());
		});

		return new MQUriProperties(u.getScheme(), u.getHost(), u.getPort(), parameters);
	}

	public final static class MQUriProperties {
		private final String scheme;
		private final String host;
		private final int port;
		private final String nameServer;
		private final Map<String, String> parameters;

		public MQUriProperties(String scheme, String host, int port, Map<String, String> parameters) {
			this.scheme = scheme;
			this.host = host;
			this.port = port;
			this.nameServer = String.format("%s:%d", host, port);
			this.parameters = parameters;
		}

		public String getScheme() {
			return scheme;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public String getNameServer() {
			return nameServer;
		}

		public Map<String, String> getParameters() {
			return parameters;
		}

		public boolean hasProperty(String name) {
			return parameters.containsKey(name);
		}

		public String getProperty(String name) {
			return parameters.get(name);
		}
	}

}

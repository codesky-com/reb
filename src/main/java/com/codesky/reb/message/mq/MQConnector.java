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

package com.codesky.reb.message.mq;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class MQConnector implements MQMessageListener {

	private final Logger logger = LoggerFactory.getLogger(MQConnector.class);

	@Value("${reb.mq.consumer.provider}")
	private String consumerProvider;

	@Value("${reb.mq.consumer.uri}")
	private String consumerUri;

	private MQConsumer consumer;
	
	private final AtomicLong lastRecvTime = new AtomicLong(0);
	
	public final long getLastRecvTime() {
		return lastRecvTime.get();
	}

	private MQConsumer newMqConsumer() {
		Assert.notNull(consumerProvider, "");
		Assert.notNull(consumerUri, "");

		try {
			Class<?> providerClazz = Class.forName(consumerProvider);
			if (MQConsumer.class.isAssignableFrom(providerClazz)) {
				MQConsumer instance = (MQConsumer) providerClazz.getDeclaredConstructor().newInstance();
				if (instance.setup(consumerUri, this))
					return instance;
			}
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public void connect() {
		if (consumer == null) {
			consumer = newMqConsumer();
		}

		Assert.notNull(consumer, "");

		consumer.start();
		
		lastRecvTime.set(System.currentTimeMillis());
	}
	
	public void close() {
		if (consumer != null) {
			consumer.shutdown();
		}
		
		consumer = null;
	}
	
	public void reconnect() {
		close();
		connect();
	}

	@Override
	public boolean receive(MQMessage msg) {
		try {
			lastRecvTime.set(System.currentTimeMillis());
			
			String txt = new String(msg.getBody(), "UTF-8");
			System.out.println(txt);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}

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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.codesky.reb.message.MessageCallback;
import com.codesky.reb.message.MessageDecoder;
import com.codesky.reb.message.MessageFactory;
import com.codesky.reb.message.struct.DataPacket;
import com.codesky.reb.message.struct.DataStructOuterClass.DataStruct;
import com.google.protobuf.Message;

@Component
public class MQConnector implements MQMessageListener, InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(MQConnector.class);

	@Value("${reb.mq.consumer.provider}")
	private String consumerProvider;

	@Value("${reb.mq.consumer.uri}")
	private String consumerUri;

	@Autowired
	private MessageFactory messageFactory;

	private MessageCallback messageCallback;

	private MessageDecoder decoder;

	private MQConsumer consumer;

	private final AtomicLong lastRecvTime = new AtomicLong(0);

	@Override
	public void afterPropertiesSet() throws Exception {
		decoder = new MessageDecoder(messageFactory);
	}

	public MessageCallback getMessageCallback() {
		return messageCallback;
	}

	public void setMessageCallback(MessageCallback messageCallback) {
		this.messageCallback = messageCallback;
	}

	public final long getLastRecvTime() {
		return lastRecvTime.get();
	}

	private MQConsumer newConsumer() {
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
			consumer = newConsumer();
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
	public boolean receive(MQMessage mqMsg) {
		try {
			lastRecvTime.set(System.currentTimeMillis());
			if (messageCallback == null)
				return true;

			DataStruct ds = DataStruct.parseFrom(mqMsg.getBody());
			DataPacket packet = new DataPacket(ds);

			Message msg = decoder.decode(packet);
			if (msg == null) {
				logger.error("Unknown message cmd={}", Long.toHexString(packet.getCmd()));
				return false;
			}

			return messageCallback.onMessage(packet.getCmd(), msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.codesky.reb.message.MessageCallback;
import com.codesky.reb.message.struct.DataPacket;
import com.codesky.reb.message.struct.DataStructOuterClass.DataStruct;

@Component
public class MQConnector implements MQMessageListener {

	private final Logger logger = LoggerFactory.getLogger(MQConnector.class);

	@Value("${reb.mq.consumer.provider}")
	private String consumerProvider;

	@Value("${reb.mq.consumer.uri}")
	private String consumerUri;

	@Value("${reb.mq.producer.provider}")
	private String producerProvider;

	@Value("${reb.mq.producer.uri}")
	private String producerUri;

	private MessageCallback messageCallback;

	private MQConsumer consumer;

	private MQProducer producer;

	private final AtomicLong lastRecvTime = new AtomicLong(0);
	private final AtomicLong lastSendTime = new AtomicLong(0);
	private final AtomicLong recvMsgCounter = new AtomicLong(0);
	private final AtomicLong sendMsgCounter = new AtomicLong(0);

	public MessageCallback getMessageCallback() {
		return messageCallback;
	}

	public void setMessageCallback(MessageCallback messageCallback) {
		this.messageCallback = messageCallback;
	}

	public final long getLastRecvTime() {
		return lastRecvTime.get();
	}

	public final long getLastSendTime() {
		return lastSendTime.get();
	}

	public final long getRecvMsgCount() {
		return recvMsgCounter.get();
	}

	public final long getSendMsgCount() {
		return sendMsgCounter.get();
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

	private MQProducer newProducer() {
		Assert.notNull(producerProvider, "");
		Assert.notNull(producerUri, "");

		try {
			Class<?> providerClazz = Class.forName(producerProvider);
			if (MQProducer.class.isAssignableFrom(providerClazz)) {
				MQProducer instance = (MQProducer) providerClazz.getDeclaredConstructor().newInstance();
				if (instance.setup(producerUri))
					return instance;
			}
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

	public boolean send(byte[] body, String topic, String tags) {
		Assert.notNull(producer, "");
		if (producer.send(body, topic, tags)) {
			lastSendTime.set(System.currentTimeMillis());
			sendMsgCounter.incrementAndGet();
			return true;
		}
		return false;
	}

	public void connect() {
		if (consumer == null) {
			consumer = newConsumer();
		}

		if (producer == null) {
			producer = newProducer();
		}

		Assert.notNull(consumer, "");
		Assert.notNull(producer, "");

		consumer.start();
		producer.start();

		lastRecvTime.set(System.currentTimeMillis());
		lastSendTime.set(System.currentTimeMillis());
	}

	public void close() {
		if (consumer != null) {
			consumer.shutdown();
		}

		if (producer != null) {
			producer.shutdown();
		}

		consumer = null;
		producer = null;
	}

	public void reconnect() {
		close();
		connect();
	}

	@Override
	public boolean receive(Collection<MQMessage> msgs) {
		try {
			lastRecvTime.set(System.currentTimeMillis());
			recvMsgCounter.addAndGet(msgs.size());
			if (messageCallback == null)
				return true;

			Collection<DataPacket> packets = new ArrayList<DataPacket>(msgs.size());
			for (MQMessage msg : msgs) {
				DataStruct ds = DataStruct.parseFrom(msg.getBody());
				DataPacket packet = new DataPacket(ds);
				packets.add(packet);
			}

			return messageCallback.onMessage(packets);

		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}
}

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

package com.codesky.reb.mq.impl;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codesky.reb.mq.MQConsumer;

public class RocketMQConsumer implements MQConsumer {

	private final Logger logger = LoggerFactory.getLogger(RocketMQConsumer.class);

	private DefaultMQPushConsumer consumer;

	public RocketMQConsumer(String nameServer, String group) {
		consumer = new DefaultMQPushConsumer(group);
		consumer.setNamesrvAddr(nameServer);
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.registerMessageListener(new MessageListenerOrderly() {
			@Override
			public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
				return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
			}
		});
	}

	@Override
	public void start() {
		assertNotNull(consumer);
		try {
			consumer.start();
		} catch (MQClientException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	@Override
	public void stop() {
		assertNotNull(consumer);
		consumer.shutdown();
	}

	@Override
	public void subscribe(String topic, String expr) {
		try {
			consumer.subscribe(topic, expr);
		} catch (MQClientException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}
}

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

package com.codesky.reb.message.mq.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.codesky.reb.message.mq.MQConsumer;
import com.codesky.reb.message.mq.MQMessage;
import com.codesky.reb.message.mq.MQMessageListener;
import com.codesky.reb.message.mq.MQUriUtils;
import com.codesky.reb.message.mq.MQUriUtils.MQUriProperties;

public class RocketMQConsumer implements MQConsumer {

	private final Logger logger = LoggerFactory.getLogger(RocketMQConsumer.class);

	private DefaultMQPushConsumer consumer;

	@Override
	public boolean setup(String uri, MQMessageListener listener) {
		MQUriProperties uriProperties = MQUriUtils.parse(uri);
		if (uriProperties == null) {
			logger.error("Invalid URI={}", uri);
			return false;
		}

		String[] checkProperties = { "group_id", "topicTags" };
		for (String item : checkProperties) {
			if (!uriProperties.hasProperty(item)) {
				logger.error("MQ URI lost '{}' property.", item);
				return false;
			}
		}

		Collection<String> topicTagSplits = Arrays.asList(uriProperties.getProperty("topicTags").split(","));
		Map<String, String> topicTags = new HashMap<String, String>(topicTagSplits.size());
		topicTagSplits.forEach((s) -> {
			String[] kv = s.split(":");
			topicTags.put(kv[0].trim(), kv[1].trim());
		});

		boolean isOrderly = uriProperties.hasProperty("orderly")
				? BooleanUtils.toBoolean(uriProperties.getProperty("orderly"))
				: true;

		int batchMaxSize = uriProperties.hasProperty("batchMaxSize")
				? Integer.valueOf(uriProperties.getProperty("batchMaxSize"))
				: 1;

		consumer = new DefaultMQPushConsumer(uriProperties.getProperty("group_id"));
		consumer.setNamesrvAddr(uriProperties.getNameServer());
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.setConsumeMessageBatchMaxSize(batchMaxSize);

		if (isOrderly) {
			consumer.registerMessageListener(new MessageListenerOrderly() {
				@Override
				public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
					try {
						Collection<MQMessage> mqMessages = new ArrayList<MQMessage>(msgs.size());
						msgs.forEach((item) -> {
							MQMessage m = new MQMessage(item.getMsgId(), item.getBornTimestamp(), item.getQueueOffset(),
									item.getReconsumeTimes(), item.getBody());
							mqMessages.add(m);
						});

						if (listener.receive(mqMessages))
							return ConsumeOrderlyStatus.SUCCESS;

					} catch (Throwable e) {
						logger.error(ExceptionUtils.getStackTrace(e));
					}
					return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
				}
			});
		} else {
			consumer.registerMessageListener(new MessageListenerConcurrently() {
				@Override
				public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
						ConsumeConcurrentlyContext context) {
					try {
						Collection<MQMessage> mqMessages = new ArrayList<MQMessage>(msgs.size());
						msgs.forEach((item) -> {
							MQMessage m = new MQMessage(item.getMsgId(), item.getBornTimestamp(), item.getQueueOffset(),
									item.getReconsumeTimes(), item.getBody());
							mqMessages.add(m);
						});

						if (listener.receive(mqMessages))
							return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

					} catch (Throwable e) {
						logger.error(ExceptionUtils.getStackTrace(e));
					}
					return ConsumeConcurrentlyStatus.RECONSUME_LATER;
				}
			});
		}

		topicTags.forEach((k, v) -> {
			subscribe(k, v);
		});
		return true;
	}

	@Override
	public void start() {
		Assert.notNull(consumer, "");
		try {
			consumer.start();
		} catch (MQClientException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	@Override
	public void shutdown() {
		Assert.notNull(consumer, "");
		consumer.shutdown();
	}

	@Override
	public void subscribe(String topic, String expr) {
		Assert.notNull(consumer, "");
		try {
			consumer.subscribe(topic, expr);
		} catch (MQClientException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}
}

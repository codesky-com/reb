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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.util.Assert;

import com.codesky.reb.mq.MQConsumer;
import com.codesky.reb.mq.MQMessageListener;

public class RocketMQConsumer implements MQConsumer {

	private final Logger logger = LoggerFactory.getLogger(RocketMQConsumer.class);

	private DefaultMQPushConsumer consumer;
	
	@Override
	public boolean setup(String uri, MQMessageListener listener) {
		URI u = null;
		try {
			u = new URI(uri);
		} catch (URISyntaxException e) {
			logger.error("URI={}", uri);
			return false;
		}
		
		if (!StringUtils.equals(u.getScheme().toLowerCase(), "rocketmq")) {
			logger.error("'{}' scheme is not rocketmq.", u.getScheme());
			return false;
		}

		Collection<String> paramSplits = Arrays.asList(u.getQuery().split("&"));
		Map<String, String> parameters = new HashMap<String, String>(paramSplits.size());
		paramSplits.forEach((s) -> {
			String[] kv = s.split("=");
			parameters.put(kv[0].trim(), kv[1].trim());
		});
		
		String[] checkExistParams = {"group_id", "topicTags"};
		for (String item : checkExistParams) {
			if (!parameters.containsKey(item)) {
				logger.error("lost '{}' parameter.", item);
				return false;
			}
		}
		
		Collection<String> topicTagSplits = Arrays.asList(parameters.get("topicTags").split(","));
		Map<String, String> topicTags = new HashMap<String, String>(topicTagSplits.size());
		topicTagSplits.forEach((s) -> {
			String[] kv = s.split(":");
			topicTags.put(kv[0].trim(), kv[1].trim());
		});

		boolean isOrderly = parameters.containsKey("orderly") ? BooleanUtils.toBoolean(parameters.get("orderly")) : true;

		consumer = new DefaultMQPushConsumer(parameters.get("group_id"));
		consumer.setNamesrvAddr(String.format("%s:%d", u.getHost(), u.getPort()));
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.setConsumeMessageBatchMaxSize(1);
		
		if (isOrderly) {
			consumer.registerMessageListener(new MessageListenerOrderly() {
				@Override
				public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
					try {
						Assert.isTrue(msgs.size() == 1, "");
						MessageExt msg = msgs.get(0);
						if (listener.receive(msg.getBody())) {
							return ConsumeOrderlyStatus.SUCCESS;
						}
					} catch (Throwable e) {
						logger.error(ExceptionUtils.getStackTrace(e));
					}
					return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
				}
			});
		}
		
		topicTags.forEach((k, v) -> { subscribe(k, v); });
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
	public void stop() {
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

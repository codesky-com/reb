package com.codesky.reb.message.mq.impl;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.codesky.reb.message.mq.MQProducer;
import com.codesky.reb.message.mq.MQUriUtils;
import com.codesky.reb.message.mq.MQUriUtils.MQUriProperties;

public class RocketMQProducer implements MQProducer {

	private final Logger logger = LoggerFactory.getLogger(RocketMQProducer.class);

	private DefaultMQProducer producer;

	@Override
	public boolean setup(String uri) {
		MQUriProperties uriProperties = MQUriUtils.parse(uri);
		if (uriProperties == null) {
			logger.error("Invalid URI={}", uri);
			return false;
		}

		String[] checkProperties = { "group_id" };
		for (String item : checkProperties) {
			if (!uriProperties.hasProperty(item)) {
				logger.error("MQ URI lost '{}' property.", item);
				return false;
			}
		}

		producer = new DefaultMQProducer(uriProperties.getProperty("group_id"));
		producer.setNamesrvAddr(uriProperties.getNameServer());
		return true;
	}

	@Override
	public void start() {
		Assert.notNull(producer, "");
		try {
			producer.start();
		} catch (MQClientException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	@Override
	public void shutdown() {
		Assert.notNull(producer, "");
		producer.shutdown();
	}

	@Override
	public boolean send(byte[] body, String topic, String tags) {
		Message mqMsg = new Message(topic, tags, body);
		try {
			SendResult result = producer.send(mqMsg);
			return (result.getSendStatus() == SendStatus.SEND_OK);
		} catch (Throwable e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}

}

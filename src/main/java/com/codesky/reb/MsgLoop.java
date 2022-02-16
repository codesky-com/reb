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

package com.codesky.reb;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesky.reb.message.MessageCallback;
import com.codesky.reb.message.MessageHandler;
import com.codesky.reb.message.mq.MQConnector;
import com.google.protobuf.Message;

@Component
public class MsgLoop extends Thread implements MessageCallback {

	private final Logger logger = LoggerFactory.getLogger(MsgLoop.class);

	private final AtomicBoolean running = new AtomicBoolean(true);

	private final ConcurrentLinkedQueue<Pair<Long, Message>> messageQueue = new ConcurrentLinkedQueue<Pair<Long, Message>>();
	
	@Autowired
	private Context context;

	@Autowired
	private MQConnector connector;

	public MsgLoop() {
		super("MsgLoopThread");
	}

	public void shutdown() {
		running.set(false);
	}

	@Override
	public boolean onMessage(long cmd, Message msg) {
		if (msg == null)
			return false;

		messageQueue.add(new ImmutablePair<Long, Message>(cmd, msg));
		return true;
	}

	@Override
	public void run() {
		logger.info("MainLoopThread running.");
		connector.setMessageCallback(this);
		connector.connect();

		while (running.get()) {
			try {
				Pair<Long, Message> pair = messageQueue.poll();
				if (pair != null) {
					Collection<Class<? extends MessageHandler>> handlerClasses = context.getMessageHandlersByCmd(pair.getKey());
					if (handlerClasses != null) {
						for (Class<? extends MessageHandler> clazz : handlerClasses) {
							MessageHandler handler = clazz.getDeclaredConstructor().newInstance();
							handler.execute(pair.getValue());
						}
					}
				}
				Thread.sleep(3);
			} catch (Throwable e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
		}

		connector.close();
		logger.info("MainLoopThread exit.");
	}

}

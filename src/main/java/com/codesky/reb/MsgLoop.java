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

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesky.reb.mq.MQConnector;

@Component
public class MsgLoop extends Thread {
	
	private final AtomicBoolean running = new AtomicBoolean(true);
	
	private final Logger logger = LoggerFactory.getLogger(MsgLoop.class);
	
	@Autowired
	private MQConnector connector;

	public MsgLoop() {
		super("MsgLoopThread");
	}
	
	public void shutdown() {
		running.set(false);
	}
	
	@Override
	public void run() {
		logger.info("MainLoopThread running.");
		
		connector.connect();
		
		while (running.get()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("MsgLoop");
		}
		
		logger.info("MainLoopThread exit.");
	}
	
}

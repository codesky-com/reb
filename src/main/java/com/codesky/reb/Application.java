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

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;

import com.google.protobuf.Message;

@EnableAutoConfiguration
@ComponentScan(basePackages = { "${reb.scan_base_packages}" })
public class Application implements CommandLineRunner, ApplicationListener<ApplicationEvent> {

	private final Logger logger = LoggerFactory.getLogger(Application.class);

	private final Semaphore shutdownSignal = new Semaphore(1);

	private final AtomicBoolean running = new AtomicBoolean(false);
	
	@Autowired
	private MainLoop mainLoop;

	@Autowired
	private MsgLoop msgLoop;
	
	@Autowired
	private Context context;

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(Application.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		context.init();
		mainLoop.start();
		msgLoop.start();

		while (running.get()) {
			Thread.sleep(3000);
			logger.info("Application Alive.");
		}

		msgLoop.shutdown();
		msgLoop.join();

		mainLoop.shutdown();
		mainLoop.join();

		logger.info("System has been shutdown!!!");
		context.reset();
		shutdownSignal.release();
	}
	
	public boolean isRunning() {
		return running.get();
	}
	
	public boolean sendMessage(Message protoMsg, String topic) {
		return sendMessage(protoMsg, topic, "");
	}
	
	public boolean sendMessage(Message protoMsg, String topic, String tags) {
		return msgLoop.sendMessage(protoMsg, topic, tags);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		try {
			if (event instanceof ContextClosedEvent) {
				// Set the shutdown state and wait for all business threads to exit.
				if (running.compareAndSet(true, false)) {
					// Block the execution of ShutdownHook and wait for the service exit completion signal.
					shutdownSignal.acquire();
				}
			} else if (event instanceof ApplicationStartedEvent) {
				// set running state
				if (running.compareAndSet(false, true)) {
					shutdownSignal.acquire();
					logger.info("System started.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

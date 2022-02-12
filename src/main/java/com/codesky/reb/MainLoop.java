package com.codesky.reb;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MainLoop extends Thread {

	private final AtomicBoolean running = new AtomicBoolean(true);
	
	private final Logger logger = LoggerFactory.getLogger(MainLoop.class);

	public MainLoop() {
		super("MainLoopThread");
	}

	public void shutdown() {
		running.set(false);
	}

	@Override
	public void run() {
		logger.info("MainLoopThread running.");

		while (running.get()) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("MainLoop");
		}
		
		logger.info("MainLoopThread exit.");
	}

}

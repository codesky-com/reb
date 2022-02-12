package com.codesky.reb;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MsgLoop extends Thread {
	
	private final AtomicBoolean running = new AtomicBoolean(true);
	
	private final Logger logger = LoggerFactory.getLogger(MsgLoop.class);

	public MsgLoop() {
		super("MsgLoopThread");
	}
	
	public void shutdown() {
		running.set(false);
	}
	
	@Override
	public void run() {
		logger.info("MainLoopThread running.");
		
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

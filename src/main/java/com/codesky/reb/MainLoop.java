package com.codesky.reb;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesky.reb.interfaces.Service;

@Component
public class MainLoop extends Thread {

	private final AtomicBoolean running = new AtomicBoolean(true);

	private final Logger logger = LoggerFactory.getLogger(MainLoop.class);

	@Autowired
	private Context context;

	public MainLoop() {
		super("MainLoopThread");
	}

	public void shutdown() {
		running.set(false);
	}

	private void start_services() {
		for (Service srv : context.getServices()) {
			try {
				if (srv.isEnabled()) {
					srv.start();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	private void stop_services() {
		for (Service srv : context.getServices()) {
			try {
				if (srv.isEnabled()) {
					srv.stop();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	private void tick(long ms) {
		for (Service srv : context.getServices()) {
			try {
				if (srv.isEnabled()) {
					srv.tick(ms);
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	@Override
	public void run() {
		logger.info("MainLoopThread running.");
		start_services();

		while (running.get()) {
			long ms = System.currentTimeMillis();
			try {
				tick(ms);
				Thread.sleep(3);
			} catch (InterruptedException e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
		}

		stop_services();
		logger.info("MainLoopThread exit.");
	}

}

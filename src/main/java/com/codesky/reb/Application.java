package com.codesky.reb;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;

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

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		try {
			if (event instanceof ContextClosedEvent) {
				// 设置关闭状态并且等待所有业务线程退出
				if (running.compareAndSet(true, false)) {
					// 阻塞ShutdownHook执行，等待业务退出完成信号。
					shutdownSignal.acquire();
				}
			} else if (event instanceof ApplicationStartedEvent) {
				// 设置运行状态Ï
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

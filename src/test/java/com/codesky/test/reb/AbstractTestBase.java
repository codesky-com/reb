package com.codesky.test.reb;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.codesky.reb.Application;
import com.codesky.reb.utils.SpringUtils;

public abstract class AbstractTestBase implements ApplicationListener<ApplicationEvent> {

	public abstract void onStarted(Application app);

	public void startup() {
		SpringApplication application = new SpringApplication(Application.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.addListeners(this);
		application.run(new String[] {});
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartedEvent) {
			Application app = SpringUtils.getBean(Application.class);
			onStarted(app);
		}
	}

}

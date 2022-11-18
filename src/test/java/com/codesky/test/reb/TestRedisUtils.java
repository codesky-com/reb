package com.codesky.test.reb;

import org.junit.Test;

import com.codesky.reb.Application;
import com.codesky.reb.utils.RedisUtils;
import com.codesky.reb.utils.SpringUtils;

public class TestRedisUtils extends AbstractTestBase {
	
	private final Thread thread = new Thread(new Runnable() {
		private int tickCount = 0;
		
		@Override
		public void run() {
			Application app = SpringUtils.getBean(Application.class);
			RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
			
			while (app.isRunning()) {
				try {
					if (tickCount % 1000 == 0) {
						redisUtils.increment("TEST_UUID", 1);
						Object v = redisUtils.get("TEST_UUID");
						System.out.println(v);
					}
					Thread.sleep(1);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				tickCount++;
			}
		}
	});
	
	@Override
	public void onStarted(Application app) {
		thread.start();
	}
	
	@Test
	public void test() throws Throwable {
//		startup();
	}

}

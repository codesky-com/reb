package com.codesky.test.reb;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.springframework.stereotype.Component;

import com.codesky.reb.Application;
import com.codesky.reb.Service;
import com.codesky.reb.utils.SpringUtils;
import com.codesky.test.reb.message.TestHelloMessageHandler;
import com.codesky.test.reb.message.TestHelloMessageOuterClass.TestHelloMessage;

@Component
public class TestMessage implements Service {

	private final AtomicLong sendCounter = new AtomicLong(0);
	private volatile long prevExecuteTime = 0;
	private volatile long prevCompletedNums = 0;

	private Thread sendThread = new Thread(new Runnable() {
		@Override
		public void run() {
			Application app = SpringUtils.getBean(Application.class);
			final int tps = 250 * 2;
			while (app.isRunning()) {
				try {
					TestHelloMessage msg = TestHelloMessage.newBuilder().setName("KK-" + System.currentTimeMillis())
							.setAge(18).build();
					app.sendMessage(msg, "test");
					sendCounter.incrementAndGet();
					Thread.sleep(1000 / tps);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});

	@Override
	public void tick(long ms) {
		if (prevExecuteTime == 0) {
			sendThread.start();
		}

		if (prevExecuteTime == 0 || (prevExecuteTime + 1000) < ms) {
			long sendNums = sendCounter.get();
			long completedNum = TestHelloMessageHandler.COUNTER.get();
			String status = String.format("[Message] Send: %d, Completed: %d, Pending: %d, Tps: %d", sendNums, completedNum,
					(sendNums - completedNum), (completedNum - prevCompletedNums));
			System.out.println(status);

			prevExecuteTime = ms;
			prevCompletedNums = completedNum;
		}
	}

	@Test
	public void testMessage() {
		Application.main(new String[] {});
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}
}

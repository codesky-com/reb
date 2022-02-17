package com.codesky.test.reb.message;

import java.util.concurrent.atomic.AtomicLong;

import com.codesky.reb.message.MessageHandler;
import com.google.protobuf.Message;

public class TestHelloMessageHandler implements MessageHandler {

	public final static AtomicLong COUNTER = new AtomicLong(0);
	
	@Override
	public void execute(long cmd, Message msg) {
		COUNTER.incrementAndGet();
	}

}

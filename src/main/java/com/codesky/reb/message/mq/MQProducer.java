package com.codesky.reb.message.mq;

public interface MQProducer {

	public boolean setup(String uri);
	
	public void start();
	
	public void shutdown();
	
	public boolean send(byte[] body, String topic, String tags);
	
}

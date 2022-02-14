package com.codesky.reb.message.mq;

public class MQMessage {

	private String msgId;
	private long bornTimestamp;
	private long queueOffset;
	private int reconsumeTimes;
	private byte[] body;

	
	public MQMessage() {
	}
	
	public MQMessage(String msgId, long bornTimestamp, long queueOffset, int reconsumeTimes, byte[] body) {
		this.msgId = msgId;
		this.bornTimestamp = bornTimestamp;
		this.queueOffset = queueOffset;
		this.reconsumeTimes = reconsumeTimes;
		this.body = body;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public long getBornTimestamp() {
		return bornTimestamp;
	}

	public void setBornTimestamp(long bornTimestamp) {
		this.bornTimestamp = bornTimestamp;
	}

	public long getQueueOffset() {
		return queueOffset;
	}

	public void setQueueOffset(long queueOffset) {
		this.queueOffset = queueOffset;
	}

	public int getReconsumeTimes() {
		return reconsumeTimes;
	}

	public void setReconsumeTimes(int reconsumeTimes) {
		this.reconsumeTimes = reconsumeTimes;
	}
	
	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
}

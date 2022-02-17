/*
 * Copyright 2002-2022 CODESKY.COM Team Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Github:
 *	https://github.com/codesky-com/reb.git
 */

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

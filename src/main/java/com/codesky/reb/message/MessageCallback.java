package com.codesky.reb.message;

import com.google.protobuf.Message;

public interface MessageCallback {

	public boolean onMessage(long cmd, Message body);
	
}

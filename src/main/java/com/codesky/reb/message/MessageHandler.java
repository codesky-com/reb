package com.codesky.reb.message;

import com.google.protobuf.Message;

public interface MessageHandler {
	
	public void execute(long cmd, Message msg);
	
}
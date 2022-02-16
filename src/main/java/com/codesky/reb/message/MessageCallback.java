package com.codesky.reb.message;

import com.codesky.reb.message.struct.DataPacket;

public interface MessageCallback {

	public boolean onMessage(DataPacket packet);
	
}

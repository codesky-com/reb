package com.codesky.reb.message;

import java.util.Collection;

import com.codesky.reb.message.struct.DataPacket;

public interface MessageCallback {

	public boolean onMessage(Collection<DataPacket> packets);
	
}

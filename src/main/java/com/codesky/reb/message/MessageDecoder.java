package com.codesky.reb.message;

import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codesky.reb.message.struct.DataPacket;
import com.google.protobuf.Message;

public class MessageDecoder {

	private final static Logger LOGGER = LoggerFactory.getLogger(MessageDecoder.class);
	
	private final MessageFactory messageFactory;
	
	public MessageDecoder(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}
	
	private boolean checkSign(DataPacket packet) {
		CRC32 crc32 = new CRC32();
		crc32.update(packet.getData());
		return (crc32.getValue() == packet.getSign());
	}
	
	public Message decode(DataPacket packet) {
		if (!checkSign(packet)) {
			LOGGER.error("Invalid signature! cmd={}", Long.toHexString(packet.getCmd()));
			return null;
		}
		
		if (packet.getLength() != (packet.getData().length + DataPacket.HEADER_SIZE)) {
			LOGGER.error("Invalid data length! cmd={}", Long.toHexString(packet.getCmd()));
			return null;
		}
		
		return messageFactory.newMessage(packet.getCmd(), packet.getData());
	}
}

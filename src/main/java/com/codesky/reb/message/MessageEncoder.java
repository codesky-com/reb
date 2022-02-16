package com.codesky.reb.message;

import java.util.zip.CRC32;

import com.codesky.reb.message.struct.DataPacket;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

public class MessageEncoder {

	private final MessageFactory messageFactory;

	public MessageEncoder(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	private long sign(byte[] data) {
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		return crc32.getValue();
	}

	public DataPacket encode(Message protoMsg) {
		Descriptor descriptor = protoMsg.getDescriptorForType();
		byte[] data = protoMsg.toByteArray();
		long cmd = messageFactory.findMessageCmdByPackageName(descriptor.getFullName());
		long sign = sign(data);
		return new DataPacket(cmd, 0, sign, data);
	}
}

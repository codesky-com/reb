package com.codesky.reb.support;

import java.io.IOException;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

import com.codesky.reb.message.MessageDecoder;
import com.codesky.reb.message.MessageEncoder;
import com.codesky.reb.message.struct.DataPacket;
import com.codesky.reb.message.struct.DataStructOuterClass.DataStruct;
import com.google.protobuf.Message;

public class DataPacketMessageConverter extends ProtobufHttpMessageConverter {

	private final MessageEncoder encoder;
	private final MessageDecoder decoder;
	
	public DataPacketMessageConverter(MessageEncoder encoder, MessageDecoder decoder) {
		this.encoder = encoder;
		this.decoder = decoder;
	}
	
	@Override
	protected Message readInternal(Class<? extends Message> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		DataStruct ds = (DataStruct) super.readInternal(DataStruct.class, inputMessage);
		DataPacket packet = new DataPacket(ds);
		return decoder.decode(packet);
	}
	
	@Override
	protected void writeInternal(Message message, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		DataPacket packet = this.encoder.encode(message);
		super.writeInternal(packet.toDataStruct(), outputMessage);
	}
	
}

package com.codesky.reb.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codesky.reb.message.MessageDecoder;
import com.codesky.reb.message.MessageEncoder;
import com.codesky.reb.message.struct.DataPacket;
import com.google.protobuf.Message;

public class CmdHandler {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(CmdHandler.class);
	
	public final static int SERVER_ERROR = 500;
	public final static int SERVER_RESPONSE_ERROR = 505;
	
	private final CmdManager manager;
	private final DataPacket packet;
	private final MessageEncoder encoder;
	private final MessageDecoder decoder;
	
	
	public CmdHandler(CmdManager manager, DataPacket packet, MessageEncoder encoder, MessageDecoder decoder) {
		this.manager = manager;
		this.packet = packet;
		this.encoder = encoder;
		this.decoder = decoder;
	}

	public void handle(HttpServletResponse response) {
		try {			
			Message msg = decoder.decode(packet);
			Object result = manager.execute(packet.getCmd(), msg);
			
			if (result instanceof Message) {
				DataPacket packet = encoder.encode((Message)result);
				response.getOutputStream().write(packet.toDataStructByteArray());
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The result type returned by the command({}) is not type Message", packet.getCmd());
				}
				response.sendError(SERVER_RESPONSE_ERROR);
			}
		} catch (Throwable ex) {
			try {
				response.sendError(SERVER_ERROR);
			} catch (IOException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(null, e);
				}
			}
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(null, ex);
			}
		}
	}
	
}

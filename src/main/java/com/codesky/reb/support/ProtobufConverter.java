package com.codesky.reb.support;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.codesky.reb.message.MessageDecoder;
import com.codesky.reb.message.MessageEncoder;

@Component
public class ProtobufConverter {
	
	@Autowired
	private MessageEncoder encoder;
	
	@Autowired
	private MessageDecoder decoder;

	@Bean
	public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
		return new DataPacketMessageConverter(encoder, decoder);
	}
	
	@Bean
	public RestTemplate restTemplate(ProtobufHttpMessageConverter protobufHttpMessageConverter) {
		return new RestTemplate(Collections.singletonList(protobufHttpMessageConverter));
	}
	
}

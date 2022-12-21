package com.codesky.reb.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import com.codesky.reb.message.MessageDecoder;
import com.codesky.reb.message.MessageEncoder;

@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CmdHandlerMapping implements HandlerMapping {

	@Autowired
	private MessageEncoder encoder;
	
	@Autowired
	private MessageDecoder decoder;
	
	@Autowired
	private CmdManager manager;
	
	@Value("${reb.web.requestURI}")
	private String requestURI;
	
	@Override
	public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		String uri = request.getRequestURI();
		if (StringUtils.equalsIgnoreCase(uri, requestURI)) {
			CmdHandler handler = new CmdHandler(manager, encoder, decoder);
			HandlerExecutionChain chain = new HandlerExecutionChain(handler);
			return chain;
		}
		return null;
	}

}

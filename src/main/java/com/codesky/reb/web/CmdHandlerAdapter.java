package com.codesky.reb.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CmdHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return (handler instanceof CmdHandler);
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		CmdHandler ch = (CmdHandler)handler;
		ch.handle(request, response);
		return null;
		
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		return 0;
	}

}

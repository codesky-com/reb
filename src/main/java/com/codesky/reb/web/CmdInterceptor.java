package com.codesky.reb.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.web.servlet.HandlerInterceptor;

public abstract class CmdInterceptor implements HandlerInterceptor {
	
	public final static List<Long> INTERCEPT_ALL_COMMANDS = Collections.unmodifiableList(new ArrayList<Long>());
	
	public List<Long> interceptCommands() {
		return INTERCEPT_ALL_COMMANDS;
	}
	
	public List<Long> excludeCommands() {
		return null;
	}
	
}

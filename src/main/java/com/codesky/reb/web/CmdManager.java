package com.codesky.reb.web;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerExecutionChain;

import com.codesky.reb.message.MessageDecoder;
import com.codesky.reb.message.MessageEncoder;
import com.codesky.reb.message.MessageFactory;
import com.codesky.reb.message.struct.DataPacket;
import com.codesky.reb.message.struct.DataStructOuterClass.DataStruct;
import com.codesky.reb.utils.SpringUtils;

@Component
public class CmdManager implements InitializingBean {
	
	private final Logger logger = LoggerFactory.getLogger(CmdManager.class);
	
	private final Map<Long, Method> cmdMappings = new ConcurrentHashMap<Long, Method>();
	
	private final List<CmdInterceptor> interceptors = new CopyOnWriteArrayList<CmdInterceptor>();
	
	@Autowired
	private MessageEncoder encoder;
	
	@Autowired
	private MessageDecoder decoder;
	
	@Autowired
	private MessageFactory messageFactory;
	
	protected void registerMappingCommands() {
		Map<String, Object> beansMap = SpringUtils.getBeansWithAnnotation(Controller.class);
		beansMap.forEach((k, v) -> {
			Method[] methods = v.getClass().getDeclaredMethods();
			for (Method m : methods) {
				CmdMapping value = m.getAnnotation(CmdMapping.class);
				if (value != null) {
					cmdMappings.putIfAbsent(value.cmd(), m);
				}
			}
		});
	}
	
	protected void registerInterceptors() {
		Map<String, CmdInterceptor> map = SpringUtils.getBeansOfType(CmdInterceptor.class);
		map.forEach((k, v) -> this.interceptors.add(v));
	}
	
	protected List<CmdInterceptor> filterInterceptors(long cmd) {
		List<CmdInterceptor> list = new ArrayList<CmdInterceptor>();
		interceptors.forEach((item) -> {
			if ((item.interceptCommands() == CmdInterceptor.INTERCEPT_ALL_COMMANDS)
				|| item.interceptCommands().contains(cmd)) {
				if (item.excludeCommands() != null && item.excludeCommands().contains(cmd)) {
					return;
				}
				list.add(item);
			}
		});
		return list;
	}
	
	public Object execute(long cmd, Object args) {
		Method method = cmdMappings.get(cmd);
		if (method == null) {
			method = messageFactory.getMessageHandlerByCmd(cmd);
		}
		
		if (method != null) {
			Object target = SpringUtils.getBean(method.getDeclaringClass());
			if (target != null) {
				try {
					return method.invoke(target, args);
				} catch (Throwable ex) {
					if (logger.isErrorEnabled()) {
						logger.error(null, ex);
					}
				}
			}
		}
		return null;
	}
	
	public HandlerExecutionChain newExecutionChain(HttpServletRequest request) {
		try {
			DataStruct ds = DataStruct.parseFrom(request.getInputStream());
			DataPacket packet = new DataPacket(ds);
			
			CmdHandler handler = new CmdHandler(this, packet, encoder, decoder);
			HandlerExecutionChain chain = new HandlerExecutionChain(handler);
			
			List<CmdInterceptor> interceptors = filterInterceptors(packet.getCmd());
			if (interceptors.size() > 0) {
				chain.addInterceptors(interceptors.toArray(new CmdInterceptor[interceptors.size()]));
			}
			return chain;
		} catch (Throwable ex) {
			logger.error(null, ex);
		}
		return null;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		registerMappingCommands();
		registerInterceptors();
	}

}

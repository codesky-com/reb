package com.codesky.test.reb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codesky.test.reb.message.TestHelloMessageOuterClass.TestHelloMessage;

@Controller
public class TestController {
	
	@RequestMapping(value = "/index", method = RequestMethod.POST, produces = "application/x-protobuf")
	public @ResponseBody TestHelloMessage index(@RequestBody TestHelloMessage input) {
		System.out.println("=================[");
		System.out.println(input);
		System.out.println("=================]");
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (4096 * 10); i++) {
			sb.append('K');
		}
		sb.append('C');
		
		TestHelloMessage msg = TestHelloMessage.newBuilder().setName(sb.toString()).setAge((int)(Math.random() * 10)).build();
		return msg;
	}
	
}

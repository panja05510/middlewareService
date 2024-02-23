package com.citizens.mainframe.controller;

import java.io.IOException;

import javax.jms.JMSException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.citizens.mainframe.service.RequestResponseHandler;


@Controller
@RequestMapping("/")
public class MiddlewareServiceController {
	RequestResponseHandler requestResponseHandler;

	
	@GetMapping("request")
	public String mainframeResponse() throws IOException, JMSException {
		Object obj=requestResponseHandler.callMq();
		return obj.toString();
		
	}
	
	

}

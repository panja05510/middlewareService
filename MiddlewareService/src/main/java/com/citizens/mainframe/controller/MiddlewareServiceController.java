package com.citizens.mainframe.controller;

import java.io.IOException;

import javax.jms.JMSException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.citizens.mainframe.service.RequestResponseHandler;


@Controller
public class MiddlewareServiceController {
	RequestResponseHandler requestResponseHandler;

	
	@PostMapping
	public String mainframeResponse() throws IOException, JMSException {
		Object obj=requestResponseHandler.callMq();
		return obj.toString();
		
	}
	
	

}

package com.citizens.mainframe.controller;

import java.io.IOException;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citizens.mainframe.service.RequestResponseHandler;


@RestController
public class MiddlewareServiceController {
	@Autowired
	RequestResponseHandler requestResponseHandler;

	
	@GetMapping("/result")
	public String mainframeResponse() throws IOException, JMSException {
		System.out.println("request received at controller...");
		String obj=requestResponseHandler.callMq();
		System.out.println(obj);
		return obj;
		
	}
	
	

}

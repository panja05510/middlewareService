package com.citizens.mainframe.controller;

import java.io.IOException;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citizens.mainframe.model.SavingsAccountDetails;
import com.citizens.mainframe.service.RequestResponseHandler;


@RestController
@RequestMapping("/deposit-accounts-fees/savings/closing-balance")
public class MiddlewareServiceController {
	@Autowired
	RequestResponseHandler requestResponseHandler;

	
	@PostMapping("/query" )
	public String mainframeResponse(SavingsAccountDetails savingsAccountDetails) throws IOException, JMSException {
		
		
		String obj=requestResponseHandler.callMq(savingsAccountDetails);
		//System.out.println(obj);
		return obj;
		
	}
	
	

}

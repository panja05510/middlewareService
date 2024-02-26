package com.citizens.mainframe.controller;

import java.io.IOException;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	public String mainframeResponse( @RequestBody SavingsAccountDetails savingsAccountDetails) throws IOException, JMSException {
		
		System.out.println(savingsAccountDetails.toString());
		String obj=requestResponseHandler.callMq(savingsAccountDetails);
		 
		return obj;
		
	}
	
	

}

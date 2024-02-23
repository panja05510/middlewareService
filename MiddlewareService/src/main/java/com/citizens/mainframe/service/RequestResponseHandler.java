package com.citizens.mainframe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Component;

import com.citizens.mainframe.model.ResponseBaseModel;

import jakarta.jms.JMSException;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RequestResponseHandler {
	
	@Autowired
	private  MFRequestHandler mfRequestResponseHandler ;
	
	@Autowired
	ResponseBaseModel responseBaseModel;
	
	@Autowired
	MessageSender sender;
	
	private  MessageReceiver receiver;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	public Object callMq( ) throws IOException, javax.jms.JMSException {
		try {
			String correlationId = sender.sendMessageToQueue();
			System.out.println("Request Sent to mainframe with message ID :"+"/"+correlationId+"/ \n");
			Object response = receiver.receiveMessageByCorrelationId(correlationId, responseBaseModel);
			return response;
		}
		catch (JmsException e) {
			//System.out.println("jmsException occur at RequestRespondHandler.java");
			logger.error("Eroor in services RequestRespondHandler : {}",e);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error("Eroor in services RequestRespondHandler : {}",e);
		}
		return "exception occur";
	}
}

package com.citizens.mainframe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.citizens.mainframe.model.SavingsAccountDetails;

import jakarta.jms.BytesMessage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.UUID;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageSender {
	private final JmsTemplate jmsTemplate;

	@Autowired
	MFRequestHandler mfRequestHandler;

	public MessageSender(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public String sendMessageToQueue(SavingsAccountDetails savingsAccountDetails)
			throws jakarta.jms.JMSException, IOException {

		byte[] message = mfRequestHandler.JsonToEbc(savingsAccountDetails);

		String[] correlationId = { "emptyString" }; // Using an array to make it mutable

		try {

			BytesMessage execute = jmsTemplate.execute(session -> {
				BytesMessage jmsMessage = session.createBytesMessage();
				jmsMessage.writeBytes(message);

				jmsMessage.setJMSType("1");

				// Generating and set JMSCorrelationID
				correlationId[0] = "200" + UUID.randomUUID().toString();
//                System.out.println("correlationId: " + correlationId[0]);
				jmsMessage.setJMSCorrelationID(correlationId[0]);
				jmsMessage.setJMSMessageID(correlationId[0]);

//                System.out.println("jms message is: " + jmsMessage);
				sendJmsMessage(jmsMessage, "DEV.QUEUE.1");

//                System.out.println("Message sent to DEV.QUEUE.1: " + message + jmsMessage.getJMSMessageID());

				return jmsMessage;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return correlationId[0];
	}

	public void sendJmsMessage(BytesMessage jmsMessage, String queueName) {
		jmsTemplate.convertAndSend(queueName, jmsMessage, mess -> {
			jmsMessage.setIntProperty("JMS_IBM_MQMD_MsgType", 1);

//    		System.out.println("jmsmessage from sendjmsMessage: "+jmsMessage);
//			logger.info("Message Sender    msg send to DEV.QUEUE.1    corrId {} Time : {}",
//					jmsMessage.getJMSCorrelationID(), messageSenderTime);
			return jmsMessage;
		});
	}
}

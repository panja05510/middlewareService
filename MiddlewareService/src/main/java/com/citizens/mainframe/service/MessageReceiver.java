package com.citizens.mainframe.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.citizens.mainframe.model.ResponseBaseModel;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import org.springframework.jms.core.JmsTemplate;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.ibm.jms.JMSBytesMessage;
import com.ibm.msg.client.jakarta.jms.JmsMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageReceiver {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	EbcdicToJson ebcdicTojson;

	@Autowired
	ParseCopybook copybookParser;

	@Autowired
	private ebcdic2json2 e2j;

	private final JmsTemplate jmsTemplate;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public MessageReceiver(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public String receiveMessageByCorrelationId(String correlationId, ResponseBaseModel responseBaseModel)
			throws jakarta.jms.JMSException, IOException, javax.jms.JMSException {
		jmsTemplate.setReceiveTimeout(20000);

		// Receive the message
		BytesMessage receivedMessage = (BytesMessage) jmsTemplate.receiveSelected("DEV.QUEUE.2",
				"JMSCorrelationID='" + correlationId + "'");
		if (receivedMessage != null) {
//			logger.info("MessageReceiver    message received    corrId : {}", receivedMessage.getJMSCorrelationID());
		}

		if (receivedMessage != null) {

			long bodyLength = receivedMessage.getBodyLength();
			System.out.println("received message body lenght is : ------> " + bodyLength);

			byte[] bytes = new byte[(int) receivedMessage.getBodyLength()];
			System.out.println("bytes length is : " + bytes.length);

			receivedMessage.readBytes(bytes);
//			System.out.println(bytes);
//			for (byte b : bytes)
//				System.out.println(b);
//			JsonObject json=ebcdicTojson.mainframe2json(bytes, responseBaseModel);
//			ArrayList<HashMap<String, String>> copybookToIntermediate = copybookParser
//					.copybookToIntermediate("customer.cpy");
//			List<Map<String, String>> copybookToListOfHash = printCopybook(copybookToIntermediate);
//			String convertToJSON = e2j.convertToJSON(bytes, copybookToListOfHash);
			// return convertToJSON;
			JsonObject json = ebcdicTojson.mainframe2json(bytes, responseBaseModel);
			return json.toString();

		} else
			return null;
	}

	public List<Map<String, String>> printCopybook(List<HashMap<String, String>> copybook) {
		List<Map<String, String>> copybookToListOfMap = new ArrayList<>();
		System.out.println("----------------------------------------------------------------");
		for (HashMap<String, String> field : copybook) {
			Map<String, String> tempMap = new HashMap<>();

			System.out.println("Field:");
			for (Map.Entry<String, String> entry : field.entrySet()) {
				tempMap.put(entry.getKey(), entry.getValue());
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
			copybookToListOfMap.add(tempMap);
			System.out.println();
		}
		System.out.println("--------------------------------------------------------------------------------");
		return copybookToListOfMap;
	}

}

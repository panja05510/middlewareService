package com.citizens.mainframe.service;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;




import org.springframework.stereotype.Component;



@Component

public class MFRequestHandler {

	

	public HashMap<String, String> populateMap() {

		HashMap<String, String> cobolConstants = new HashMap<>();
		cobolConstants.put("SDSTMYM1-EXT-AMT", "3578");
		cobolConstants.put("SDSTMYM1-EXT-AMT-LIT", "ACAI");
		cobolConstants.put("ACAI-SERVICE-NAME", "ACCOUNT CLOSING BALANCE INQUIRE");

		cobolConstants.put("ACAI-SESSION-ID", "UNKNOWN");

		cobolConstants.put("ACAI-SESSION-ARCHIVE", "Y");

		return cobolConstants;

	}

	

	public List<String> hexFields(){

		return new ArrayList<String>();

	}

	

	public byte[] JsonToEbc() {

		HashMap<String, String> cobolConstants = populateMap();

		List<String> hexFields = hexFields();

		String cobolFile = "customer.cpy";

		

		JsonToEbcdic j2e = new JsonToEbcdic(cobolConstants, hexFields);

		try {

			byte[] content = j2e.request2mainframe(cobolFile);

			return content;

		} catch (InterruptedException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

		return null;

	}
}

package com.citizens.mainframe.service;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import org.springframework.stereotype.Component;

import com.citizens.mainframe.model.SavingsAccountDetails;

@Component

public class MFRequestHandler {

	public HashMap<String, String> populateMap(SavingsAccountDetails savingsAccountDetails) {

		HashMap<String, String> cobolConstants = new HashMap<>();
		cobolConstants.put("ACC-NUMBER", savingsAccountDetails.getAccountNumber());
		cobolConstants.put("WITHDRAWAL-TYPE", savingsAccountDetails.getWithdrawalType());
		cobolConstants.put("SERVICE-NAME", "SAVINGS CLOSING BALANCE QUERY");
		cobolConstants.put("ACAI-CHANNEL", "NNN");
		cobolConstants.put("ACAI-SESSION-ID", "UNKNOWN");
		cobolConstants.put("ACAI-USER-ID", "UNKNOWN");
		return cobolConstants;

	}

	public List<String> hexFields() {

		return new ArrayList<String>();

	}

	public byte[] JsonToEbc(SavingsAccountDetails savingsAccountDetails) {

		HashMap<String, String> cobolConstants = populateMap(savingsAccountDetails);

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

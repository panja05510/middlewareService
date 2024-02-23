package com.citizens.mainframe.service;


import com.citizens.mainframe.model.ResponseAbendError;
import com.citizens.mainframe.model.ResponseBaseModel;
import com.citizens.mainframe.model.ResponseCBLModel;
import com.citizens.mainframe.model.ResponseErrorModel;
import com.google.gson.*;
import net.minidev.json.JSONValue;
import net.sf.JRecord.*;
import net.sf.JRecord.Details.AbstractLine;
import net.sf.JRecord.Details.LayoutDetail;
import net.sf.JRecord.Details.RecordDetail;
import net.sf.JRecord.Details.fieldValue.IFieldValue;
import net.sf.JRecord.cgen.def.IArray1Dimension;
import net.sf.JRecord.cgen.def.IArray2Dimension;
import net.sf.JRecord.cgen.def.IArray3Dimension;
import net.sf.JRecord.def.IO.builders.ICobolIOBuilder;
import net.sf.JRecord.detailsBasic.IItemDetails;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EbcdicToJson {

	private final String OPEN_BRACE = "{";
	private final String CLOSE_BRACE = "}";
	private final ResponseAbendError abendError;
	public static final String COMP_3_4="140";
	public static final String COMP_3_3="31";
	public static final String COMP_3_5="141";

	private EbcdicToJson(ResponseAbendError abendError) {
		this.abendError = abendError;
	}

	/*******************************************************************
	 * * CONVERT MAINFRAME TO JSON
	 ******************************************************************/
	public JsonObject mainframe2json(byte[] ebcdicBytes,ResponseBaseModel responseBaseModel) {
		try {
			
			Map<String, Map<String, String>> copybooks = responseBaseModel.getCopybooks();
			String ebcdicLength = Integer.toString(ebcdicBytes.length);
			System.out.println("length of ebcdicBytes--->"+ebcdicLength);
			String copybookName = getCopybookName(ebcdicLength, copybooks);
			System.out.println(copybookName);
			Resource resource = new ClassPathResource(copybookName);
			System.out.println(resource);
			InputStream inputStream = resource.getInputStream();
			System.out.println(inputStream);
			ICobolIOBuilder iob = JRecordInterface1.COBOL.newIOBuilder(inputStream, copybookName).setFont("CP500");
			AbstractLine line = iob.newLine(ebcdicBytes);
			LayoutDetail layout = iob.getLayout();
			RecordDetail record = layout.getRecord(0);
			IItemDetails root = record.getCobolItems().get(0);
			System.out.println(ebcdicLength);
			System.out.println(copybooks);
			System.out.println(root);
			System.out.println(layout);
			System.out.println("line --> " + line);
			System.out.println("Response: " + responseBaseModel);
			JsonObject responseOrError = getResponseOrError(ebcdicLength, copybooks, root, layout, line,
					responseBaseModel);
			
			return responseOrError;
		} catch (Exception e) {
			System.err.println("error accured during parsing Ebcdic To JSON :"+e);
			return null;

		}

	}

	/*************************************************************************************
	 * 8 Call error method or regular parsing method based on MF response length
	 */

	private JsonObject getResponseOrError(String ebcdicLength, Map<String, Map<String, String>> copybooks,
			IItemDetails root, LayoutDetail layout, AbstractLine line, ResponseBaseModel responseBaseModel) {
		System.out.println(responseBaseModel.getCobolfieldsmap());
		ResponseCBLModel rcm = responseBaseModel.getCobolfieldsmap();
		System.out.println(rcm.getLeaves());
		Map<String, String> leavesMap = rcm.getLeaves();
		System.out.println(rcm.getParents());
		List<String> parentsFieldsList = rcm.getParents();
		System.out.println(rcm.getHexFields());
		List<String> asHexFieldsList = rcm.getHexFields();
		System.out.println("after hexfields");

		JsonObject response = new JsonObject();
		
		JsonObject error;
//		System.out.println("Before");
//		System.out.println(copybooks.get(ebcdicLength));
		//System.out.println("before call");
		
	
		
		String responseType = copybooks.get("default").get("responseType");
		
		
		
		
		
		if (copybooks.containsKey(ebcdicLength)) {
			System.out.println("entering into if (copybooks.containsKey(ebcdicLength)) ");
			responseType = copybooks.get(ebcdicLength).get("responseType");
			System.out.println("response type -->"+responseType);
		}
//		

		ResponseErrorModel responseErrorModel = responseBaseModel.getError();
		final var returnCode = "returnCode";
		Map<String, String> errorFields = responseErrorModel.getErrorFieldnameMapping();
		
		System.out.println("error fields --> :"+errorFields);

		if (errorFields.containsKey(returnCode) && layout.getFieldNameMap().containsKey(errorFields.get(returnCode))) {
			if ("0".equals(line.getFieldValue(errorFields.get(returnCode)).asString())) {
				System.out.println("returnType : success");
				responseType = "success";

			} else if ("2".equals(line.getFieldValue(errorFields.get(returnCode)).asString())) {
				System.out.println("returnType : failure");
				responseType = "error";
			}
		}
		// ABEND 
System.out.println("before checking abendError");
abendError.setCopybookLength("680");
System.out.println(abendError.getCopybookLength());
		if (ebcdicLength.equals(abendError.getCopybookLength())) {
			System.out.println("entered equals getCopybookLength");
			
			error = getAbendErrorPayload(layout, line);
			response.add("error", error);
		}
		// if response type contains error it calls responseErrorFormatter service and
		// allocates TSMDA Errors
		else if (responseType.equals("error")) { // TSMDA errors
			System.out.println("entering response type error");
			if (errorFields.containsKey("messageSeverity")
					&& layout.getFieldNameMap().containsKey(errorFields.get("messageSeverity"))) {
				System.out.println("inside if of response type error");
				String mfResponseVal = line.getFieldValue(errorFields.get("messageSeverity")).asString();
				if (StringUtils.equalsIgnoreCase("I", mfResponseVal)) {
					response = getResponseJson(leavesMap, parentsFieldsList, asHexFieldsList, root,  line);

				} else {
					error = getTSMDAErrorPayload(layout, line, responseBaseModel);
					response.add("error", error);
				}
			} else {
				error = getTSMDAErrorPayload(layout, line, responseBaseModel);
				response.add("error", error);
			}
		} else {
			
		System.out.println("in getresponse or error entering last else");
			response = getResponseJson(leavesMap, parentsFieldsList, asHexFieldsList, root, line);
		}
		System.out.println(response);
		return response;
	}

	/*************************************************************************************
	 * GET APPROPRIATE COPYBOOK BASED ON RESPONSE LENGTH
	 *******************************************************************************************/

	public String getCopybookName(String ebcdicLength, Map<String, Map<String, String>> copybooks) {
		String copybookName = null;
		if (ebcdicLength.equals(abendError.getCopybookLength()))
			copybookName = abendError.getCopybook();

		else if (copybooks.containsKey(ebcdicLength))
			copybookName = copybooks.get("default").get("copybookName");
		
		return copybookName;
	}

	/**************************************************************************************
	 * GET JSON ARRAY RESPONSE
	 *****************************************************************************************/

	public JsonObject getResponseJson(Map<String, String> leavesMap, List<String> parentsFieldsList,
			List<String> asHexFieldsList, IItemDetails item,  AbstractLine line) {
		try {
			HashMap<String, Integer> fieldNameOccuranceMap = new HashMap<>();
			String res = OPEN_BRACE + parseRecordBinary(leavesMap, parentsFieldsList, asHexFieldsList, item,
					fieldNameOccuranceMap, line) + CLOSE_BRACE;
			System.out.println("In getResponseJson --> result : res-->"+res);
			res = correctResponseString(res);
			System.out.println("result: res-->"+res);
			System.out.println("error in parsing string to json");
			System.out.println(JsonParser.parseString(res).isJsonObject());
			
			System.out.println(JsonParser.parseString(res).getAsJsonObject());
			
			System.out.println("error here");
			return JsonParser.parseString(res).getAsJsonObject();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;

		}
	}

	/***********************************************************************************
	 * PARSE RECORD RECURSIVELY AND WRITE TO JSON FILE
	 ****************************************************************************************/
	public String parseRecordBinary(Map<String, String> leavesMap, List<String> parentsFieldsList,
			List<String> asHexFieldsList, IItemDetails items, HashMap<String, Integer> fieldDuplicates,
			AbstractLine ab) {

		if (items == null) {
			System.out.println("parseRecordBinary--> items is null");
			return null;
		}
		StringBuilder responseString = new StringBuilder();
		if (items.getChildItems() != null && items.getChildItems().size() > 0) {
			System.out.println(" entering 1st if items.getChildItems");
			for (IItemDetails i : items.getChildItems()) {
				String fieldName = i.getFieldName();

				// If IItemsDetails i is leaves
				if (i.isLeaf() && !(parentsFieldsList.contains(fieldName))) {
					System.out.println("parseecordBinary--> entered 1st if");
					
					responseString = new StringBuilder(getDuplicateFieldValues(leavesMap, i, fieldName, fieldDuplicates,
							ab, responseString.toString(), asHexFieldsList));
				}

				else {
					System.out.println("parseecordBinary--> entered 1st else");
					if (parentsFieldsList.contains(fieldName)) {
						System.out.println("parseecordBinary--> entered 1st else and if");

						// add below to response object string
						// parent_1{idx}

						responseString.append(String.format("\"parent_%d\"", parentsFieldsList.indexOf(fieldName) + 1));
						String OPEN_BRACKET = "[";
						responseString.append(":").append(OPEN_BRACKET);

						int occurs = i.getOccurs();
						if (occurs <= 0)
							occurs = 1;

						for (int j = 0; j < occurs; j++) {
							responseString.append(OPEN_BRACE);
							responseString.append(parseRecordBinary(leavesMap, parentsFieldsList, asHexFieldsList, i,
									fieldDuplicates, ab));
							responseString.append(CLOSE_BRACE + ",");
						}
						String CLOSE_BRACKET = "]";
						responseString.append(CLOSE_BRACKET).append(",");

					} else {
						System.out.println("parseRecord binary -->entering else else ");
						responseString.append(parseRecordBinary(leavesMap, parentsFieldsList, asHexFieldsList, i,
								fieldDuplicates, ab));}
					System.out.println("ParseRecord binary responseString is --> "+responseString);
					

				}
			}
		} else {
			System.out.println("parseecordBinary--> entered 1st else of if");
			String fieldName = items.getFieldName();
			responseString = new StringBuilder(getDuplicateFieldValues(leavesMap, items, fieldName, fieldDuplicates, ab,
					responseString.toString(), asHexFieldsList));
			System.out.println("in parserRecordBinary -->"+responseString);
		}
		return responseString.toString();
	}

	/*************************************************************************************************
	 * Get Abend error payload
	 **************************************************************************************/
	public JsonObject getAbendErrorPayload(LayoutDetail layout, AbstractLine line) {
		Map<String, String> errorFields = abendError.getFieldnameMapping();
		JsonObject error = new JsonObject();

		for (String cobolField : errorFields.keySet()) {
			String errorFieldMappedName = errorFields.get(cobolField);

			if (layout.getFieldNameMap().containsKey(cobolField)) {
				String mfResponseVal = line.getFieldValue(cobolField).asString();
				error.addProperty(errorFieldMappedName, mfResponseVal);
			}

		}

		return error;
	}

	/*******************************************************************************************
	 * Get TSMDA Error payload
	 *********************************************************************************************/
	public JsonObject getTSMDAErrorPayload(LayoutDetail layout, AbstractLine line,
			ResponseBaseModel responseBaseModel) {
		ResponseErrorModel rem = responseBaseModel.getError();

		Map<String, String> errorFields = rem.getErrorFieldnameMapping();
		JsonObject error = new JsonObject();

		for (String errorFieldAlias : errorFields.keySet()) {
			String errorField = errorFields.get(errorFieldAlias);
			JsonArray allValuesForErrorField = new JsonArray();
			String errorFieldOcc = String.format("%s(0)", errorField);

			// if only one occurance of error field in copybook(i.e no OCCURS)
			// e.g. TSMDA-OCCURANCE-IN-PROC
			if (layout.getFieldNameMap().containsKey(errorField)) {
				String mfResponseVal = line.getFieldValue(errorField).asString();
				allValuesForErrorField.add(mfResponseVal);
				error.add(errorFieldAlias, allValuesForErrorField);

			}

			// e.g. if TSMDA-OCCURANCE-IN-PROC (0) exists in copybook
			else if (layout.getFieldNameMap().containsKey(errorFieldOcc)) {
				int occurance = 0;
				while (layout.getFieldNameMap().containsKey(errorFieldOcc)) {
					String mfResponseVal = line.getFieldValue(errorFieldOcc).asString();
					allValuesForErrorField.add(mfResponseVal);
					errorFieldOcc = String.format("%s (%d)", errorField, ++occurance);
				}
				error.add(errorFieldAlias, allValuesForErrorField);

			}

		}
		return error;
	}

	/************************************************************************************************
	 * CORRECT JSON STRING
	 **************************************************************************************/

	public String correctResponseString(String jsonStringToCorrect) {
		jsonStringToCorrect = jsonStringToCorrect.replaceAll(",}", "}");
		jsonStringToCorrect = jsonStringToCorrect.replaceAll(",]", "]");
		jsonStringToCorrect = jsonStringToCorrect.replaceAll("\"\"","\",\"");
		return jsonStringToCorrect;
	}

	/*************************************************************************************************************
	 * added this implementation in order to get response values from mainframe
	 ********************************************************************************/

	private String getDuplicateFieldValues(Map<String, String> leavesMap, IItemDetails items, String fieldName,
			HashMap<String, Integer> fieldDuplicates, AbstractLine ab, String responseString,
			List<String> asHexFieldsList) {
		if (leavesMap.containsKey(fieldName)) {
			System.out.println("getDuplicateFieldValues--->1st if ");

			IFieldValue value;
			String fieldNameDuplicate = null;

			// if fieldname has already been added to map, increase occurance by 1
			if (fieldDuplicates.containsKey(fieldName)) {
				System.out.println("getDuplicateFieldValues--->2st if ");
				int fieldNameOccurance = fieldDuplicates.get(fieldName) + 1;
				fieldDuplicates.put(fieldName, fieldNameOccurance);

				// string
				IArray2Dimension twoDimensionalArray = items.getArrayDefinition().asTwoDimensionArray();
				IArray1Dimension oneDimensionalArray = items.getArrayDefinition().asOneDimensionArray();
				IArray3Dimension threeDimensionalArray = items.getArrayDefinition().asThreeDimensionArray();
				if (oneDimensionalArray != null) {
					System.out.println("getDuplicateFieldValues--->contains 1DArray ");
					fieldNameDuplicate = items.getArrayDefinition().getField(fieldNameOccurance).getName();
				}
				if (twoDimensionalArray != null) {
					System.out.println("getDuplicateFieldValues--->contains 2DArray ");
					int j = twoDimensionalArray.getArrayLength(1);
					int k = fieldNameOccurance / j;
					int l = (fieldNameOccurance) / j;
					fieldNameDuplicate = twoDimensionalArray.get(k, l).getName();
				}
				if (threeDimensionalArray != null) {
					System.out.println("getDuplicateFieldValues--->contains 3DArray ");
					int i = threeDimensionalArray.getArrayLength(1) * threeDimensionalArray.getArrayLength(2);
					int j = threeDimensionalArray.getArrayLength(2);
					int k = fieldNameOccurance / i;
					int l = (fieldNameOccurance % i) / j;
					int m = fieldNameOccurance % j;

					fieldNameDuplicate = threeDimensionalArray.get(k, l, m).getName();

				}

				value = ab.getFieldValue(fieldNameDuplicate);
			} else {
				System.out.println("getDuplicateFieldValues--->entering second else ");
				fieldDuplicates.put(fieldName, 0);

				// if filename(1) exits
				if (items.getItemType().isArray) {
					System.out.println("getDuplicateFieldValues--->entering second else 1st if ");
					String leafFieldName = items.getArrayDefinition().getField(0).getName();
					value = ab.getFieldValue(leafFieldName);
				} else {
					System.out.println("getDuplicateFieldValues--->entering second else and else ");
					value = ab.getFieldValue(fieldName);
					System.out.println("getDuplicateFieldValues--->entering second else and else: value" +fieldName );

				}
			}

			// Append to Response JSON String
			String fieldNameQuotes = String.format("\"%s\"", leavesMap.get(fieldName));
			String valueAsString = getValuesString(value);

			String valueQuotes = String.format("\"%s\"", JSONValue.escape(valueAsString));

			if (asHexFieldsList != null && asHexFieldsList.contains(fieldName)) {
				System.out.println("getDuplicateFieldValues--->entering 1st if if ");
				String realHexQuotes = String.format("\"%s\"", value.asHex());
				responseString += String.format("%s:%s,", fieldNameQuotes, valueQuotes);

			} else {
				System.out.println("getDuplicateFieldValues--->entering 1st if else : fieldNameQuotes"+fieldNameQuotes);
				System.out.println("getDuplicateFieldValues--->entering 1st if else : valueQuotes"+ valueQuotes);
				responseString += String.format("%s:%s", fieldNameQuotes, valueQuotes);
				System.out.println("getDuplicateFieldValues--->entering 1st if else ");
			}
		}
		System.out.println(responseString);
		return responseString;

	}

	/************************************************************************************
	 * CONVERT MAINFRAME COBOL VALUE TO STRING
	 ************************************************************************************/

	private String getValuesString(IFieldValue value) {
		String valueType = value.getTypeName();
		if ((COMP_3_4.equals(valueType) || COMP_3_3.equals(valueType) || COMP_3_5.equals(valueType))
				&& !value.isFieldPresent()) {
			System.out.println("getValuesString--> if block:"+valueType);
			return "";
		} else {
			System.out.println("getValuesString-->"+value.asString());
		return value.asString();
		}
	}
}


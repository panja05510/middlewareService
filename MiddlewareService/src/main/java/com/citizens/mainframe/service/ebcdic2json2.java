package com.citizens.mainframe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ebcdic2json2 {

	public static final String LATIN_1_CHARSET = "ISO-8859-1";
	public static final String EBCDIC_CHARSET = String.format("CP%s", "500");

	/****************************************************************************
	 * Method to convert EBCDIC byte array to JSON byte[] - ebcdic data as array of
	 * bytes list of map - copybook converted into list of map to define the
	 * structure of the data
	 ******************************************************************************/

	public String convertToJSON(byte[] ebcdicData, List<Map<String, String>> copybook) {
		String asciiData = new String(ebcdicData, Charset.forName(EBCDIC_CHARSET));
		System.out.println("ascii data -----> " + asciiData);
		StringBuilder jsonBuilder = new StringBuilder("{");

		int startIndex = 0;
		for (Map<String, String> field : copybook) {
			String fieldName = field.get("name");
			int fieldLength = Integer.parseInt(field.get("display_length"));
			String fieldValue = asciiData.substring(startIndex, startIndex + fieldLength).trim();
			System.out.println("substring is ==========> " + fieldValue);

			// Add field to JSON
			jsonBuilder.append("\"").append(fieldName).append("\": \"").append(fieldValue).append("\", ");

			startIndex += fieldLength;
			System.out.println("start index reach : " + startIndex);
		}

		// Remove trailing comma and space
		if (jsonBuilder.length() > 1) {
			jsonBuilder.setLength(jsonBuilder.length() - 2);
		}

		// Close JSON object
		jsonBuilder.append("}");

		return jsonBuilder.toString();
	}

	
	
	public class JsonToMap {

		public Map<String, String> convertToJson(String jsonData) throws JsonProcessingException {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(jsonData);

			Map<String, String> resultMap = new HashMap<>();
			populateMap(jsonNode, resultMap, "");
			return resultMap;
		}

		private void populateMap(JsonNode jsonNode, Map<String, String> resultMap, String currentPath) {
			Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				String fieldName = field.getKey();
				JsonNode fieldValue = field.getValue();

				if (fieldValue.isObject()) {
					populateMap(fieldValue, resultMap, currentPath + fieldName + ".");
				} else {
					resultMap.put(currentPath + fieldName, fieldValue.asText());
				}
			}
		}
	}
}

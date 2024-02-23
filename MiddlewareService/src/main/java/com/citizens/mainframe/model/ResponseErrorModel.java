package com.citizens.mainframe.model;

import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Component;



 import java.io.Serializable;

 import java.util.Map;



 @Component

 @Scope(value = "prototype")

 public class ResponseErrorModel implements Serializable {


private Map<String, String> errorFieldnameMapping;

private Map<String, Map<String, Map<String, String>>> validErrorMetadata;

public Map<String, String> getErrorFieldnameMapping() {
	return errorFieldnameMapping;
}

public void setErrorFieldnameMapping(Map<String, String> errorFieldnameMapping) {
	this.errorFieldnameMapping = errorFieldnameMapping;
}

public Map<String, Map<String, Map<String, String>>> getValidErrorMetadata() {
	return validErrorMetadata;
}

public void setValidErrorMetadata(Map<String, Map<String, Map<String, String>>> validErrorMetadata) {
	this.validErrorMetadata = validErrorMetadata;
}
 }


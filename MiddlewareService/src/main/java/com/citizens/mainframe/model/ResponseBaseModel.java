package com.citizens.mainframe.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@ComponentScan
@Getter
@Setter
@Component
@ConfigurationProperties("savingsclosingbalancequeryresponse")
public class ResponseBaseModel implements Serializable {

private String apiName;

private String resourceName;

private String queue;
@Autowired

private ResponseCBLModel cobolfieldsmap;

@Autowired
private ResponseErrorModel error;

private Map<String, Map<String, String>> mfResponseValuesMapping;

private Map<String, Map<String, String>> copybooks;







public String getApiName() {
	return apiName;
}
public void setApiName(String apiName) {
	this.apiName = apiName;
}
public String getResourceName() {
	return resourceName;
}
public void setResourceName(String resourceName) {
	this.resourceName = resourceName;
}
public String getQueue() {
	return queue;
}
public void setQueue(String queue) {
	this.queue = queue;
}
public Map<String, Map<String, String>> getCopybooks() {
	return copybooks;
}
public void setCopybooks(Map<String, Map<String, String>> copybooks) {
	this.copybooks = copybooks;
	
	
}
public ResponseCBLModel getCobolfieldsmap() {

return cobolfieldsmap;

}
public void setCobolfieldsmap(ResponseCBLModel cobolfieldsmap) { this.cobolfieldsmap = cobolfieldsmap;
}
public ResponseErrorModel getError() { return error;
}
public void setError(ResponseErrorModel error) {
this.error=error;
}
public Map<String, Map<String, String>> getMfResponseValuesMapping() {
return mfResponseValuesMapping;
}
public void setMfResponseValuesMapping(Map<String, Map<String, String>> mfResponseValuesMapping) { this.mfResponseValuesMapping = mfResponseValuesMapping;
}

@Override
public String toString() {
return String.format("%s, %s, %s)", this.getResourceName(), this.getQueue(), this.getApiName());
}
}

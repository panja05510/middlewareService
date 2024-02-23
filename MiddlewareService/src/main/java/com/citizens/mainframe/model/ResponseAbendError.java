package com.citizens.mainframe.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import java.io.Serializable;

import java.util.Map;

@Component

@ConfigurationProperties("abend")

public class ResponseAbendError implements Serializable {

	private Map<String, String> fieldnameMapping;

	private String copybook;

	private String copybookLength;

	public Map<String, String> getFieldnameMapping() {
		return fieldnameMapping;
	}

	public void setfieldnameMapping(Map<String, String> fieldnameMapping) {
		this.fieldnameMapping = fieldnameMapping;
	}

	public String getCopybook() {
		return copybook;
	}

	public void setCopybook(String copybook) {
		this.copybook = copybook;
	}

	public String getCopybookLength() {
		return copybookLength;
	}

	public void setCopybookLength(String copybookLength) {
		this.copybookLength = copybookLength;
	}
}

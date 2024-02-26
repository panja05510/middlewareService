package com.citizens.mainframe.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = "prototype")
public class ResponseCBLModel implements Serializable {

	private Map<String, String> leaves;
	private List<String> parents;
	private List<String> hexFields;

	public Map<String, String> getLeaves() {
		return leaves;
	}

	public void setLeaves(Map<String, String> leaves) {
		this.leaves = leaves;
	}

	public List<String> getParents() {
		return parents;
	}

	public void setParents(List<String> parents) {
		this.parents = parents;
	}

	public List<String> getHexFields() {
		return hexFields;
	}

	public void setHexFields(List<String> hexFields) {
		this.hexFields = hexFields;
	}

	@Override

	public String toString() {

		return "{" + this.getLeaves() + "," + this.getParents() + "}";

	}
}

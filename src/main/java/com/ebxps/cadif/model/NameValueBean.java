package com.ebxps.cadif.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Simple data-model bean for holding name/value pairs.
 * 
 * @author Steve Higgins - Orchestra Networks - December 2017
 *
 */
public class NameValueBean {

	@Expose @SerializedName("name")	
	private String name;
	
	@Expose @SerializedName("value")	
	private String value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("%s=[%s]", name, value);
	}
	
}

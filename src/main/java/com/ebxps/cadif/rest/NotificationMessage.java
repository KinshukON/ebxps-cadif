package com.ebxps.cadif.rest;

import java.util.List;

import com.ebxps.cadif.model.NameValueBean;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationMessage {

	@Expose @SerializedName("source")	
	private String source = "EBX";
	
	@Expose @SerializedName("system")	
	private String systemCode = null;
	
	@Expose @SerializedName("object")	
	private String objectCode = null;
	
	@Expose @SerializedName("action")	
	private String action = null;
	
	@Expose @SerializedName("objectid")	
	private String objectId = null;
	
	@Expose @SerializedName("logid") 	
	private String logIdentifier = null;
	
	@Expose @SerializedName("extraFields")
	private List<NameValueBean> extraFields = null;


	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String getObjectCode() {
		return objectCode;
	}

	public void setObjectCode(String objectCode) {
		this.objectCode = objectCode;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Integer getObjectId() {
		return Integer.parseInt(objectId);
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public int getLogIdentifier() {
		return Integer.parseInt(logIdentifier);
	}

	public void setLogIdentifier(Integer logIdentifier) {
		this.logIdentifier = logIdentifier.toString();
	}

	public List<NameValueBean> getExtraFields() {
		return extraFields;
	}

	public void setExtraFields(List<NameValueBean> extraFields) {
		this.extraFields = extraFields;
	}

}

package com.gsma.android.xoperatorapidemo.logo;

import org.json.JSONException;
import org.json.JSONObject;

public class LogoResponse {
	String operatorId;
	String apiName;
	String language;
	String size;
	String height;
	String width;
	String action;
	String url;
	String bgColourRange;
	
	public LogoResponse() {
	}

	public LogoResponse(Object source) throws JSONException {
		if (source!=null && source instanceof JSONObject) {
			JSONObject so=(JSONObject) source;
			operatorId=so.has("operatorId")?so.getString("operatorId"):null;
			apiName=so.has("apiName")?so.getString("apiName"):null;
			language=so.has("language")?so.getString("language"):null;
			size=so.has("size")?so.getString("size"):null;
			height=so.has("height")?so.getString("height"):null;
			width=so.has("width")?so.getString("width"):null;
			action=so.has("action")?so.getString("action"):null;
			url=so.has("url")?so.getString("url"):null;
			bgColourRange=so.has("bgColourRange")?so.getString("bgColourRange"):null;
		}
	}
	
	public JSONObject toObject() throws JSONException {
		JSONObject obj=new JSONObject();
		
		if (operatorId!=null) obj.put("operatorId", operatorId);
		if (apiName!=null) obj.put("apiName", apiName);
		if (language!=null) obj.put("language", language);
		if (size!=null) obj.put("size", size);
		if (height!=null) obj.put("height", height);
		if (width!=null) obj.put("width", width);
		if (action!=null) obj.put("action", action);
		if (url!=null) obj.put("url", url);
		if (bgColourRange!=null) obj.put("bgColourRange", bgColourRange);
		return obj;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBgColourRange() {
		return bgColourRange;
	}

	public void setBgColourRange(String bgColourRange) {
		this.bgColourRange = bgColourRange;
	}

	
	
/*
 * [{"operatorId":"exchange",
 * "apiName":"operatorid",
 * "language":"en",
 * "size":"large",
 * "height":"140px",
 * "width":"640px",
 * "action":"default",
 * "url":"http://integration-api.apiexchange.org:80/v1/logostorage/images/english/connect_with_operator_640x140.png",
 * "bgColourRange":"#ffffff,#000000"},
 * 
 * {"operatorId":"exchange",
 * "apiName":"payment",
 * "language":"en",
 * "size":"large",
 * "height":"140px",
 * "width":"640px",
 * "action":"default",
 * "url":"http://integration-api.apiexchange.org:80/v1/logostorage/images/english/pay_with_operator_640x140.png",
 * "bgColourRange":"#ffffff,#000000"}]
 */
	
}

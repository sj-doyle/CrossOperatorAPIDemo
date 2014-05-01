package com.gsma.android.xoperatorapidemo.discovery;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class DiscoveryData implements Serializable {
	private static final long serialVersionUID = 2147233270126180800L;
	private static final String TAG = "DiscoveryData";

	public DiscoveryData() {}

	public DiscoveryData(JSONObject json) throws JSONException { 
		if (json!=null) {
			this.ttl=json.getString("ttl");
			Log.d(TAG, "ttl = "+ttl);
			this.response=new Response(json.getJSONObject("response"));
		}
	}
	
	String ttl=null;
	public String getTtl() { return ttl; }
	public void setTtl(String ttl) { this.ttl = ttl; }

	Response response=null;
	public Response getResponse() { return this.response; }
	public void setResponse(Response response) { this.response=response; }
	
	public JSONObject toObject() throws JSONException {
		JSONObject obj=new JSONObject();
		obj.put("ttl", ttl);
		if (response!=null) obj.put("response", response);
		return obj;
	}

}

package com.gsma.android.xoperatorapidemo.discovery;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Link implements Serializable {
	private static final long serialVersionUID = 5293311103983139347L;
	private static final String TAG = "Link";

	String href=null;
	String rel=null;
	
	public Link() {
		
	}
	
	public Link(JSONObject json) throws JSONException { 
		if (json!=null) {
			this.rel=json.getString("rel");
			Log.d(TAG, "rel = "+rel);
			this.href=json.getString("href");
			Log.d(TAG, "href = "+href);
		}
	}
	
	public String getHref() { return this.href; }
	public void setHRef(String href) { this.href=href; }
	
	public String getRel() { return this.rel; }
	public void setRel(String rel) { this.rel=rel; }

}

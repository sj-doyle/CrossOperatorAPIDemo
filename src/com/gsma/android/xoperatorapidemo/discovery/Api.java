package com.gsma.android.xoperatorapidemo.discovery;

import java.io.Serializable;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Api implements Serializable {
	private static final long serialVersionUID = -7245260322532564148L;

	public Api() {
		
	}
	
	public Api (JSONObject jsonObject) throws JSONException {
		if (jsonObject!=null) {
			JSONArray linkArray=jsonObject.getJSONArray("link");
			if (linkArray!=null) {
				link=new Link[linkArray.length()];
				linkMap=new HashMap<String,String> ();
				for (int i=0; i<linkArray.length(); i++) {
					link[i]=new Link(linkArray.getJSONObject(i));
					linkMap.put(link[i].getRel(), link[i].getHref());
				}
			}
		}
	}
	
	Link[] link=null;
	public Link[] getLink() { return this.link; }
	public void setLink(Link[] link) {
		this.link=link;
		linkMap=new HashMap<String,String> ();
		if (link!=null) {
			for (Link l:link) {
				linkMap.put(l.getRel(), l.getHref());
			}
		}
	}
	
	HashMap<String,String> linkMap=null;
	@JsonIgnore
	public String getHref(String rel) { return (linkMap!=null&&rel!=null)?linkMap.get(rel):null; }

	public JSONObject toObject() throws JSONException {
		JSONObject obj=new JSONObject();
		if (link!=null) obj.put("link", link);
		return obj;
	}

	public String toString() {
		String rv=null;
		try {
			JSONObject obj = toObject();
			rv=obj.toString();
		} catch (JSONException e) {
		}
		return rv;
	}
	
}
	

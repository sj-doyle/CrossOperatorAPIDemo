package com.gsma.android.xoperatorapidemo.logo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public class LogoCacheItem {
	private static final String TAG="LogoCacheItem";
	
	public LogoResponse getLogo() {
		return logo;
	}
	public void setLogo(LogoResponse logo) {
		this.logo = logo;
	}
	public String getLocalFile() {
		return localFile;
	}
	public void setLocalFile(String localFile) {
		this.localFile = localFile;
	}
	public String getETag() {
		return eTag;
	}
	public void setETag(String eTag) {
		this.eTag = eTag;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	LogoResponse logo;
	String localFile;
	String eTag;
	String lastModified;
	long lastModifiedTimestamp;
	
	public long getLastModifiedTimestamp() {
		return lastModifiedTimestamp;
	}
	public void setLastModifiedTimestamp(long lastModifiedTimestamp) {
		this.lastModifiedTimestamp = lastModifiedTimestamp;
	}
	Bitmap imageFile;
	
	public Bitmap getImageFile() {
		return imageFile;
	}
	public void setImageFile(Bitmap imageFile) {
		this.imageFile = imageFile;
	}
	public LogoCacheItem (JSONObject jsonObject) throws JSONException {
		if (jsonObject!=null) {
			this.localFile=jsonObject.getString("localFile");
			this.eTag=jsonObject.getString("eTag");
			this.lastModified=jsonObject.getString("lastModified");
			this.lastModifiedTimestamp=jsonObject.getLong("lastModifiedTimestamp");
			Object logo=jsonObject.get("logo");
			if (logo!=null) {
				this.logo=new LogoResponse(logo);
			}
		}
	}
	
	public LogoCacheItem() {
	}
	
	public static LogoCacheItem[] fromSerialisedArrayString(String logoCacheSerialised) throws JSONException {
		LogoCacheItem[] logoArray=null;
		JSONArray sa=new JSONArray(logoCacheSerialised);
		if (sa!=null) {
			logoArray=new LogoCacheItem[sa.length()];
			for (int i=0; i<sa.length(); i++) {
				JSONObject cur=sa.getJSONObject(i);
				logoArray[i]=new LogoCacheItem(cur);
			}
		}
		return logoArray;
	}
	
	public JSONObject toObject() throws JSONException {
		JSONObject obj=new JSONObject();
		
		if (eTag!=null) obj.put("eTag", eTag);
		if (localFile!=null) obj.put("localFile", localFile);
		if (lastModified!=null) obj.put("lastModified", lastModified);
		if (logo!=null) obj.put("logo", logo.toObject());
		obj.put("lastModifiedTimestamp", lastModifiedTimestamp);
		return obj;
	}


}

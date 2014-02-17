package com.gsma.android.xoperatorapidemo.discovery;

public class DeveloperOperatorSetting {
	String name;
	String endpoint; 
	String appKey;
	String appSecret;
	
	public DeveloperOperatorSetting(String name, String endpoint, String appKey, String appSecret) {
		this.name=name;
		this.endpoint=endpoint;
		this.appKey=appKey;
		this.appSecret=appSecret;
	}
	
	public String getName() { return this.name; }
	public String getEndpoint() { return this.endpoint; }
	public String getAppKey() { return this.appKey; }
	public String getAppSecret() { return this.appSecret; }
	
}
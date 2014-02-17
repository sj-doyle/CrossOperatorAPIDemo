package com.gsma.android.xoperatorapidemo.payment;

public class CallbackReference {
	String callbackData=null;
	String notifyURL=null;
	String consentResumeURL=null;
	
	/**
	 * @return the callbackData
	 */
	public String getCallbackData() {
		return callbackData;
	}
	/**
	 * @param callbackData the callbackData to set
	 */
	public void setCallbackData(String callbackData) {
		this.callbackData = callbackData;
	}
	/**
	 * @return the notifyURL
	 */
	public String getNotifyURL() {
		return notifyURL;
	}
	/**
	 * @param notifyURL the notifyURL to set
	 */
	public void setNotifyURL(String notifyURL) {
		this.notifyURL = notifyURL;
	}
	/**
	 * @return the consentResumeURL
	 */
	public String getConsentResumeURL() {
		return consentResumeURL;
	}
	/**
	 * @param consentResumeURL the consentResumeURL to set
	 */
	public void setConsentResumeURL(String consentResumeURL) {
		this.consentResumeURL = consentResumeURL;
	}
	
}

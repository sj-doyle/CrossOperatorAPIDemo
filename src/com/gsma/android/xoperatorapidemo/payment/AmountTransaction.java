package com.gsma.android.xoperatorapidemo.payment;

import com.gsma.android.xoperatorapidemo.discovery.Link;

public class AmountTransaction {
	/*
	 * {"amountTransaction":{
	 * "transactionOperationStatus":"Reserved",
	 * "clientCorrelator":"8702474bje94bj4dabjbdd5j97c748940863",
	 * "endUserId":"acr:Authorization",
	 * "referenceCode":"ced821cejaeb6j4279jaebaj7c992f7fb7e7",
	 * "paymentAmount":{"chargingInformation":{"amount":"0.09","currency":"EUR","title":"Samurai","description":["Samurai fight for honour"]},"chargingMetaData":{"onBehalfOf":"Hirakari Games Inc","purchaseCategoryCode":"1","channel":"MOBILE_WEB","taxAmount":"0","callbackURL":"http://enum.apiexchange.org/Interop/Notify"}}}}
	 */

	String transactionOperationStatus=null;
	String clientCorrelator=null;
	String endUserId=null;
	String referenceCode=null;
	String resourceURL=null;
	String serverReferenceCode=null;
	String originalServerReferenceCode=null;
	
	PaymentAmount paymentAmount=null;
	CallbackReference callbackReference=null;
	
	Link[] link=null;

	/**
	 * @return the transactionOperationStatus
	 */
	public String getTransactionOperationStatus() {
		return transactionOperationStatus;
	}

	/**
	 * @param transactionOperationStatus the transactionOperationStatus to set
	 */
	public void setTransactionOperationStatus(String transactionOperationStatus) {
		this.transactionOperationStatus = transactionOperationStatus;
	}

	/**
	 * @return the clientCorrelator
	 */
	public String getClientCorrelator() {
		return clientCorrelator;
	}

	/**
	 * @param clientCorrelator the clientCorrelator to set
	 */
	public void setClientCorrelator(String clientCorrelator) {
		this.clientCorrelator = clientCorrelator;
	}

	/**
	 * @return the endUserId
	 */
	public String getEndUserId() {
		return endUserId;
	}

	/**
	 * @param endUserId the endUserId to set
	 */
	public void setEndUserId(String endUserId) {
		this.endUserId = endUserId;
	}

	/**
	 * @return the referenceCode
	 */
	public String getReferenceCode() {
		return referenceCode;
	}

	/**
	 * @param referenceCode the referenceCode to set
	 */
	public void setReferenceCode(String referenceCode) {
		this.referenceCode = referenceCode;
	}

	/**
	 * @return the paymentAmount
	 */
	public PaymentAmount getPaymentAmount() {
		return paymentAmount;
	}

	/**
	 * @param paymentAmount the paymentAmount to set
	 */
	public void setPaymentAmount(PaymentAmount paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	/**
	 * @return the resourceURL
	 */
	public String getResourceURL() {
		return resourceURL;
	}

	/**
	 * @param resourceURL the resourceURL to set
	 */
	public void setResourceURL(String resourceURL) {
		this.resourceURL = resourceURL;
	}

	/**
	 * @return the serverReferenceCode
	 */
	public String getServerReferenceCode() {
		return serverReferenceCode;
	}

	/**
	 * @param serverReferenceCode the serverReferenceCode to set
	 */
	public void setServerReferenceCode(String serverReferenceCode) {
		this.serverReferenceCode = serverReferenceCode;
	}

	/**
	 * @return the callbackReference
	 */
	public CallbackReference getCallbackReference() {
		return callbackReference;
	}

	/**
	 * @param callbackReference the callbackReference to set
	 */
	public void setCallbackReference(CallbackReference callbackReference) {
		this.callbackReference = callbackReference;
	}

	/**
	 * @return the link
	 */
	public Link[] getLink() {
		return link;
	}

	/**
	 * @param link the link to set
	 */
	public void setLink(Link[] link) {
		this.link = link;
	}

	/**
	 * @return the originalServerReferenceCode
	 */
	public String getOriginalServerReferenceCode() {
		return originalServerReferenceCode;
	}

	/**
	 * @param originalServerReferenceCode the originalServerReferenceCode to set
	 */
	public void setOriginalServerReferenceCode(String originalServerReferenceCode) {
		this.originalServerReferenceCode = originalServerReferenceCode;
	}

	
}

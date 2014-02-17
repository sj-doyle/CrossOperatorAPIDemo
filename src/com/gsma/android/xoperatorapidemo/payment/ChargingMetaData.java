package com.gsma.android.xoperatorapidemo.payment;

public class ChargingMetaData {

	/*
	 * 	 * "paymentAmount":{"chargingInformation":{"amount":"0.09","currency":"EUR","title":"Samurai","description":["Samurai fight for honour"]},
	 * "chargingMetaData":{"onBehalfOf":"Hirakari Games Inc","purchaseCategoryCode":"1","channel":"MOBILE_WEB",
	 * "taxAmount":"0","callbackURL":"http://enum.apiexchange.org/Interop/Notify"}}}}

	 */

	String onBehalfOf=null;
	String purchaseCategoryCode=null;
	String channel=null;
	String taxAmount=null;
	
	/**
	 * @return the onBehalfOf
	 */
	public String getOnBehalfOf() {
		return onBehalfOf;
	}
	/**
	 * @param onBehalfOf the onBehalfOf to set
	 */
	public void setOnBehalfOf(String onBehalfOf) {
		this.onBehalfOf = onBehalfOf;
	}
	/**
	 * @return the purchaseCategoryCode
	 */
	public String getPurchaseCategoryCode() {
		return purchaseCategoryCode;
	}
	/**
	 * @param purchaseCategoryCode the purchaseCategoryCode to set
	 */
	public void setPurchaseCategoryCode(String purchaseCategoryCode) {
		this.purchaseCategoryCode = purchaseCategoryCode;
	}
	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}
	/**
	 * @return the taxAmount
	 */
	public String getTaxAmount() {
		return taxAmount;
	}
	/**
	 * @param taxAmount the taxAmount to set
	 */
	public void setTaxAmount(String taxAmount) {
		this.taxAmount = taxAmount;
	}
	
}

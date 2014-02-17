package com.gsma.android.xoperatorapidemo.payment;

public class PaymentAmount {

	/*
	 * 	 * "paymentAmount":{"chargingInformation":{"amount":"0.09","currency":"EUR","title":"Samurai","description":["Samurai fight for honour"]},"chargingMetaData":{"onBehalfOf":"Hirakari Games Inc","purchaseCategoryCode":"1","channel":"MOBILE_WEB","taxAmount":"0","callbackURL":"http://enum.apiexchange.org/Interop/Notify"}}}}

	 */
	
	ChargingInformation chargingInformation=null;
	/**
	 * @return the chargingInformation
	 */
	public ChargingInformation getChargingInformation() {
		return chargingInformation;
	}
	/**
	 * @param chargingAmount the chargingAmount to set
	 */
	public void setChargingInformation(ChargingInformation chargingInformation) {
		this.chargingInformation = chargingInformation;
	}

	ChargingMetaData chargingMetaData=null;
	/**
	 * @return the chargingMetaData
	 */
	public ChargingMetaData getChargingMetaData() {
		return chargingMetaData;
	}
	/**
	 * @param chargingMetaData the chargingMetaData to set
	 */
	public void setChargingMetaData(ChargingMetaData chargingMetaData) {
		this.chargingMetaData = chargingMetaData;
	}
	
	String totalAmountCharged=null;
	
	
	/**
	 * @return the totalAmountCharged
	 */
	public String getTotalAmountCharged() {
		return totalAmountCharged;
	}
	/**
	 * @param totalAmountCharged the totalAmountCharged to set
	 */
	public void setTotalAmountCharged(String totalAmountCharged) {
		this.totalAmountCharged = totalAmountCharged;
	}

	String amountReserved=null;
	/**
	 * @return the amountReserved
	 */
	public String getAmountReserved() {
		return amountReserved;
	}
	/**
	 * @param amountReserved the amountReserved to set
	 */
	public void setAmountReserved(String amountReserved) {
		this.amountReserved = amountReserved;
	}
	
	
}

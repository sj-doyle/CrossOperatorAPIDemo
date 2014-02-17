package com.gsma.android.xoperatorapidemo.payment;

public class ChargingInformation {

	String amount=null;
	String code=null;
	String currency=null;
	String[] title=null;
	String[] description=null;
	String[] locale=null;
	
	/**
	 * @return the amount
	 */
	public String getAmount() {
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}
	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	/**
	 * @return the title
	 */
	public String[] getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String[] title) {
		this.title = title;
	}
	/**
	 * @return the description
	 */
	public String[] getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String[] description) {
		this.description = description;
	}
	/**
	 * @return the locale
	 */
	public String[] getLocale() {
		return locale;
	}
	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String[] locale) {
		this.locale = locale;
	}

	
}

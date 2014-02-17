package com.gsma.android.xoperatorapidemo.identity;

import java.io.Serializable;

public class UserinfoAddress implements Serializable {
	private static final long serialVersionUID = -7856764248641080794L;
	
	String formatted;
	String street_address;
	String locality;
	String region;
	String postal_code;
	String country;
	
	/**
	 * @return the formatted
	 */
	public String getFormatted() {
		return formatted;
	}
	/**
	 * @param formatted the formatted to set
	 */
	public void setFormatted(String formatted) {
		this.formatted = formatted;
	}
	/**
	 * @return the street_address
	 */
	public String getStreet_address() {
		return street_address;
	}
	/**
	 * @param street_address the street_address to set
	 */
	public void setStreet_address(String street_address) {
		this.street_address = street_address;
	}
	/**
	 * @return the locality
	 */
	public String getLocality() {
		return locality;
	}
	/**
	 * @param locality the locality to set
	 */
	public void setLocality(String locality) {
		this.locality = locality;
	}
	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}
	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}
	/**
	 * @return the postal_code
	 */
	public String getPostal_code() {
		return postal_code;
	}
	/**
	 * @param postal_code the postal_code to set
	 */
	public void setPostal_code(String postal_code) {
		this.postal_code = postal_code;
	}
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
}

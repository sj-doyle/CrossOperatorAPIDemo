package com.gsma.android.xoperatorapidemo.discovery;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Response implements Serializable {
	private static final long serialVersionUID = 6597100283176242379L;
	private static final String TAG = "Response";

	
	/*
	 * {"ttl":"1385519464","response":{
	 * 	"client_id":"att-dev1-payment-app-test-key",
	 * "subscriber_operator":"att",
	 * "country":"US",
	 * "currency":"USD","apis":{"payment":{"link":[{"href":"https://att-ex-test.apigee.net/v1/payment/acr:Authorization/transactions/amount","rel":"transactionstatus"},{"href":"http://att-ex-test.apigee.net/v1/payment/acr:Authorization/transactions/amountReservation","rel":"reserve"},{"href":"http://att-ex-test.apigee.net/payment/v1_1/acr:de993519-7922-4a03-8204-4303f941f853/transactions/amount","rel":"uri"},{"href":"GET,POST-/payment/acr:Authorization/transactions/amount","rel":"scope"}]}}}}
	 */

	public Response () {
		
	}
	
	public Response (JSONObject jsonObject) throws JSONException {
		if (jsonObject!=null) {
			this.client_id=jsonObject.getString("client_id");
			Log.d(TAG, "client_id = "+client_id);
			this.client_secret=jsonObject.getString("client_secret");
			Log.d(TAG, "client_secret = "+client_secret);
			this.subscriber_operator=jsonObject.getString("subscriber_operator");
			Log.d(TAG, "subscriber_operator = "+subscriber_operator);
			this.country=jsonObject.getString("country");
			Log.d(TAG, "country = "+country);
			this.currency=jsonObject.getString("currency");
			Log.d(TAG, "currency = "+currency);
			
			JSONObject apilist = jsonObject.getJSONObject("apis");
			@SuppressWarnings("rawtypes")
			Iterator iter = apilist.keys();
			apis=new HashMap<String, Api>();
		    while(iter.hasNext()){
		        String key = (String)iter.next();
		        Log.d(TAG, "key = "+key);
		        Api api = new Api(apilist.getJSONObject(key));
		        apis.put(key,api);
		    }
		}
	}
	
	String client_id=null;
	public String getClient_id() { return this.client_id; }
	public void setClient_id(String client_id) { this.client_id=client_id; }
	
	String client_secret=null;
	public String getClient_secret() { return this.client_secret; }
	public void setClient_secret(String client_secret) { this.client_secret=client_secret; }
	
	String subscriber_operator=null;
	public String getSubscriber_operator() { return this.subscriber_operator; }
	public void setSubscriber_operator(String subscriber_operator) { this.subscriber_operator=subscriber_operator; }
	
	String country=null;
	public String getCountry() { return this.country; }
	public void setCountry(String country) { this.country=country; }
	
	String currency=null;
	public String getCurrency() { return this.currency; }
	public void setCurrency(String currency) { this.currency=currency; }
	
	HashMap<String, Api> apis=null;
	public HashMap<String, Api> getApis() { return this.apis; }
	public Api getApi(String name) { return (name!=null&&apis!=null)?apis.get(name):null; }

	public JSONObject toObject() throws JSONException {
		JSONObject obj=new JSONObject();
		obj.put("client_id", client_id);
		obj.put("client_secret", client_secret);
		obj.put("subscriber_operator", subscriber_operator);
		obj.put("country", country);
		obj.put("currency", currency);
		if (apis!=null) obj.put("apis", apis);
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
	

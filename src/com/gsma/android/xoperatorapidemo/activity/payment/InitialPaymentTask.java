package com.gsma.android.xoperatorapidemo.activity.payment;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.discovery.LinkConstants;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransaction;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.AmountTransaction;
import com.gsma.android.xoperatorapidemo.payment.AmountTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.CallbackReference;
import com.gsma.android.xoperatorapidemo.payment.ChargingInformation;
import com.gsma.android.xoperatorapidemo.payment.ChargingMetaData;
import com.gsma.android.xoperatorapidemo.payment.PaymentAmount;
import com.gsma.android.xoperatorapidemo.payment.PaymentStates;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;
import com.gsma.android.xoperatorapidemo.utils.JsonUtils;

class InitialPaymentTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "InitialPaymentTask";

	Activity invokingActivity;
	
	String amount=null;
	DiscoveryData discoveryData=null;
	String method=null;

	public InitialPaymentTask(Activity invokingActivity, String amount, DiscoveryData discoveryData, String method) {
		this.invokingActivity=invokingActivity;
		this.amount=amount;
		this.discoveryData=discoveryData;
		this.method=method;
	}

	/*
	 * the doInBackground function does the actual background processing
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject response=null;
		String jsonData=null;
		
		UUID clientCorrelator=UUID.randomUUID();
		UUID referenceCode=UUID.randomUUID();
		PaymentAmount paymentAmount=new PaymentAmount();
		ChargingInformation chargingInformation=new ChargingInformation();
		paymentAmount.setChargingInformation(chargingInformation);
		
		chargingInformation.setAmount(amount);
		chargingInformation.setCurrency(discoveryData.getResponse().getCurrency());
		chargingInformation.setTitle(new String[]{"Samurai"});
		chargingInformation.setDescription(new String[]{"Samurai fight for honour"});
		chargingInformation.setLocale(new String[]{"en"});
		
		ChargingMetaData chargingMetaData=new ChargingMetaData();
		paymentAmount.setChargingMetaData(chargingMetaData);
		
		chargingMetaData.setOnBehalfOf("ACME Inc.");
		chargingMetaData.setChannel("wap");
		chargingMetaData.setTaxAmount("0.0");
		chargingMetaData.setPurchaseCategoryCode("1");
		
		CallbackReference callbackReference=new CallbackReference();
		callbackReference.setCallbackData(UUID.randomUUID().toString());
		callbackReference.setConsentResumeURL("gsmademo://paymentConsentComplete");
		
		String endpoint=null;

		try {

			if (PaymentStartActivity.METHOD_1_PHASE.equals(method)) {
				endpoint=discoveryData.getResponse().getApi("payment").getHref("charge");
				
				AmountTransactionWrapper amountTransactionWrapper=new AmountTransactionWrapper();
				
				AmountTransaction amountTransaction=new AmountTransaction();
				amountTransactionWrapper.setAmountTransaction(amountTransaction);
				
				amountTransaction.setTransactionOperationStatus(PaymentStates.CHARGED);
				amountTransaction.setClientCorrelator(clientCorrelator.toString());
				amountTransaction.setEndUserId("acr:Authorization");
				amountTransaction.setReferenceCode(referenceCode.toString());
				
				amountTransaction.setPaymentAmount(paymentAmount);
				
				amountTransaction.setCallbackReference(callbackReference);
		
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
				jsonData=objectMapper.writeValueAsString(amountTransactionWrapper);
			} else {
				endpoint=discoveryData.getResponse().getApi("payment").getHref("reserve");
				
				AmountReservationTransactionWrapper amountReservationTransactionWrapper=new AmountReservationTransactionWrapper();
				
				AmountReservationTransaction amountReservationTransaction=new AmountReservationTransaction();
				amountReservationTransactionWrapper.setAmountReservationTransaction(amountReservationTransaction);
				
				amountReservationTransaction.setTransactionOperationStatus(PaymentStates.RESERVED);
				amountReservationTransaction.setClientCorrelator(clientCorrelator.toString());
				amountReservationTransaction.setEndUserId("acr:Authorization");
				amountReservationTransaction.setReferenceCode(referenceCode.toString());
				
				amountReservationTransaction.setPaymentAmount(paymentAmount);
				
				amountReservationTransaction.setCallbackReference(callbackReference);
				
				amountReservationTransaction.setReferenceSequence(1);
		
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
				jsonData=objectMapper.writeValueAsString(amountReservationTransactionWrapper);
			}
			
			Log.d(TAG, "HTTP Post to "+endpoint);
			Log.d(TAG, "ClientID: "+discoveryData.getResponse().getClient_id());
			Log.d(TAG, "ClientSecret: "+discoveryData.getResponse().getClient_secret());
			Log.d(TAG, "Data: "+jsonData);
			
			HttpClient httpClient = HttpUtils.getHttpClient(endpoint,
					discoveryData.getResponse().getClient_id(), discoveryData.getResponse().getClient_secret());
	
			HttpPost httpRequest = new HttpPost(endpoint);
			httpRequest.addHeader("Content-Type", "application/json");
			httpRequest.addHeader("Accept", "application/json");
	
			HttpParams httpParams = httpRequest.getParams();
			httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
			httpRequest.setParams(httpParams);
			
			StringEntity se = new StringEntity(jsonData);
			
			Log.d(TAG, "Sending request to "+endpoint);
			
			httpRequest.setEntity(se);
			
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			
			int statusCode=httpResponse.getStatusLine().getStatusCode();
			
			Log.d(TAG, "Response status code = "+httpResponse.getStatusLine().getStatusCode());
			
			String jsonResponse=HttpUtils.getContentsFromHttpResponse(httpResponse);
			
	        Log.d(TAG, "Read "+jsonResponse);
	        
	        if (statusCode==HttpStatus.SC_CREATED || statusCode==HttpStatus.SC_ACCEPTED) {
	            ObjectMapper mapper=new ObjectMapper();
	            if (PaymentStartActivity.METHOD_1_PHASE.equals(method)) {
		        	AmountTransactionWrapper amountTransactionResponseWrapper=mapper.readValue(jsonResponse, AmountTransactionWrapper.class);
		        	Log.d(TAG, "amountTransactionResponseWrapper="+amountTransactionResponseWrapper);
		        	if (amountTransactionResponseWrapper!=null) {
		        		Log.d(TAG, "amountTransaction="+amountTransactionResponseWrapper.getAmountTransaction());
		        	}
		        	if (amountTransactionResponseWrapper!=null && amountTransactionResponseWrapper.getAmountTransaction()!=null) {
		        		AmountTransaction amountTransactionResponse=amountTransactionResponseWrapper.getAmountTransaction();
		            	if (statusCode==HttpStatus.SC_CREATED && amountTransactionResponse.getTransactionOperationStatus().equalsIgnoreCase(PaymentStates.CHARGED)) {
		                    Log.d(TAG, "charged");
		                    //TODO - not handled here - consent is expected
		            		
		            	} else if (statusCode==HttpStatus.SC_ACCEPTED && amountTransactionResponse.getTransactionOperationStatus().equalsIgnoreCase(PaymentStates.PROCESSING)) {
		            		Log.d(TAG, "processing - getting consent URL");
		                    
		                	String consentUrl=JsonUtils.getLinkArrayHref(amountTransactionResponse.getLink(), LinkConstants.CONSENT);
		                	if (consentUrl!=null && (consentUrl.startsWith("http://") || consentUrl.startsWith("https://"))) {
		                		Log.d(TAG, "opening consent "+consentUrl);
		                		Intent openBrowser = new Intent(Intent.ACTION_VIEW);
	            				openBrowser.setData(Uri.parse(consentUrl));
	            				invokingActivity.startActivity(openBrowser);
		                	}
		            		
		            	}
		        	}
		            
	        	} else { // 2 phase
		        	AmountReservationTransactionWrapper amountReservationTransactionResponseWrapper=mapper.readValue(jsonResponse, AmountReservationTransactionWrapper.class);
		        	Log.d(TAG, "amountReservationTransactionResponseWrapper="+amountReservationTransactionResponseWrapper);
		        	if (amountReservationTransactionResponseWrapper!=null) {
		        		Log.d(TAG, "amountReservationTransaction="+amountReservationTransactionResponseWrapper.getAmountReservationTransaction());
		        	}
		        	if (amountReservationTransactionResponseWrapper!=null && amountReservationTransactionResponseWrapper.getAmountReservationTransaction()!=null) {
		        		AmountReservationTransaction amountReservationTransactionResponse=amountReservationTransactionResponseWrapper.getAmountReservationTransaction();
		            	if (statusCode==HttpStatus.SC_CREATED && amountReservationTransactionResponse.getTransactionOperationStatus().equalsIgnoreCase(PaymentStates.RESERVED)) {
		                    Log.d(TAG, "reserved");
		                    //TODO - not handled here - consent is expected
		            		
		            	} else if (statusCode==HttpStatus.SC_ACCEPTED && amountReservationTransactionResponse.getTransactionOperationStatus().equalsIgnoreCase(PaymentStates.PROCESSING)) {
		            		Log.d(TAG, "processing - getting consent URL");
		            		
		                	String consentUrl=JsonUtils.getLinkArrayHref(amountReservationTransactionResponse.getLink(), LinkConstants.CONSENT);
		                	if (consentUrl!=null && (consentUrl.startsWith("http://") || consentUrl.startsWith("https://"))) {
		                		Log.d(TAG, "opening consent "+consentUrl);
		                		Intent openBrowser = new Intent(Intent.ACTION_VIEW);
	            				openBrowser.setData(Uri.parse(consentUrl));
	            				invokingActivity.startActivity(openBrowser);
		                	}
	
		            	}
		        	}
	        		
	        	}
	        }
	        
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return response;
	}

}

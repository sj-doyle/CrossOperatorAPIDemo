package com.gsma.android.xoperatorapidemo.activity.payment;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.MainActivity;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransaction;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.AmountTransaction;
import com.gsma.android.xoperatorapidemo.payment.AmountTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.PaymentStates;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;
import com.gsma.android.xoperatorapidemo.utils.ParameterList;

public class PaymentConsentCompleteActivity extends Activity {
	private static final String TAG = "PaymentConsentCompleteActivity";

	PaymentConsentCompleteActivity paymentConsentCompleteActivityInstance = null;

	String transactionOperationStatus;
	String resourceURL;
	String transactionId;
	
	DiscoveryData discoveryData=null;
	
	TextView paymentConsentCompleteStatusValue=null;
	TextView paymentConsentCompleteTransactionIdValue=null;
	TextView paymentConsentCompleteServerReferenceValue=null;

	Button paymentConsentCompleteQuery=null;
	Button paymentConsentCompleteChargeReservation=null;
	
	public static AmountTransaction amountTransaction=null;
	public static AmountReservationTransaction amountReservationTransaction=null;
	
	boolean payment1Phase=false;
	boolean payment2Phase=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		paymentConsentCompleteActivityInstance = this;
		setContentView(R.layout.activity_payment_consent_complete);
		
		paymentConsentCompleteQuery = (Button) findViewById(R.id.paymentConsentCompleteQuery);
		paymentConsentCompleteChargeReservation = (Button) findViewById(R.id.paymentConsentCompleteChargeReservation);
		
		paymentConsentCompleteStatusValue = (TextView) findViewById(R.id.paymentConsentCompleteStatusValue);
		paymentConsentCompleteTransactionIdValue = (TextView) findViewById(R.id.paymentConsentCompleteTransactionIdValue);
		paymentConsentCompleteServerReferenceValue = (TextView) findViewById(R.id.paymentConsentCompleteServerReferenceValue);

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart called ");

		discoveryData=MainActivity.getDiscoveryData();
		
		paymentConsentCompleteQuery.setVisibility(View.INVISIBLE);
		paymentConsentCompleteChargeReservation.setVisibility(View.INVISIBLE);
		
		Uri data = getIntent().getData(); 
		String queryPart=data.getQuery();
		
		Log.d(TAG, "Received "+queryPart);
		
		ParameterList parameters=ParameterList.getKeyValuesFromUrl(queryPart, 0);
		
		transactionOperationStatus = parameters.getValue("transactionOperationStatus");
		resourceURL = parameters.getValue("resourceURL");
		transactionId = parameters.getValue("transactionId");
		
		Log.d(TAG, "received transactionOperationStatus = " + transactionOperationStatus);
		Log.d(TAG, "received resourceURL = " + resourceURL);
		Log.d(TAG, "received transactionId = " + transactionId);

		paymentConsentCompleteStatusValue.setText(transactionOperationStatus);
		paymentConsentCompleteTransactionIdValue.setText(transactionId);
		
		if (transactionOperationStatus!=null && transactionId!=null) {
			String chargeEndpoint=discoveryData.getResponse().getApi("payment").getHref("charge");
			String reserveEndpoint=discoveryData.getResponse().getApi("payment").getHref("reserve");
			String expectedEndpoint=null;
			if (transactionOperationStatus.equalsIgnoreCase(PaymentStates.CHARGED) && chargeEndpoint!=null) {
				expectedEndpoint=chargeEndpoint+(!chargeEndpoint.endsWith("/")?"/":"")+transactionId;
				Log.d(TAG, "expectedEndpoint="+expectedEndpoint);
				payment1Phase=true;
			} else if (transactionOperationStatus.equalsIgnoreCase(PaymentStates.RESERVED) && reserveEndpoint!=null) {
				expectedEndpoint=reserveEndpoint+(!reserveEndpoint.endsWith("/")?"/":"")+transactionId;
				Log.d(TAG, "expectedEndpoint="+expectedEndpoint);
				payment2Phase=true;
			}
			if (expectedEndpoint==null) {
				//TODO
			} else if (resourceURL==null) {
				resourceURL=expectedEndpoint;
			} else if (!expectedEndpoint.equals(resourceURL)) {
				//TODO
				Log.d(TAG, expectedEndpoint+" <> "+resourceURL);
				resourceURL=expectedEndpoint;
			} 
			if (resourceURL!=null) {
				paymentConsentCompleteQuery.setVisibility(View.VISIBLE);
			}
		}
	}
	
	/*
	 * go back to the main screen
	 */
	public void home(View view) {
		Intent intent = new Intent(paymentConsentCompleteActivityInstance,
				MainActivity.class);
		startActivity(intent);
	}
	
	public void claim(View view) {
		Log.d(TAG, "claim payment");
		if (resourceURL!=null && payment2Phase && transactionOperationStatus.equalsIgnoreCase(PaymentStates.RESERVED) && amountReservationTransaction!=null) {
			AmountReservationTransactionWrapper amountReservationTransactionWrapper=new AmountReservationTransactionWrapper();
			AmountReservationTransaction claim=new AmountReservationTransaction();
			amountReservationTransactionWrapper.setAmountReservationTransaction(claim);
			claim.setPaymentAmount(amountReservationTransaction.getPaymentAmount());
			claim.setReferenceSequence(amountReservationTransaction.getReferenceSequence()+1);
			claim.setEndUserId(amountReservationTransaction.getEndUserId());
			claim.setCallbackReference(amountReservationTransaction.getCallbackReference());
			claim.setTransactionOperationStatus(PaymentStates.CHARGED);
			claim.setReferenceCode(amountReservationTransaction.getReferenceCode());
			claim.setClientCorrelator(null);
			claim.setOriginalServerReferenceCode(amountReservationTransaction.getServerReferenceCode());
			
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
			
			try {
				String jsonData = objectMapper.writeValueAsString(amountReservationTransactionWrapper);
				Log.d(TAG, "HTTP Post to "+resourceURL);
				Log.d(TAG, "ClientID: "+discoveryData.getResponse().getClient_id());
				Log.d(TAG, "ClientSecret: "+discoveryData.getResponse().getClient_secret());
				Log.d(TAG, "Data: "+jsonData);
			
				HttpClient httpClient = HttpUtils.getHttpClient(resourceURL,
						discoveryData.getResponse().getClient_id(), discoveryData.getResponse().getClient_secret());
		
				HttpPost httpRequest = new HttpPost(resourceURL);
				httpRequest.addHeader("Content-Type", "application/json");
				httpRequest.addHeader("Accept", "application/json");
		
				HttpParams httpParams = httpRequest.getParams();
				httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
				httpRequest.setParams(httpParams);
				
				StringEntity se = new StringEntity(jsonData);
				
				httpRequest.setEntity(se);
				
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				
				int statusCode=httpResponse.getStatusLine().getStatusCode();

				Log.d(TAG, "Response status code = "+httpResponse.getStatusLine().getStatusCode());
				
				String jsonResponse=HttpUtils.getContentsFromHttpResponse(httpResponse);
				
		        Log.d(TAG, "Read "+jsonResponse);
					        
		        if (statusCode==HttpStatus.SC_OK) { 
		            ObjectMapper mapper=new ObjectMapper();
		            AmountReservationTransactionWrapper amountReservationTransactionResponseWrapper=
		            		mapper.readValue(jsonResponse, AmountReservationTransactionWrapper.class);
		        	if (amountReservationTransactionResponseWrapper!=null && amountReservationTransactionResponseWrapper.getAmountReservationTransaction()!=null) {
		        		AmountReservationTransaction amountReservationTransactionResponse=amountReservationTransactionResponseWrapper.getAmountReservationTransaction();
		        		paymentConsentCompleteStatusValue.setText(amountReservationTransactionResponse.getTransactionOperationStatus());
		        	}
		        }
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		

		}
	}

	public void query(View view) {
		Log.d(TAG, "query payment");
		if (resourceURL!=null && (resourceURL.startsWith("http://") || resourceURL.startsWith("https://"))) {
			if (payment1Phase) {
				if (PaymentStates.CHARGED.equalsIgnoreCase(transactionOperationStatus)) {
					try {
						HttpClient httpClient = HttpUtils.getHttpClient(resourceURL,
								discoveryData.getResponse().getClient_id(), discoveryData.getResponse().getClient_secret());
			
						HttpGet httpRequest = new HttpGet(resourceURL);
				
						httpRequest.addHeader("Accept", "application/json");
				
						HttpParams httpParams = httpRequest.getParams();
						httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
						httpRequest.setParams(httpParams);
						HttpResponse httpResponse = httpClient.execute(httpRequest);
						
						int statusCode=httpResponse.getStatusLine().getStatusCode();
						
						Log.d(TAG, "queryTransactionStatus completion code="+statusCode);
						
						String jsonResponse=HttpUtils.getContentsFromHttpResponse(httpResponse);
						
				        Log.d(TAG, "Read "+jsonResponse);

				        if (statusCode==HttpStatus.SC_OK) {
				            ObjectMapper mapper=new ObjectMapper();
				        	AmountTransactionWrapper amountTransactionResponseWrapper=mapper.readValue(jsonResponse, AmountTransactionWrapper.class);
				        	if (amountTransactionResponseWrapper!=null && amountTransactionResponseWrapper.getAmountTransaction()!=null) {
				        		amountTransaction=amountTransactionResponseWrapper.getAmountTransaction();
				        		
				        		paymentConsentCompleteServerReferenceValue.setText(amountTransaction.getServerReferenceCode());
				        	}
				        }
				        
					} catch (IOException ioe) {
						
					}
				} // Charge response
				
			} else if (payment2Phase) {
				if (PaymentStates.CHARGED.equalsIgnoreCase(transactionOperationStatus) || PaymentStates.RESERVED.equalsIgnoreCase(transactionOperationStatus)) {
					try {
						HttpClient httpClient = HttpUtils.getHttpClient(resourceURL,
								discoveryData.getResponse().getClient_id(), discoveryData.getResponse().getClient_secret());
			
						HttpGet httpRequest = new HttpGet(resourceURL);
				
						httpRequest.addHeader("Accept", "application/json");
				
						HttpParams httpParams = httpRequest.getParams();
						httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
						httpRequest.setParams(httpParams);
						HttpResponse httpResponse = httpClient.execute(httpRequest);
						
						int statusCode=httpResponse.getStatusLine().getStatusCode();
						
						Log.d(TAG, "queryTransactionStatus completion code="+statusCode);
				
						String jsonResponse=HttpUtils.getContentsFromHttpResponse(httpResponse);
						
				        Log.d(TAG, "Read "+jsonResponse);
						
				        if (statusCode==HttpStatus.SC_OK) {
				            ObjectMapper mapper=new ObjectMapper();
				        	AmountReservationTransactionWrapper amountReservationTransactionResponseWrapper=
				        			mapper.readValue(jsonResponse, AmountReservationTransactionWrapper.class);
				        	if (amountReservationTransactionResponseWrapper!=null && amountReservationTransactionResponseWrapper.getAmountReservationTransaction()!=null) {
				        		amountReservationTransaction=amountReservationTransactionResponseWrapper.getAmountReservationTransaction();
				        		transactionOperationStatus=amountReservationTransaction.getTransactionOperationStatus();
				        		paymentConsentCompleteServerReferenceValue.setText(amountReservationTransaction.getServerReferenceCode());
				        		
								if (transactionOperationStatus!=null && transactionOperationStatus.equalsIgnoreCase(PaymentStates.RESERVED)) {
									paymentConsentCompleteChargeReservation.setVisibility(View.VISIBLE);
								}
				        	}
				        } 
				        
					} catch (IOException ioe) {
						
					}
				} // charged or reserved
			}
		}
	}
	
}

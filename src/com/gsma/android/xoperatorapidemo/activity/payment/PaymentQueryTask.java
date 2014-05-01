package com.gsma.android.xoperatorapidemo.activity.payment;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.map.ObjectMapper;

import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.AmountTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.PaymentStates;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;

class PaymentQueryTask extends AsyncTask<Void, Void, Object> {
	private static final String TAG = "PaymentQueryTask";

	PaymentAbstractActivity invokingActivity;
	
	DiscoveryData discoveryData=null;
	String resourceURL=null;
	boolean payment1Phase=false;
	boolean payment2Phase=false;
	String transactionOperationStatus=null;

	public PaymentQueryTask(PaymentAbstractActivity invokingActivity, DiscoveryData discoveryData, String resourceURL, boolean payment1Phase, boolean payment2Phase, 
					String transactionOperationStatus) {
		this.invokingActivity=invokingActivity;
		this.discoveryData=discoveryData;
		this.resourceURL=resourceURL;
		this.payment1Phase=payment1Phase;
		this.payment2Phase=payment2Phase;
		this.transactionOperationStatus=transactionOperationStatus;
	}

	/*
	 * the doInBackground function does the actual background processing
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Object doInBackground(Void... params) {
		Object response=null;
		
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
				        		response=amountTransactionResponseWrapper.getAmountTransaction();
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
				        		response=amountReservationTransactionResponseWrapper.getAmountReservationTransaction();
				        	}
				        } 
				        
					} catch (IOException ioe) {
						
					}
				} // charged or reserved
			}
		}
		return response;
	}
	
	@Override
	protected void onPostExecute(Object response) {
		invokingActivity.processQueryResponse(response);
	}

}

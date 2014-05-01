package com.gsma.android.xoperatorapidemo.activity.payment;

import java.io.IOException;

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

import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransaction;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.PaymentStates;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;

class ReservationClaimTask extends AsyncTask<Void, Void, AmountReservationTransaction> {
	private static final String TAG = "ReservationClaimTask";

	PaymentAbstractActivity invokingActivity;
	
	DiscoveryData discoveryData=null;
	String resourceURL=null;
	boolean payment2Phase=false;
	String transactionOperationStatus=null;
	AmountReservationTransaction amountReservationTransaction=null;

	public ReservationClaimTask(PaymentAbstractActivity invokingActivity, DiscoveryData discoveryData, String resourceURL, boolean payment2Phase,
					String transactionOperationStatus,
					AmountReservationTransaction amountReservationTransaction) {
		this.invokingActivity=invokingActivity;
		this.discoveryData=discoveryData;
		this.resourceURL=resourceURL;
		this.payment2Phase=payment2Phase;
		this.transactionOperationStatus=transactionOperationStatus;
		this.amountReservationTransaction=amountReservationTransaction;
		
		Log.d(TAG, "invokingActivity="+invokingActivity);
		Log.d(TAG, "discoveryData="+discoveryData);
		Log.d(TAG, "resourceURL="+resourceURL);
		Log.d(TAG, "payment2Phase="+payment2Phase);
		Log.d(TAG, "transactionOperationStatus="+transactionOperationStatus);
		Log.d(TAG, "amountReservationTransaction="+amountReservationTransaction);
	}

	/*
	 * the doInBackground function does the actual background processing
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected AmountReservationTransaction doInBackground(Void... params) {
		AmountReservationTransaction response=null;
		
		Log.d(TAG, "Claiming payment");
		
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
			objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
			
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
		        		response=amountReservationTransactionResponseWrapper.getAmountReservationTransaction();
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
		return response;
	}
	
	@Override
	protected void onPostExecute(AmountReservationTransaction response) {
		invokingActivity.processClaimResponse(response);
	}

}

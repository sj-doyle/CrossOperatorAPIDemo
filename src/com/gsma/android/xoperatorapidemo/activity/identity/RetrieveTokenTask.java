package com.gsma.android.xoperatorapidemo.activity.identity;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.xoperatorapidemo.utils.HttpUtils;
import com.gsma.android.xoperatorapidemo.utils.JsonUtils;

/*
 * this is a background task which initiates the identity process using OpenID Connect
 */
public class RetrieveTokenTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "RetrieveTokenTask";

	String tokenUri; //
	String clientId; //
	String clientSecret; //
	String code; // 
	String redirectUri; //

	OpenIDConnectAbstractActivity initiator;
	
	/*
	 * standard constructor - receives information from MainActivity
	 */
	public RetrieveTokenTask(OpenIDConnectAbstractActivity initiator, String code, String clientId, String clientSecret,
			String tokenUri, String redirectUri) {
		this.initiator = initiator;
		this.code = code;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.tokenUri = tokenUri;
		this.redirectUri = redirectUri;
	}
	
	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject json=null;
		
		Log.d(TAG, "Pausing a short while");
		try {
			Thread.sleep(3000);
		} catch (Exception e) {}
		
		Log.d(TAG, "requestUri="+tokenUri);
		
		HttpPost httpRequest = new HttpPost(tokenUri);
		
		List<NameValuePair> postparams = new ArrayList<NameValuePair>();
		BasicNameValuePair codeParams = new BasicNameValuePair("code", code);
		BasicNameValuePair clientIdParams = new BasicNameValuePair("client_id", clientId);
		BasicNameValuePair grantTypeParams = new BasicNameValuePair("grant_type", "authorization_code");
		BasicNameValuePair redirectUriParams = new BasicNameValuePair("redirect_uri", redirectUri);
		postparams.add(codeParams);
		postparams.add(clientIdParams);
		postparams.add(grantTypeParams);
		postparams.add(redirectUriParams);
		if (clientSecret!=null && clientSecret.trim().length()>0) {
			BasicNameValuePair clientSecretParams = new BasicNameValuePair("client_secret", clientSecret);
			postparams.add(clientSecretParams);
		}
		
		/* add the post parameters as the request body */
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(postparams));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		httpRequest.addHeader("Accept", "application/json");
		httpRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");

		HttpClient httpClient = HttpUtils.getHttpClient();
		HttpParams httpParams = httpRequest.getParams();
		httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
		httpRequest.setParams(httpParams);

		try {
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			
			Log.d(TAG, "Request completed with status="+httpResponse.getStatusLine().getStatusCode());
	
			/*
			 * obtain the headers from the httpResponse. Content-Type and
			 * Location are particularly required
			 */
			HashMap<String, String> headerMap = HttpUtils
					.getHeaders(httpResponse);
			String contentType = headerMap.get("content-type");
			String location = headerMap.get("location");
	
			/*
			 * the status code from the HTTP response is also needed in
			 * processing
			 */
			int statusCode = httpResponse.getStatusLine().getStatusCode();
	
			Log.d(TAG, "status=" + statusCode + " CT=" + contentType + " Loc="
					+ location + " JSON?" + HttpUtils.isJSON(contentType)
					+ " HTML?" + HttpUtils.isHTML(contentType));
			
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream is = httpEntity.getContent();
			
			String responseData=JsonUtils.readString(is);
			Log.d(TAG, "Converting response data "+responseData);
			json=new JSONObject(responseData);
			
		} catch (java.io.IOException ioe) {
			Log.e(TAG, "IOException "+ioe.getMessage());
			json=new JSONObject();
			try {
				json.put("Exception", "IOException");
				json.put("Message", ioe.getMessage());
			} catch (JSONException e) {
			}
		} catch (JSONException je) {
			Log.e(TAG, "JSONException "+je.getMessage());
			json=new JSONObject();
			try {
				json.put("Exception", "JSONException");
				json.put("Message", je.getMessage());
			} catch (JSONException e) {
			}
		}
		
		return json;
	}
	
	@Override
	protected void onPostExecute(JSONObject response) {
		initiator.processTokenResponse(response);
	}

}

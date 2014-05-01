package com.gsma.android.xoperatorapidemo.activity.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import com.gsma.android.xoperatorapidemo.activity.MainActivity;
import com.gsma.android.xoperatorapidemo.activity.SettingsActivity;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.logo.LogoLoaderTask;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;
import com.gsma.android.xoperatorapidemo.utils.JsonUtils;

/*
 * this is a background task which makes an initial connection to the discovery service - it will handle a variety of initial response types
 */
public class PassiveDiscoveryTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "PassiveDiscoveryTask";

	String serviceUri; // the URI of the discovery service
	String consumerKey; // the consumerKey and optional secret of the application - used to
	String consumerSecret; 	// authorize access
	String mcc; // mobile country code of the user's subscription
	String mnc; // mobile network code of the user's subscription
	String sourceIP;
	
	Boolean enableCookies;
	
	Activity invokingActivity;

	/*
	 * standard constructor - receives information from MainActivity
	 */
	public PassiveDiscoveryTask(Activity invokingActivity, String serviceUri, String consumerKey, String consumerSecret,
			String mcc, String mnc, Boolean enableCookies, String sourceIP) {
		this.invokingActivity=invokingActivity;
		this.serviceUri = serviceUri;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.mcc = mcc;
		this.mnc = mnc;
		this.enableCookies = enableCookies;
		this.sourceIP = sourceIP;
	}

	/*
	 * the doInBackground function does the actual background processing
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject errorResponse = null;

		Log.d(TAG, "Started discovery process via " + serviceUri);

		Log.d(TAG, "Using MCC=" + mcc);
		Log.d(TAG, "Using MNC=" + mnc);

		CookieManager cookieManager=CookieManager.getInstance();
		cookieManager.setAcceptCookie(enableCookies!=null?enableCookies.booleanValue():false);
		Log.d(TAG, "Allowing cookies = "+cookieManager.acceptCookie());

		/*
		 * sets up the HTTP request with a redirect_uri parameter - in practice
		 * we're looking for mcc/mnc added to the redirect_uri if this step is necessary
		 */
		String phase1Uri = serviceUri + "?redirect_uri=http://gsma.com/oneapi";

		/*
		 * if there are Mobile Country Code and Mobile Network Code values add
		 * as HTTP headers
		 */
		if (mcc != null && mnc != null) {
			phase1Uri = phase1Uri + "&mcc_mnc="+mcc+"_"+mnc;
		}

		HttpGet httpRequest = new HttpGet(phase1Uri);
		
		httpRequest.addHeader("Accept", "application/json");
		
		if (sourceIP!=null) {
			httpRequest.addHeader("x-source-ip", sourceIP);
		}

		try {

			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

			/*
			 * get an instance of an HttpClient, the helper makes sure HTTP
			 * Basic Authorization uses the consumer Key
			 */
			HttpClient httpClient = HttpUtils.getHttpClient(phase1Uri, consumerKey, consumerSecret);
			HttpParams httpParams = httpRequest.getParams();
			httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
			httpRequest.setParams(httpParams);
			
			/*
			 * send the HTTP POST request and get the response
			 */
			Log.d(TAG, "Making " + httpRequest.getMethod() + " request to "
					+ httpRequest.getURI());
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

			/*
			 * process a HTTP 200 (OK) response
			 */
			if (statusCode == HttpStatus.SC_OK) {
				/*
				 * if the response content type is json this will contain the
				 * endpoint information
				 */
				if (HttpUtils.isJSON(contentType)) {
					/*
					 * obtain the response body (via the InputStream from the
					 * httpResponse)
					 */
					HttpEntity httpEntity = httpResponse.getEntity();
					InputStream is = httpEntity.getContent();
					
					Log.d(TAG, "Converting discovery data");
					DiscoveryData discoveryData=JsonUtils.readDiscoveryData(is);
					
					Log.d(TAG, "Signalling discovery complete");
					MainActivity.updateDiscoveryData(discoveryData);
					
					Log.d(TAG, "Now fetching logos");
					new LogoLoaderTask(invokingActivity, 
							SettingsActivity.getDeveloperOperator().getLogoEndpoint(),
							consumerKey, 
							consumerSecret,
							mcc, mnc, enableCookies, 
							sourceIP, "small").execute();
				}

				/*
				 * HTTP status code 302 is a redirect - there should also be a
				 * location header
				 */
				/*
				 * any HTTP status code 400 or above is an error
				 */
			} else if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
				/*
				 * read the contents of the response body
				 */
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream is = httpEntity.getContent();
				String contents = HttpUtils.getContentsFromInputStream(is);

				/*
				 * if the response content type is JSON return as the error
				 */
				if (HttpUtils.isJSON(contentType)) {
					Object rawJSON = JsonUtils.convertContent(contents,
							contentType);
					if (rawJSON != null && rawJSON instanceof JSONObject) {
						errorResponse = (JSONObject) rawJSON;
					}
				} else {
					/*
					 * non JSON data - just return the HTTP status code
					 */
					errorResponse = JsonUtils.simpleError("HTTP " + statusCode,
							"HTTP " + statusCode);
				}

			} // is this request a redirection?

			/*
			 * convert the various internal error types to displayable errors
			 */
		} catch (UnsupportedEncodingException e) {
			errorResponse = JsonUtils.simpleError(
					"UnsupportedEncodingException",
					"UnsupportedEncodingException - " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.d(TAG, "ClientProtocolException="+e.getMessage());
			errorResponse = JsonUtils.simpleError("ClientProtocolException",
					"ClientProtocolException - " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "IOException="+e.getMessage());
			errorResponse = JsonUtils.simpleError("IOException",
					"IOException - " + e.getMessage());
		} catch (JSONException e) {
			Log.d(TAG, "JSONException="+e.getMessage());
			errorResponse = JsonUtils.simpleError("JSONException",
					"JSONException - " + e.getMessage());
		}

		return errorResponse;
	}

	/*
	 * on completion of this background task either this task has started the
	 * next part of the process or an error has occurred.
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(JSONObject errorResponse) {
		/*
		 * if there is an error display to the end user
		 */
		if (errorResponse != null) {
			/*
			 * extract the error fields
			 */
			String error = JsonUtils.getJSONStringElement(errorResponse,
					"error");
			String errorDescription = JsonUtils.getJSONStringElement(
					errorResponse, "error_description");
			Log.d(TAG, "error=" + error);
			Log.d(TAG, "error_description=" + errorDescription);

			/*
			 * display to the user
			 */
			MainActivity.mainActivityInstance.displayError(error,
					errorDescription);
		}
	}
}

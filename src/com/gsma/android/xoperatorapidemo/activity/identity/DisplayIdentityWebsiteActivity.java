package com.gsma.android.xoperatorapidemo.activity.identity;

import java.util.HashMap;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;
import com.gsma.android.xoperatorapidemo.utils.ParameterList;

@SuppressLint("SetJavaScriptEnabled")
public class DisplayIdentityWebsiteActivity extends Activity {
	private static final String TAG = "DisplayIdentityWebsiteActivity";

	String authUri; //
	String tokenUri; //
	String userinfoUri; //
	String clientId; //
	String clientSecret; //
	String scopes; // 
	String returnUri; // 
	
	String secret;

	Activity initiator;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_identity_webview);
		
		initiator=this;
	}
	
	public void onStart() {
		super.onStart();//
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			/*
			 * Extract the parameters from the bundle provided
			 */
			authUri = extras.getString("authUri");
			tokenUri = extras.getString("tokenUri");
			userinfoUri = extras.getString("userinfoUri");
			clientId = extras.getString("clientId");
			clientSecret = extras.getString("clientSecret");
			scopes = extras.getString("scopes");
			returnUri = extras.getString("returnUri");

			secret=UUID.randomUUID().toString();			

			Log.d(TAG, "authUri = "+authUri);
			Log.d(TAG, "tokenUri = "+tokenUri);
			Log.d(TAG, "userinfoUri = "+userinfoUri);
			Log.d(TAG, "clientId = "+clientId);
			Log.d(TAG, "scopes = "+scopes);
			Log.d(TAG, "returnUri = "+returnUri);
			
			String requestUri=authUri;
			if (authUri.indexOf("?") == -1) {
				requestUri+="?";
			} else if (authUri.indexOf("&") == -1) {
				requestUri+="&";
			}
			
			requestUri+="client_id="+HttpUtils.encodeUriParameter(clientId);
			requestUri+="&scope="+HttpUtils.encodeUriParameter(scopes);
			requestUri+="&redirect_uri="+HttpUtils.encodeUriParameter(returnUri);
			requestUri+="&response_type=code";
			requestUri+="&state="+HttpUtils.encodeUriParameter(secret);

			WebView view = (WebView) findViewById(R.id.identityWebView);
			
			view.setWebViewClient(new WebViewClient() {

				/*
				 * This is a stub - could be extended to handle error situations
				 * by returning to a relevant application screen
				 * 
				 * @see
				 * android.webkit.WebViewClient#onReceivedError(android.webkit
				 * .WebView, int, java.lang.String, java.lang.String)
				 */
				@Override
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					Log.d(TAG, "onReceivedError errorCode=" + errorCode
							+ " description=" + description + " failingUrl="
							+ failingUrl);
				}

				/*
				 * The onPageStarted method is called whenever the WebView
				 * starts to load a new page - by examining the url for a
				 * discovery token we can extract this and move to the next
				 * stage of the process
				 * 
				 * @see
				 * android.webkit.WebViewClient#onPageStarted(android.webkit
				 * .WebView, java.lang.String, android.graphics.Bitmap)
				 */
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					Log.d(TAG, "onPageStarted url=" + url);
					/*
					 * Check to see if the url contains the discovery token
					 * identifier - it could be a url parameter or a page
					 * fragment. The following checks and string manipulations
					 * retrieve the actual discovery token
					 */
					
					if (url != null && url.startsWith(returnUri)) {
						Log.d(TAG, "intercepted return");
						
						ParameterList parameters=ParameterList.getKeyValuesFromUrl(url, 0);
						
						String state=parameters.getValue("state");
						String code=parameters.getValue("code");
						String error=parameters.getValue("error");

						Log.d(TAG, "state = "+state);
						Log.d(TAG, "code = "+code);
						Log.d(TAG, "error = "+error);
						
						if (secret.equalsIgnoreCase(state)) {

							view.stopLoading();

							Intent intent = new Intent(
									initiator,
									AuthorizationCompleteActivity.class);
							intent.putExtra("state", state);
							intent.putExtra("code", code);
							intent.putExtra("error", error);
							intent.putExtra("authUri", authUri);
							intent.putExtra("tokenUri", tokenUri);
							intent.putExtra("userinfoUri", userinfoUri);
							intent.putExtra("clientId", clientId);
							intent.putExtra("clientSecret", clientSecret);
							intent.putExtra("scopes", scopes);
							intent.putExtra("returnUri", returnUri);
							
							startActivity(intent);
						}

					}
				}

			});
			
			/*
			 * enable JavaScript - the discovery web pages are enhanced with
			 * JavaScript
			 */
			WebSettings settings = view.getSettings();
			settings.setJavaScriptEnabled(true);

			/*
			 * load the specified URI along with the authorization header
			 */
			HashMap<String, String> extraheaders = new HashMap<String, String>();
			view.loadUrl(requestUri, extraheaders);
		}
	}

}

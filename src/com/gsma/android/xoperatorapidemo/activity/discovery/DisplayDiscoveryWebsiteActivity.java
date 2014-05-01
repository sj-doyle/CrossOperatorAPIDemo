package com.gsma.android.xoperatorapidemo.activity.discovery;

import java.util.HashMap;

import com.gsma.android.xoperatorapidemo.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/*
 * This class will handle the display of the web pages provided by the Discovery service, when the user
 * is asked to confirm their country and MSISDN.
 * 
 * It will scan for the provision of a discovery token in a resulting redirect and forward to the action
 * using a discovery token to obtain operator endpoints
 */
@SuppressLint("SetJavaScriptEnabled")
public class DisplayDiscoveryWebsiteActivity extends Activity {
	private static final String TAG = "DisplayDiscoveryWebsite";
	
	WebView discoveryWebsiteView;
	View originalView;
	
	boolean finished;

//	Activity invokingActivity;
	String consumerKey;
	String consumerSecret;
	String serviceUri;
	String uri;
	Boolean enableCookies;

	public void onStart() {
		super.onStart();
		
		Bundle extras = getIntent().getExtras();

		consumerKey = extras.getString("consumerKey");
		consumerSecret = extras.getString("consumerSecret");
		serviceUri = extras.getString("serviceUri");
		uri = extras.getString("uri");
		enableCookies = extras.getBoolean("enableCookies");
		
		discoveryWebsiteView=new WebView(this);
		
		final DisplayDiscoveryWebsiteActivity thisActivity=this;
		
		setContentView(discoveryWebsiteView);

		Log.d(TAG, "uri - " + uri);
		Log.d(TAG, "consumerKey = " + consumerKey);
		Log.d(TAG, "serviceUri = " + consumerSecret);
		
		finished=false;

		HashMap<String, String> extraheaders = new HashMap<String, String>();
		extraheaders.put("Accept", "text/html");

		Log.d(TAG, "uri = "+uri);

		CookieManager cookieManager=CookieManager.getInstance();
		cookieManager.setAcceptCookie(enableCookies!=null?enableCookies.booleanValue():false);
		Log.d(TAG, "Allowing cookies = "+cookieManager.acceptCookie());

//		invokingActivity.setContentView(discoveryWebsiteView);

		discoveryWebsiteView.getSettings().setJavaScriptEnabled(true); 
		discoveryWebsiteView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

		/*
		 * Add a handler which will scan for URL changes
		 */
		discoveryWebsiteView.setWebViewClient(new WebViewClient() {

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
				finished=true;
				finish();
//				_invokingActivity.setContentView(originalView);
			}
			
			@Override
	        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
	            super.onReceivedSslError(view, handler, error);
	           //Error happens here and returns an empty page.
	            Log.d(TAG, "onReceivedSslError");
	            finished=true;
	            finish();
//				_invokingActivity.setContentView(originalView);
	        }

			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d(TAG, "onPageFinished "+url);
	        }
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.d(TAG, "shouldOverrideUrlLoading "+url);
	            view.loadUrl(url);
	            return true;
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
				
				if (url != null && url.indexOf("mcc_mnc") > -1) {
					String[] parts = url.split("mcc_mnc", 2);
					if (parts.length == 2) {
						String mcc_mnc = parts[1].replaceFirst("=",
								"").trim();

						Log.d(TAG, "mcc_mnc = " + mcc_mnc);

						/*
						 * There is no need to load the remainder of this url as
						 * the discovery token was located
						 */
						view.stopLoading();

						/*
						 * With the discovery token this can now be used to
						 * retrieve service endpoints (back to MainActivity)
						 */
						ProcessDiscoveryToken.start(thisActivity, mcc_mnc, consumerKey, serviceUri);
						
						finished=true;
						
						thisActivity.finish();
						
					} // have a discovery token pair

				}
			}

		});

		/*
		 * load the specified URI along with the authorization header
		 */
		
		discoveryWebsiteView.loadUrl(uri, extraheaders);
	}
	
	public void discover(Activity invokingActivity, String uri, String consumerKey, String consumerSecret, String serviceUri, Boolean enableCookies) {

		try {
			Intent intent = new Intent(invokingActivity, this.getClass());
			intent.putExtra("consumerKey", consumerKey);
			intent.putExtra("consumerSecret", consumerSecret);
			intent.putExtra("serviceUri", serviceUri);
			intent.putExtra("uri", uri);
			intent.putExtra("enableCookies", enableCookies);
			
			invokingActivity.startActivity(intent);
		} catch ( ActivityNotFoundException e) {
		    e.printStackTrace();
		}
	}

	public void stopLoading() {
		if (discoveryWebsiteView!=null) {
			discoveryWebsiteView.stopLoading();
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	
	
}

package com.gsma.android.xoperatorapidemo.activity.discovery;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gsma.android.xoperatorapidemo.R;

/*
 * This class will handle the display of the web pages provided by the Discovery service, when the user
 * is asked to confirm their country and MSISDN.
 * 
 * It will scan for the provision of a discovery token in a resulting redirect and forward to the action
 * using a discovery token to obtain operator endpoints
 */
@SuppressLint("SetJavaScriptEnabled")
public class DisplayDiscoveryWebsiteActivity extends Activity {
	private static final String TAG = "DisplayDiscoveryWebsiteActivity";

	String uri = null; // The URI of the page that is being loaded
	String consumerKey = null; // The API credentials of the application
	String serviceUri = null; // The base URI used to convert the discovery
	// token to endpoints
	
	Activity displayDiscoveryWebsiteActivityInstance=null;

	/*
	 * Handle the opening of the Discovery service web pages
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_discovery_website);

		displayDiscoveryWebsiteActivityInstance=this;
		/*
		 * Receive the parameters from the activity (process) which identified
		 * the discovery service web pages are to be displayed
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			/*
			 * Extract the parameters from the bundle provided
			 */
			uri = extras.getString("uri");
			consumerKey = extras.getString("consumerKey");
			serviceUri = extras.getString("serviceUri");
			Boolean enableCookies=extras.getBoolean("enableCookies");
			String sourceIP=extras.getString("sourceIP");

			Log.d(TAG, "uri - " + uri);
			Log.d(TAG, "consumerKey = " + consumerKey);
			Log.d(TAG, "serviceUri = " + serviceUri);

			/*
			 * Convert the consumerKey into HTTP Basic Authorization credentials
			 */
			String usernamePassword = consumerKey + ":";
			byte[] _usernamePassword = usernamePassword.getBytes();
			String base64encoded = new String(Base64.encode(_usernamePassword,
					Base64.DEFAULT));

			/*
			 * Provide the Base64 encoded consumerKey as an HTTP header
			 */
			HashMap<String, String> extraheaders = new HashMap<String, String>();
			extraheaders.put("Authorization", "Basic " + base64encoded);
			
			if (sourceIP!=null) {
				extraheaders.put("x-source-ip", sourceIP);
			}

			/*
			 * Locate the WebView which will open the required discovery service
			 * web page
			 */
			WebView view = (WebView) findViewById(R.id.interimWebView);
			Log.d(TAG, "View = "+view);
			
			CookieManager cookieManager=CookieManager.getInstance();
			cookieManager.setAcceptCookie(enableCookies!=null?enableCookies.booleanValue():false);
			Log.d(TAG, "Allowing cookies = "+cookieManager.acceptCookie());

			/*
			 * Add a handler which will scan for URL changes
			 */
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
							ProcessDiscoveryTokenTask processTask = new ProcessDiscoveryTokenTask(
									displayDiscoveryWebsiteActivityInstance, mcc_mnc, consumerKey, serviceUri);
							processTask.execute();

						} // have a discovery token pair

					}
				}

				/*
				 * In the event that an authorization request is received
				 * provide the auth credentials for the app
				 * 
				 * @see
				 * android.webkit.WebViewClient#onReceivedHttpAuthRequest(android
				 * .webkit.WebView, android.webkit.HttpAuthHandler,
				 * java.lang.String, java.lang.String)
				 */
				@Override
				public void onReceivedHttpAuthRequest(WebView view,
						HttpAuthHandler handler, String host, String realm) {
					Log.d(TAG, "onReceivedHttpAuthRequest");
					handler.proceed(consumerKey, "");
				}
			});

			/*
			 * load the specified URI along with the authorization header
			 */
			view.loadUrl(uri, extraheaders);

			/*
			 * enable JavaScript - the discovery web pages are enhanced with
			 * JavaScript
			 */
			WebSettings settings = view.getSettings();
			settings.setJavaScriptEnabled(true);
		}
	}

}

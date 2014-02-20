package com.gsma.android.xoperatorapidemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.discovery.InitialDiscoveryTask;
import com.gsma.android.xoperatorapidemo.activity.identity.DisplayIdentityWebsiteActivity;
import com.gsma.android.xoperatorapidemo.discovery.DeveloperOperatorSetting;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryDeveloperOperatorSettings;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryServingOperatorSettings;
import com.gsma.android.xoperatorapidemo.discovery.ServingOperatorSetting;
import com.gsma.android.xoperatorapidemo.utils.PhoneState;
import com.gsma.android.xoperatorapidemo.utils.PhoneUtils;
import com.gsma.android.xoperatorapidemo.utils.PreferencesUtils;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	public static MainActivity mainActivityInstance = null;
	static String userAgent = null;
	
//	public static InitiateAuthorization authorization = null;

	/*
	 * Currently selected developer operator/ serving operator
	 */
	private static int developerOperatorIndex=0;
	private static int servingOperatorIndex=0;
	private DeveloperOperatorSetting developerOperator=DiscoveryDeveloperOperatorSettings.getOperator(developerOperatorIndex);
	private ServingOperatorSetting servingOperator=DiscoveryServingOperatorSettings.getOperator(servingOperatorIndex); 
	
	private static DiscoveryData discoveryData=null;	

	/*
	 * has discovery been started - used to avoid making a duplicate request
	 */
	boolean started = false;

	CheckBox mccMncPrompt = null;
	CheckBox promptCookies = null;
	Spinner developerOperatorSpinner = null;
	Spinner servingOperatorSpinner = null;
	TextView startButton = null;
	TextView vMCC = null;
	TextView vMNC = null;
	TextView vStatus = null;

	/*
	 * method called when the application first starts.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mccMncPrompt = (CheckBox) findViewById(R.id.promptMCCMNC);
		promptCookies = (CheckBox) findViewById(R.id.promptCookies);
		developerOperatorSpinner = (Spinner) findViewById(R.id.developerOperatorSpinner);
		servingOperatorSpinner = (Spinner) findViewById(R.id.servingOperator);
		startButton = (TextView) findViewById(R.id.startButton);
		vMCC = (TextView) findViewById(R.id.valueMCC);
		vMNC = (TextView) findViewById(R.id.valueMNC);
		vStatus = (TextView) findViewById(R.id.valueStatus);

		/*
		 * load defaults from preferences file
		 */
		PreferencesUtils.loadPreferences(this);

		/*
		 * save a copy of the current instance - will be needed later
		 */
		mainActivityInstance = this;

		CookieManager.getInstance().setAcceptCookie(true);
		
		/*
		 * create a temporary WebView to obtain the user agent string that would
		 * be used by the inbuilt browser - this may be needed
		 */
		WebView fwv = new WebView(mainActivityInstance);
		WebSettings settings = fwv.getSettings();
		userAgent = settings.getUserAgentString();
		
		
		ArrayAdapter<String> developerOperatorAdapter = new ArrayAdapter<String>(this,   
				android.R.layout.simple_spinner_item, DiscoveryDeveloperOperatorSettings.getOperatorNames());
		developerOperatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
		developerOperatorSpinner.setAdapter(developerOperatorAdapter);
		
		developerOperatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				updateDeveloperOperator(pos);
		    } 
		    public void onNothingSelected(AdapterView<?> adapterView) {
		        return;
		    } 
		}); 
		
		ArrayAdapter<String> servingOperatorAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item, DiscoveryServingOperatorSettings.getOperatorNames());
		servingOperatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
		servingOperatorSpinner.setAdapter(servingOperatorAdapter);

		servingOperatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				updateServingOperator(pos);
		    } 
		    public void onNothingSelected(AdapterView<?> adapterView) {
		        return;
		    } 
		}); 

		/*
		 * Set the MCC/MNC discovery checkbox to use MCC/MNC by default
		 */
		mccMncPrompt.setChecked(true);
		promptCookies.setChecked(true);
	}

	/*
	 * on start or return to the main screen reset the screen so that discovery
	 * can be started
	 */
	@Override
	public void onStart() {
		super.onStart();

		/* Reset the text on the start button */
		startButton.setText(getString(R.string.start));
		
		/* Reset the flag that stops a duplicate discovery request to be made */
		started = false;
		
		/* Update the phone status */
		checkStatus();
	}

	/*
	 * default method to add a menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/*
	 * get the phone status and display relevant indicators on the home screen
	 */
	public void checkStatus() {

		/*
		 * From the standard Android phone state retrieve values of interest for
		 * display/ discovery
		 */
		PhoneState state = PhoneUtils
				.getPhoneState(
						(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE),
						(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

		String mcc = state.getMcc(); // Mobile country code
		String mnc = state.getMnc(); // Mobile network code

		boolean connected = state.isConnected(); // Is the device connected to
		// the Internet
		boolean usingMobileData = state.isUsingMobileData(); // Is the device
		// connected using cellular/mobile data
		boolean roaming = state.isRoaming(); // Is the device roaming
		// (international roaming)

		/*
		 * For test mode the MSISDN, MCC and MNC are set from default
		 * preferences
		 */
		if (!servingOperator.isAutomatic()) {
			mcc=servingOperator.getMcc();
			mnc=servingOperator.getMnc();
		}

		/* Set the displayed MCC value */
		vMCC.setText(mcc);

		/* Set the displayed MNC value */
		vMNC.setText(mnc);

		/* Set the displayed network status */
		String status = getString(R.string.statusDisconnected);
		if (roaming) {
			status = getString(R.string.statusRoaming);
		} else if (usingMobileData) {
			status = getString(R.string.statusOnNet);
		} else if (connected) {
			status = getString(R.string.statusOffNet);
		}
		vStatus.setText(status);
	}

	/*
	 * handles a restart/ refresh of the discovery process
	 */
	public void restart(View view) {
		/* Reset text on start button */
		final TextView startButton = (TextView) findViewById(R.id.startButton);
		startButton.setText(getString(R.string.start));

		/* Reset the discovery process lock */
		started = false;

		/* Update the phone status */
		checkStatus();
	}

	/*
	 * handles a request to initiate OpenID Connect
	 */
	public void demoID(View view) {
		String demoOpenIDConnectClientID=PreferencesUtils.getPreference("DemoOpenIDConnectClientID");
		String demoOpenIDConnectClientSecret=PreferencesUtils.getPreference("DemoOpenIDConnectClientSecret");
		String demoOpenIDConnectAuthEndpoint=PreferencesUtils.getPreference("DemoOpenIDConnectAuthEndpoint");
		String demoOpenIDConnectTokenEndpoint=PreferencesUtils.getPreference("DemoOpenIDConnectTokenEndpoint");
		String demoOpenIDConnectUserinfoEndpoint=PreferencesUtils.getPreference("DemoOpenIDConnectUserinfoEndpoint");
		String demoOpenIDConnectScopes=PreferencesUtils.getPreference("DemoOpenIDConnectScopes");
		Log.d(TAG, "demoOpenIDConnectClientID="+demoOpenIDConnectClientID);
		Log.d(TAG, "demoOpenIDConnectAuthEndpoint="+demoOpenIDConnectAuthEndpoint);
		Log.d(TAG, "demoOpenIDConnectTokenEndpoint="+demoOpenIDConnectTokenEndpoint);
		Log.d(TAG, "demoOpenIDConnectUserinfoEndpoint="+demoOpenIDConnectUserinfoEndpoint);
		Log.d(TAG, "demoOpenIDConnectScopes="+demoOpenIDConnectScopes);
		
		String returnUri="http://oauth2callback.gsma.com/oauth2callback";

		Intent intent = new Intent(
				mainActivityInstance,
				DisplayIdentityWebsiteActivity.class);
		intent.putExtra("authUri", demoOpenIDConnectAuthEndpoint);
		intent.putExtra("tokenUri", demoOpenIDConnectTokenEndpoint);
		intent.putExtra("userinfoUri", demoOpenIDConnectUserinfoEndpoint);
		intent.putExtra("clientId", demoOpenIDConnectClientID);
		intent.putExtra("clientSecret", demoOpenIDConnectClientSecret);
		intent.putExtra("scopes", demoOpenIDConnectScopes);
		intent.putExtra("returnUri", returnUri);
		
		startActivity(intent);
	}
		
	public void updateDeveloperOperator(int index) {
    	developerOperatorIndex=index;
    	developerOperator=DiscoveryDeveloperOperatorSettings.getOperator(index);
		Log.d(TAG, "Selected developer operator "+index+" "+developerOperator.getName());
	}
	
	public static void updateDiscoveryData(DiscoveryData discoveryData) {
		MainActivity.discoveryData=discoveryData;
	}
	
	public static DiscoveryData getDiscoveryData() {
		return MainActivity.discoveryData;
	}
	
	public void updateServingOperator(int index) {
    	servingOperatorIndex=index;
    	servingOperator=DiscoveryServingOperatorSettings.getOperator(index);
		Log.d(TAG, "Selected serving operator "+index+" "+servingOperator.getName());

		String mcc = null; // Mobile country code
		String mnc = null; // Mobile network code

		if (servingOperator.isAutomatic()) {
			PhoneState state = PhoneUtils
					.getPhoneState(
							(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE),
							(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
	
			mcc = state.getMcc(); // Mobile country code
			mnc = state.getMnc(); // Mobile network code
		} else {
			mcc=servingOperator.getMcc();
			mnc=servingOperator.getMnc();
			
		}

		/* Set the displayed MCC value */
		vMCC.setText(mcc);

		/* Set the displayed MNC value */
		vMNC.setText(mnc);
	}

	/*
	 * handler when the user presses the start button - if not currently started
	 * discovery will initiate the discovery process
	 */
	public void startIdentification(View view) {
		/* check that discovery process has not been started already */
		if (!started) {

			Log.d(TAG, "startIdentification called");

			/* get the current phone state */
			PhoneState state = PhoneUtils
					.getPhoneState(
							(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE),
							(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

			/*
			 * retrieve the phone state that assists the discovery process
			 */
			String mcc = state.getMcc();
			String mnc = state.getMnc();

			boolean isConnected = state.isConnected();

			/*
			 * if running in test mode use the defaults from the preferences
			 * file
			 */
			if (!servingOperator.isAutomatic()) {
				mcc = servingOperator.getMcc();
				mnc = servingOperator.getMnc();
			}

			/*
			 * if the user has chosen not to use any of the parameters for
			 * discovery set to null. In practice this is not going to happen in
			 * a real application - but useful to show the different parts of
			 * discovery
			 */
			if (!mccMncPrompt.isChecked()) {
				mcc = null;
				mnc = null;
			}

			Log.d(TAG, "mcc = " + mcc);
			Log.d(TAG, "mnc = " + mnc);
			Log.d(TAG, "isConnected = " + isConnected);
			
			boolean cookiesEnabled=promptCookies.isChecked();

			/*
			 * discovery is only possible of course if the device is connected
			 * to the Internet
			 */
			if (isConnected) {

				/*
				 * discovery has started. Set the text on the start button to
				 * indicate discovery is in progress
				 */
				started = true;
				startButton.setText(getString(R.string.requesting));

				/*
				 * start the background task which makes the first request to
				 * the discovery service - using the available phone state
				 */
				
				new InitialDiscoveryTask(mainActivityInstance, developerOperator.getEndpoint(),
						developerOperator.getAppKey(), developerOperator.getAppSecret(),
						mcc, mnc, userAgent, cookiesEnabled, servingOperator.getIpaddress()).execute();

			} else {
				/*
				 * if not connected display an error to the user
				 */
				Context context = getApplicationContext();
				Toast toast = Toast.makeText(context,
						getString(R.string.notConnectedToInternet),
						Toast.LENGTH_LONG);
				toast.show();
			}

		}
	}

	/*
	 * if there is an error any time during discovery it will be displayed via
	 * the displayError function
	 */
	public void displayError(String error, String errorDescription) {
		Toast toast = Toast.makeText(getBaseContext(), errorDescription,
				Toast.LENGTH_LONG);
		toast.show();
	}
	
	public String getServingOperatorName() {
		return servingOperator.getName();
	}
}

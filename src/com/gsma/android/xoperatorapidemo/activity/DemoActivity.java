package com.gsma.android.xoperatorapidemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.identity.DisplayIdentityWebsiteActivity;
import com.gsma.android.xoperatorapidemo.utils.PreferencesUtils;

public class DemoActivity extends Activity {

	private static final String TAG = "DemoActivity";

	public static DemoActivity demoActivityInstance = null;
	
	/*
	 * method called when the application first starts.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);

		/*
		 * save a copy of the current instance - will be needed later
		 */
		demoActivityInstance = this;
	}

	/*
	 * handles a request to initiate OpenID Connect
	 */
	public void startDTIdentityDemo(View view) {
		String demoOpenIDConnectClientID=PreferencesUtils.getPreference("DemoOpenIDConnectClientID");
		String demoOpenIDConnectClientSecret=PreferencesUtils.getPreference("DemoOpenIDConnectClientSecret");
		String demoOpenIDConnectAuthEndpoint=PreferencesUtils.getPreference("DemoOpenIDConnectAuthEndpoint");
		String demoOpenIDConnectTokenEndpoint=PreferencesUtils.getPreference("DemoOpenIDConnectTokenEndpoint");
		String demoOpenIDConnectUserinfoEndpoint=PreferencesUtils.getPreference("DemoOpenIDConnectUserinfoEndpoint");
		String demoOpenIDConnectScopes=PreferencesUtils.getPreference("DemoOpenIDConnectScopes");
		String demoOpenIDConnectReturnUri=PreferencesUtils.getPreference("DemoOpenIDConnectReturnUri");
		Log.d(TAG, "demoOpenIDConnectClientID="+demoOpenIDConnectClientID);
		Log.d(TAG, "demoOpenIDConnectAuthEndpoint="+demoOpenIDConnectAuthEndpoint);
		Log.d(TAG, "demoOpenIDConnectTokenEndpoint="+demoOpenIDConnectTokenEndpoint);
		Log.d(TAG, "demoOpenIDConnectUserinfoEndpoint="+demoOpenIDConnectUserinfoEndpoint);
		Log.d(TAG, "demoOpenIDConnectScopes="+demoOpenIDConnectScopes);
		Log.d(TAG, "demoOpenIDConnectReturnUri="+demoOpenIDConnectReturnUri);
		
		Intent intent = new Intent(
				this,
				DisplayIdentityWebsiteActivity.class);
		intent.putExtra("authUri", demoOpenIDConnectAuthEndpoint);
		intent.putExtra("tokenUri", demoOpenIDConnectTokenEndpoint);
		intent.putExtra("userinfoUri", demoOpenIDConnectUserinfoEndpoint);
		intent.putExtra("clientId", demoOpenIDConnectClientID);
		intent.putExtra("clientSecret", demoOpenIDConnectClientSecret);
		intent.putExtra("scopes", demoOpenIDConnectScopes);
		intent.putExtra("returnUri", demoOpenIDConnectReturnUri);
		
		startActivity(intent);
	}
		
}

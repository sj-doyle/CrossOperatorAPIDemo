package com.gsma.android.xoperatorapidemo.activity.openid2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.gsma.android.xoperatorapidemo.R;

/*
 * initiate the process of sign-in using the OperatorID API. 
 * the sign-in process is based on the user accessing the operator portal
 * through a browser. It is based on OpenID 2
 * 
 * details on using an external browser are not finalised therefore at the moment
 * this uses a WebView
 */
@SuppressLint("SetJavaScriptEnabled")
public class SignInActivity extends Activity {
	private static final String TAG = "SignInActivity";

	String authenticateuri; // the authenticateuri value returned from the
	// discovery process - this is the endpoint for
	// OperatorID
	String assoc_handle; // association handle returned during 'association'
	// phase

	/*
	 * these values are sent to the OperatorID sign-in process - at completion
	 * the OperatorID service redirects the browser to the returnUri
	 */
	static String pseudoReturnUri = "gsmademo://loginredirect";
	static String pseudoRealm = "gsmademo://loginredirect";

	SignInActivity signInActivityInstance; // saved copy of this instance -
	// needed when sending an intent

	/*
	 * method called when this activity is created - handles the receiving of
	 * endpoint parameters and setting up the WebView
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		signInActivityInstance = this;
		setContentView(R.layout.activity_signin);

		/*
		 * receive the endpoint information (post discovery) and store the
		 * received values
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			authenticateuri = extras.getString("authenticateuri");

			Log.d(TAG, "Starting user identification ... ");
			Log.d(TAG, "authenticateuri = " + authenticateuri);
		}
	}

	/*
	 * when this activity starts
	 * 
	 * @see android.app.Activity#onStart()
	 */
	public void onStart() {
		super.onStart();

		/*
		 * start the sign-in process by forming an association - the background
		 * task will eventually call the locateTo method
		 */
		new SignInAssociationTask(authenticateuri, signInActivityInstance).execute();
	}

	/*
	 * when an association has been formed open the web site to allow the user
	 * to sign in
	 */
	public void locateTo(String signInUri, String assoc_handle) {
		this.assoc_handle = assoc_handle;

		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(signInUri));
		startActivity(browserIntent);
	}

}

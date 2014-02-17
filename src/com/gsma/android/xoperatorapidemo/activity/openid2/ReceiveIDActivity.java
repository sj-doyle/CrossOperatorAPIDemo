package com.gsma.android.xoperatorapidemo.activity.openid2;

import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.MainActivity;
import com.gsma.android.xoperatorapidemo.utils.ParameterList;

public class ReceiveIDActivity extends Activity {
	private static final String TAG = "ReceiveIDActivity";

	ReceiveIDActivity receiveIDActivityInstance = null;

	String rAssocHandle;
	String rIdentity;
	String rMode;
	
	String aCity=null;
	String aCountry=null;
	String aPrefix=null;
	String aFirstName=null;
	String aLastName=null;
	String aLanguage=null;
	String aPostalCode=null;
	String aPostalAddress=null;
	String aEmail=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiveIDActivityInstance = this;
		setContentView(R.layout.activity_signin_complete);
		Log.d(TAG, "onCreate called ");

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Set<String> keyset = extras.keySet();
			if (keyset != null) {
				for (String key : keyset) {
					Object v = extras.get(key);
					if (v != null && v instanceof String) {
						Log.d(TAG, "Received " + key + " = " + v);
					} else {
						Log.d(TAG, "Received " + key + " of type "
								+ v.getClass().getName());
					}
				}
			}
			// rAssocHandle = extras.getString("rAssocHandle");
			// rIdentity = extras.getString("rIdentity");
			// rMode = extras.getString("rMode");
			// Log.d(TAG,"received rAssocHandle = "+rAssocHandle);
			// Log.d(TAG,"received rIdentity = "+rIdentity);
			// Log.d(TAG,"received rMode = "+rMode);
			//
			// TextView signInCompleteHeading=(TextView)
			// findViewById(R.id.signInCompleteHeading);
			// TextView signInCompleteInfo=(TextView)
			// findViewById(R.id.signInCompleteInfo);
			// TextView signInCompleteID=(TextView)
			// findViewById(R.id.signInCompleteID);
			//
			// if ("id_res".equalsIgnoreCase(rMode)) {
			// signInCompleteHeading.setText(getString(R.string.signInCompleteHeading));
			// signInCompleteInfo.setText(getString(R.string.signInCompleteInfo));
			// signInCompleteID.setVisibility(View.VISIBLE);
			// signInCompleteID.setText(rIdentity);
			// } else {
			// signInCompleteHeading.setText(getString(R.string.signInRejectHeading));
			// signInCompleteInfo.setText(getString(R.string.signInRejectInfo));
			// signInCompleteID.setVisibility(View.INVISIBLE);
			// }
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart called ");

		Uri data = getIntent().getData(); 
		String queryPart=data.getQuery();
		
		Log.d(TAG, "Received "+queryPart);
		
		ParameterList parameters=ParameterList.getKeyValuesFromUrl(queryPart);
		
		rAssocHandle = parameters.getValue("openid.assoc_handle");
		rIdentity = parameters.getValue("openid.claimed_id");
		rMode = parameters.getValue("openid.mode");
		
		Log.d(TAG, "received AssocHandle = " + rAssocHandle);
		Log.d(TAG, "received Identity = " + rIdentity);
		Log.d(TAG, "received Mode = " + rMode);

		aCity=fetchAttribute(parameters, "City", "http://openid.net/schema/contact/city/home");
		aCountry=fetchAttribute(parameters, "Country", "http://openid.net/schema/contact/country/home");
		aPrefix=fetchAttribute(parameters, "Prefix", "http://openid.net/schema/namePerson/prefix");
		aFirstName=fetchAttribute(parameters, "First Name", "http://openid.net/schema/namePerson/first");
		aLastName=fetchAttribute(parameters, "Last Name", "http://openid.net/schema/namePerson/last");
		aPostalCode=fetchAttribute(parameters, "Postal Code", "http://openid.net/schema/contact/postalcode/home");
		aPostalAddress=fetchAttribute(parameters, "Postal Address", "http://openid.net/schema/contact/postaladdress/home");
		aEmail=fetchAttribute(parameters, "Email", "http://openid.net/schema/contact/internet/email");
		aLanguage=fetchAttribute(parameters, "Language", "http://openid.net/schema/language/pref");
				
		/*
		 * locate the various screen elements. heading, information text and
		 * the user ID field
		 */
		TextView signInCompleteHeading = (TextView) findViewById(R.id.signInCompleteHeading);
		TextView signInCompleteInfo = (TextView) findViewById(R.id.signInCompleteInfo);
		TextView signInCompleteID = (TextView) findViewById(R.id.signInCompleteID);
		
		TextView signInCompleteEmailLabel = (TextView) findViewById(R.id.signInCompleteEmailLabel);
		TextView signInCompleteEmailValue = (TextView) findViewById(R.id.signInCompleteEmailValue);
		
		signInCompleteEmailLabel.setVisibility(View.INVISIBLE);
		signInCompleteEmailValue.setVisibility(View.INVISIBLE);

		/*
		 * successful identification
		 */
		if ("id_res".equalsIgnoreCase(rMode)) {

			/*
			 * set the fixed text to indicate a successful identification,
			 * and display the user identity
			 */
			signInCompleteHeading
					.setText(getString(R.string.signInCompleteHeading));
			signInCompleteInfo
					.setText(getString(R.string.signInCompleteInfo));
			signInCompleteID.setVisibility(View.VISIBLE);
			signInCompleteID.setText(rIdentity);
			
			if (aEmail!=null && aEmail.trim().length()>0) {
				signInCompleteEmailLabel.setVisibility(View.VISIBLE);
				signInCompleteEmailValue.setVisibility(View.VISIBLE);
				signInCompleteEmailValue.setText(aEmail);
			}
			
		} else {

			/*
			 * set the fixed text to indicate the user declined
			 * identification
			 */
			signInCompleteHeading
					.setText(getString(R.string.signInRejectHeading));
			signInCompleteInfo
					.setText(getString(R.string.signInRejectInfo));
			signInCompleteID.setVisibility(View.INVISIBLE);
		}
	}
	
	/*
	 * Retrieve attribute value and log it
	 */
	private String fetchAttribute(ParameterList parameters, String friendly, String formal) {
		String value=null;
		String[] attributes=parameters.getAttribute(formal);

		if (attributes!=null && attributes.length>0) value=attributes[0];
		
		Log.d(TAG, friendly+" = "+value);
		return value;
	}

	/*
	 * go back to the main screen
	 */
	public void home(View view) {
		Intent intent = new Intent(receiveIDActivityInstance,
				MainActivity.class);
		receiveIDActivityInstance.startActivity(intent);
	}

	/*
	 * go back to the attribute display screen
	 */
	public void displayAttributes(View view) {
		Intent intent = new Intent(receiveIDActivityInstance,
				AttributesDisplayActivity.class);
		
		intent.putExtra("aCity", aCity);
		intent.putExtra("aCountry", aCountry);
		intent.putExtra("aPrefix", aPrefix);
		intent.putExtra("aFirstName", aFirstName);
		intent.putExtra("aLastName", aLastName);
		intent.putExtra("aLanguage", aLanguage);
		intent.putExtra("aPostalCode", aPostalCode);
		intent.putExtra("aPostalAddress", aPostalAddress);
		intent.putExtra("aEmail", aEmail);
		
		receiveIDActivityInstance.startActivity(intent);
	}

}

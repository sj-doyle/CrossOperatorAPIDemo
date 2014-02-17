package com.gsma.android.xoperatorapidemo.activity.openid2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.MainActivity;

/*
 * allow OpenID attributes to be displayed to the end user
 */
public class AttributesDisplayActivity extends Activity {
	
	String aCity=null;
	String aCountry=null;
	String aPrefix=null;
	String aFirstName=null;
	String aLastName=null;
	String aLanguage=null;
	String aPostalCode=null;
	String aPostalAddress=null;
	String aEmail=null;

	AttributesDisplayActivity attributesDisplayInstance; // saved copy of this instance -
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
		attributesDisplayInstance = this;
		setContentView(R.layout.activity_attributes);

		
	}

	/*
	 * when this activity starts
	 * 
	 * @see android.app.Activity#onStart()
	 */
	public void onStart() {
		super.onStart();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			aCity = extras.getString("aCity");
			aCountry = extras.getString("aCountry");
			aPrefix = extras.getString("aPrefix");
			aFirstName = extras.getString("aFirstName");
			aLastName = extras.getString("aLastName");
			aLanguage = extras.getString("aLanguage");
			aPostalCode = extras.getString("aPostalCode");
			aPostalAddress = extras.getString("aPostalAddress");
			aEmail = extras.getString("aEmail");
			
			TextView city = (TextView) findViewById(R.id.attributesValueCity);
			city.setText(aCity);

			TextView country = (TextView) findViewById(R.id.attributesValueCountry);
			country.setText(aCountry);

			TextView prefix = (TextView) findViewById(R.id.attributesValuePrefix);
			prefix.setText(aPrefix);

			TextView firstName = (TextView) findViewById(R.id.attributesValueFirstName);
			firstName.setText(aFirstName);

			TextView lastName = (TextView) findViewById(R.id.attributesValueLastName);
			lastName.setText(aLastName);

			TextView postalCode = (TextView) findViewById(R.id.attributesValuePostalCode);
			postalCode.setText(aPostalCode);

			TextView postalAddress = (TextView) findViewById(R.id.attributesValuePostalAddress);
			postalAddress.setText(aPostalAddress);

			TextView language = (TextView) findViewById(R.id.attributesValueLanguage);
			language.setText(aLanguage);

		}
	}

	/*
	 * go back to the main screen
	 */
	public void home(View view) {
		Intent intent = new Intent(attributesDisplayInstance,
				MainActivity.class);
		attributesDisplayInstance.startActivity(intent);
	}


}

package com.gsma.android.xoperatorapidemo.activity.identity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.MainActivity;

/*
 * initiate the process of sign-in using the OperatorID API. 
 * the sign-in process is based on the user accessing the operator portal
 * through a browser. It is based on OpenID 2
 * 
 * details on using an external browser are not finalised therefore at the moment
 * this uses a WebView
 */
public class AuthorizationCompleteActivity extends OpenIDConnectAbstractActivity {
	private static final String TAG = "AuthorizationCompleteActivity";

	AuthorizationCompleteActivity authorizationCompleteActivityInstance; // saved copy of this instance -
	// needed when sending an intent
	
	String authUri = null;
	String tokenUri = null;
	String userinfoUri = null;
	String clientId = null;
	String clientSecret = null;
	String scopes = null;
	String returnUri = null;
	String state = null;
	String code = null;
	String error = null;

	TextView statusField = null;

	TextView authorizationCompleteEmailValue = null;
	TextView authorizationCompleteSubValue = null;
	TextView authorizationCompleteNameValue = null;
	TextView authorizationCompleteGenderValue = null;
	TextView authorizationCompleteLocaleValue = null;

	/*
	 * method called when this activity is created - handles the receiving of
	 * endpoint parameters and setting up the WebView
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		authorizationCompleteActivityInstance = this;
		setContentView(R.layout.activity_identity_authorization_complete);
		
		statusField = (TextView) findViewById(R.id.authorizationCompleteStatus);

		authorizationCompleteEmailValue = (TextView) findViewById(R.id.authorizationCompleteEmailValue);
		authorizationCompleteSubValue = (TextView) findViewById(R.id.authorizationCompleteSubValue);
		authorizationCompleteNameValue = (TextView) findViewById(R.id.authorizationCompleteNameValue);
		authorizationCompleteGenderValue = (TextView) findViewById(R.id.authorizationCompleteGenderValue);
		authorizationCompleteLocaleValue = (TextView) findViewById(R.id.authorizationCompleteLocaleValue);

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
			state = extras.getString("state");
			code = extras.getString("code");
			error = extras.getString("error");
			
			Log.d(TAG, "handling code="+code+" error="+error);
			
			String statusDescription="unknown";
			boolean authorized=false;
			if (code!=null && code.trim().length()>0) {
				statusDescription="authorized";
				authorized=true;
			} else if (error!=null && error.trim().length()>0) {
				statusDescription="not authorized";
			} 
				
			statusField.setText(statusDescription);
			
			if (authorized) {
				authorizationCompleteEmailValue.setText("retrieving ...");
				authorizationCompleteSubValue.setText("retrieving ...");
				authorizationCompleteNameValue.setText("retrieving ...");
				authorizationCompleteGenderValue.setText("retrieving ...");
				authorizationCompleteLocaleValue.setText("retrieving ...");
				
				RetrieveTokenTask tokenRetriever=new RetrieveTokenTask(authorizationCompleteActivityInstance, code, clientId, clientSecret, tokenUri, returnUri);
				tokenRetriever.execute();
			} else {
				authorizationCompleteEmailValue.setText("not available");
			}
			
 		}
	}

	/*
	 * go back to the main screen
	 */
	public void home(View view) {
		Intent intent = new Intent(authorizationCompleteActivityInstance, MainActivity.class);
		startActivity(intent);
	}

	@Override
	void processTokenResponse(JSONObject response) {
		Log.d(TAG, "processs token");
//		authorizationCompleteEmailValue.setText("not available");
		try {
			String access_token = (String) response.get("access_token");
			if (access_token!=null && access_token.trim().length()>0) {
				statusField.setText("retrieved access token");
			}
			String id_token = (String) response.get("id_token");
			if (id_token!=null && id_token.trim().length()>0) {
				String[] id_token_parts=id_token.split("\\.");
				if (id_token_parts!=null && id_token_parts.length>=2) {
					String idValue=id_token_parts[1];
					byte[] decoded=Base64.decode(idValue, Base64.DEFAULT);
					String dec=new String(decoded);
					Log.d(TAG, "decoded to "+dec);
					JSONObject json=new JSONObject(dec);
					String email=json.getString("email");
					if (email!=null && email.trim().length()>0) {
						authorizationCompleteEmailValue.setText(email);
					}
					String sub=json.getString("sub");
					if (sub!=null && sub.trim().length()>0) {
						authorizationCompleteSubValue.setText(sub);
					}
				}					
			}

			if (access_token!=null && access_token.trim().length()>0) {
				RetrieveUserinfoTask userinfoRetriever=new RetrieveUserinfoTask(authorizationCompleteActivityInstance, access_token, userinfoUri);
				userinfoRetriever.execute();
			}

		} catch (JSONException e) {
		}
		
	}
	
	@Override
	void processUserinfoResponse(JSONObject response) {
		Log.d(TAG, "processs userinfo");
		if (response!=null) {
			TextView authorizationCompleteEmailValue = (TextView) findViewById(R.id.authorizationCompleteEmailValue);
			TextView statusField = (TextView) findViewById(R.id.authorizationCompleteStatus);
			statusField.setText("retrieved userinfo");
			try {
				String email=response.has("email")?response.getString("email"):null;
				String name=response.has("name")?response.getString("name"):"not available";
				String locale=response.has("locale")?response.getString("locale"):"not available";
				String gender=response.has("gender")?response.getString("gender"):"not available";
				
				Log.d(TAG, "email = "+email);
				Log.d(TAG, "name = "+name);
				Log.d(TAG, "locale = "+locale);
				Log.d(TAG, "gender = "+gender);
				
				if (email!=null) {
					authorizationCompleteEmailValue.setText(email);
				}
				if (name!=null) {
					authorizationCompleteNameValue.setText(name);	
				}
				if (locale!=null) {
					authorizationCompleteLocaleValue.setText(locale);	
				}
				if (gender!=null) {
					authorizationCompleteGenderValue.setText(gender);	
				}
			} catch (JSONException e) {
				
			}
		}
	}

}

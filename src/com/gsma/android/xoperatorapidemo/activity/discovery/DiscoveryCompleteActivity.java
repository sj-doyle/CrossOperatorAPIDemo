package com.gsma.android.xoperatorapidemo.activity.discovery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.MainActivity;
import com.gsma.android.xoperatorapidemo.activity.identity.DisplayIdentityWebsiteActivity;
import com.gsma.android.xoperatorapidemo.activity.payment.PaymentStartActivity;
import com.gsma.android.xoperatorapidemo.discovery.Api;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.utils.PreferencesUtils;

/*
 * initiate the process of sign-in using the OperatorID API. 
 * the sign-in process is based on the user accessing the operator portal
 * through a browser. It is based on OpenID 2
 * 
 * details on using an external browser are not finalised therefore at the moment
 * this uses a WebView
 */
@SuppressLint("SetJavaScriptEnabled")
public class DiscoveryCompleteActivity extends Activity {
	private static final String TAG = "DiscoveryCompleteActivity";

	String operatoridauthenticateuri; // the authenticateuri value returned from the
	// discovery process - this is the endpoint for
	// OperatorID
	String operatoridAuth; // the uri to an XML file providing information about the
	// OperatorID service
	String paymentcharge; // the uri to the payment::charge endpoint
	String paymentreserve; // the uri to the payment::reserve endpoint
	String paymenttransactionstatus; // the uri to the payment::transactionstatus endpoint
	
	DiscoveryCompleteActivity discoveryCompleteActivityInstance; // saved copy of this instance -
	// needed when sending an intent

	CheckBox discoveryOperatorID = null;
	CheckBox discoveryPaymentCharge = null;
	CheckBox discoveryPaymentReserve = null;
	Button testOperatorID = null;
	Button testPaymentCharge = null;
	Button testPaymentReserve = null;
	TextView discoveryServingOperatorName = null;

	/*
	 * method called when this activity is created - handles the receiving of
	 * endpoint parameters and setting up the WebView
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		discoveryCompleteActivityInstance = this;
		setContentView(R.layout.activity_discovery_complete);
		discoveryOperatorID = (CheckBox) findViewById(R.id.discoveryOperatorID);
		discoveryPaymentCharge = (CheckBox) findViewById(R.id.discoveryPaymentCharge);
		discoveryPaymentReserve = (CheckBox) findViewById(R.id.discoveryPaymentReserve);
		testOperatorID = (Button) findViewById(R.id.testOperatorID);
		testPaymentCharge = (Button) findViewById(R.id.testPaymentCharge);
		testPaymentReserve = (Button) findViewById(R.id.testPaymentReserve);
		discoveryServingOperatorName = (TextView) findViewById(R.id.discoveryServingOperatorName);
	}

	/*
	 * when this activity starts
	 * 
	 * @see android.app.Activity#onStart()
	 */
	public void onStart() {
		super.onStart();

		DiscoveryData discoveryData=MainActivity.getDiscoveryData();
		
		paymentcharge=null;
		paymentreserve=null;
		paymenttransactionstatus=null;
		operatoridAuth=null;
		
		discoveryServingOperatorName.setText(discoveryData.getResponse().getSubscriber_operator());

		if (discoveryData!=null) {
			Api payment=discoveryData.getResponse()!=null?discoveryData.getResponse().getApi("payment"):null;
			if (payment!=null) {
				paymentcharge=payment.getHref("charge");
				paymentreserve=payment.getHref("reserve");
				paymenttransactionstatus=payment.getHref("transactionstatus");
			}
			Api operatoridEndpoint=discoveryData.getResponse()!=null?discoveryData.getResponse().getApi("operatorid"):null;
			if (operatoridEndpoint!=null) {
				operatoridAuth=operatoridEndpoint.getHref("authorize");
			}
		}

		discoveryOperatorID.setChecked(operatoridAuth!=null);
		discoveryPaymentCharge.setChecked(paymentcharge!=null);
		discoveryPaymentReserve.setChecked(paymentreserve!=null);
		testOperatorID.setVisibility(operatoridAuth!=null?View.VISIBLE:View.INVISIBLE);
		testPaymentCharge.setVisibility(paymentcharge!=null?View.VISIBLE:View.INVISIBLE);
		testPaymentReserve.setVisibility(paymentreserve!=null?View.VISIBLE:View.INVISIBLE);
	}

	public void testOperatorID(View view) {
		Log.d(TAG, "testOperatorID called");
		
		DiscoveryData discoveryData=MainActivity.getDiscoveryData();
		Api operatoridEndpoint=discoveryData.getResponse()!=null?discoveryData.getResponse().getApi("operatorid"):null;
		
		String openIDConnectScopes=PreferencesUtils.getPreference("OpenIDConnectScopes");
		
		String returnUri=PreferencesUtils.getPreference("OpenIDConnectReturnUri");
		
		Intent intent = new Intent(
				discoveryCompleteActivityInstance,
				DisplayIdentityWebsiteActivity.class);
		intent.putExtra("authUri", operatoridEndpoint.getHref("authorize"));
		intent.putExtra("tokenUri", operatoridEndpoint.getHref("token"));
		intent.putExtra("userinfoUri", operatoridEndpoint.getHref("userinfo"));
		intent.putExtra("clientId", discoveryData.getResponse().getClient_id());
		intent.putExtra("clientSecret", discoveryData.getResponse().getClient_secret());
		intent.putExtra("scopes", openIDConnectScopes);
		intent.putExtra("returnUri", returnUri);
		
		discoveryCompleteActivityInstance.startActivity(intent);
	}

	public void testPayment1Phase(View view) {
		Log.d(TAG, "testPaymentCharge called");
		
		Intent intent = new Intent(
				MainActivity.mainActivityInstance,
				PaymentStartActivity.class);
		intent.putExtra("method", PaymentStartActivity.METHOD_1_PHASE);
		MainActivity.mainActivityInstance
				.startActivity(intent);
	}

	public void testPayment2Phase(View view) {
		Log.d(TAG, "testPaymentCharge called");
		
		Intent intent = new Intent(
				MainActivity.mainActivityInstance,
				PaymentStartActivity.class);
		intent.putExtra("method", PaymentStartActivity.METHOD_2_PHASE);
		MainActivity.mainActivityInstance
				.startActivity(intent);
	}

}

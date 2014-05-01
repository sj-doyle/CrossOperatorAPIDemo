package com.gsma.android.xoperatorapidemo.activity.payment;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.MainActivity;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;

public class PaymentStartActivity extends Activity {
	private static final String TAG = "PaymentStartActivity";

	PaymentStartActivity paymentStartActivityInstance = null;
	DiscoveryData discoveryData=null;
	
	public static final String METHOD_1_PHASE="1 Phase Charge";
	public static final String METHOD_2_PHASE="2 Phase Reserve/Charge";

	String method=null; 

	TextView paymentStartCurrencyValue = null;
	TextView paymentStartAmountValue = null;
	TextView paymentStartMethodValue = null;
	
	private static final String amount="0.10";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		paymentStartActivityInstance = this;
		setContentView(R.layout.activity_payment_start);
		paymentStartCurrencyValue = (TextView) findViewById(R.id.paymentStartCurrencyValue);
		paymentStartAmountValue = (TextView) findViewById(R.id.paymentStartAmountValue);
		paymentStartMethodValue = (TextView) findViewById(R.id.paymentStartMethodValue);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart called ");
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			method = extras.getString("method");
		}
		
		discoveryData=MainActivity.getDiscoveryData();

		String currency=discoveryData.getResponse().getCurrency();

		paymentStartCurrencyValue.setText(currency);
		paymentStartAmountValue.setText(amount);
		paymentStartMethodValue.setText(method);
	}
	
	public void payNow(View view) throws JsonGenerationException, JsonMappingException, IOException {
		InitialPaymentTask initialPaymentTask=new InitialPaymentTask(paymentStartActivityInstance, amount, discoveryData, method);
		initialPaymentTask.execute();
	}
	
	/*
	 * go back to the main screen
	 */
	public void home(View view) {
		Intent intent = new Intent(paymentStartActivityInstance,
				MainActivity.class);
		startActivity(intent);
	}
	
}

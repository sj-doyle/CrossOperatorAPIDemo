package com.gsma.android.xoperatorapidemo.activity.payment;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.MainActivity;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.discovery.LinkConstants;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransaction;
import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.AmountTransaction;
import com.gsma.android.xoperatorapidemo.payment.AmountTransactionWrapper;
import com.gsma.android.xoperatorapidemo.payment.CallbackReference;
import com.gsma.android.xoperatorapidemo.payment.ChargingInformation;
import com.gsma.android.xoperatorapidemo.payment.ChargingMetaData;
import com.gsma.android.xoperatorapidemo.payment.PaymentAmount;
import com.gsma.android.xoperatorapidemo.payment.PaymentStates;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;
import com.gsma.android.xoperatorapidemo.utils.JsonUtils;

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

package com.gsma.android.xoperatorapidemo.activity.payment;

import android.app.Activity;

import com.gsma.android.xoperatorapidemo.payment.AmountReservationTransaction;

public abstract class PaymentAbstractActivity extends Activity {
	abstract void processQueryResponse(Object response);
	abstract void processClaimResponse(AmountReservationTransaction response);
}

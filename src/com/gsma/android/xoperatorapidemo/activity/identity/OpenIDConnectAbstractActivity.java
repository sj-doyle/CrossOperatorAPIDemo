package com.gsma.android.xoperatorapidemo.activity.identity;

import org.json.JSONObject;

import android.app.Activity;

public abstract class OpenIDConnectAbstractActivity extends Activity {
	abstract void processTokenResponse(JSONObject response);
	abstract void processUserinfoResponse(JSONObject response);
}

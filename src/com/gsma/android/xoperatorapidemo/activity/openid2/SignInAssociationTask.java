package com.gsma.android.xoperatorapidemo.activity.openid2;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.xoperatorapidemo.utils.OpenIDUtils;
import com.gsma.android.xoperatorapidemo.utils.ParameterList;

/*
 * form an OpenID 2 association with OperatorID server
 */
class SignInAssociationTask extends AsyncTask<Void, Void, String[]> {
	private static final String TAG = "SignInAssociationTask";

	String authenticateuri; // the authenticateuri value returned from the
	// discovery process - this is the endpoint for
	// OperatorID
	SignInActivity signInActivity; // the activity which originated this
									// association request

	/*
	 * constructor receives the discovery parameters and the result activity
	 */
	public SignInAssociationTask(String authenticateuri,
			SignInActivity signInActivity) {
		this.authenticateuri = authenticateuri;
		this.signInActivity = signInActivity;
	}

	/*
	 * this method is doing the actual background processing - making an
	 * association request
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected String[] doInBackground(Void... params) {
		/*
		 * the completion data will be Uri for sign-in and the association
		 * handle
		 */
		String signInUri = null;
		String assoc_handle = null;

		/*
		 * make an association request and get the resulting OpenID 2 parameters
		 */
		try {
			ParameterList associationData = OpenIDUtils
					.associationRequest(authenticateuri);
			if (associationData != null) {

				/* obtain the association handle */
				assoc_handle = associationData.getValue("assoc_handle");

				/*
				 * form the Uri for sign-in based on the baseUri, association
				 * handle, ReturnUri and Realm (as well as other standard OpenID
				 * 2 parameters)
				 */
				signInUri = OpenIDUtils.formOperatorIdUri(authenticateuri,
						assoc_handle, SignInActivity.pseudoReturnUri,
						SignInActivity.pseudoRealm);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String[]{signInUri, assoc_handle};
	}

	/*
	 * on completion send the signInUri and association handle back to the
	 * sign-in activity
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String[] responseData) {
		Log.d(TAG, "signInUri = " + responseData[0]);
		Log.d(TAG, "assoc_handle = " + responseData[1]);

		signInActivity.locateTo(responseData[0], responseData[1]);
	}

}

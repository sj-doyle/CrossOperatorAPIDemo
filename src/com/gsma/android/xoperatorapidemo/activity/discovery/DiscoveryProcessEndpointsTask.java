package com.gsma.android.xoperatorapidemo.activity.discovery;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.gsma.android.xoperatorapidemo.activity.MainActivity;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.utils.HttpUtils;
import com.gsma.android.xoperatorapidemo.utils.JsonUtils;

/*
 * read a set of endpoints from a source InputStream and initiate OperatorID sign-in
 */
class DiscoveryProcessEndpointsTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "DiscoveryProcessEndpointsTask";

	String contentType; // Content-Type header
	HttpResponse httpResponse; // the HttpResponse object
	InputStream inputStream; // InputStream from the HttpResponse
	
	Activity invokingActivity=null;

	/*
	 * constructor requires the contentType header, HttpResponse and InputStream
	 * from the HttpResponse
	 */
	public DiscoveryProcessEndpointsTask(Activity invokingActivity, String contentType,
			HttpResponse httpResponse, InputStream inputStream) {
		this.invokingActivity=invokingActivity;
		this.contentType = contentType;
		this.httpResponse = httpResponse;
		this.inputStream = inputStream;
	}

	/*
	 * the background task firstly identifies this is a JSON response, and
	 * extracts the OperatorID endpoint from the response. it then initiates
	 * OperatorID sign-in
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject errorResponse = null;

		try {
			/*
			 * extract the text content from the HttpResponse body
			 */
			String contents = HttpUtils.getContentsFromInputStream(inputStream);

			Log.d(TAG, "Read " + contents);

			/*
			 * check for the presence of JSON content type
			 */
			if (contentType != null
					&& contentType.toLowerCase().startsWith("application/json")) {
				Log.d(TAG, "Read JSON content");

				/*
				 * convert the text format of JSON
				 */
				Object rawJSON = JsonUtils
						.convertContent(contents, contentType);
				if (rawJSON != null) {
					Log.d(TAG, "Have read the json data");

					/*
					 * there should be a JSONObject at the top level
					 */
					if (rawJSON instanceof JSONObject) {
						JSONObject json = (JSONObject) rawJSON;

						/*
						 * the HTTP status code associated with the response
						 * should be 200 (OK)
						 */
						if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

							DiscoveryData discoveryData=JsonUtils.readDiscoveryData(contents);
							MainActivity.updateDiscoveryData(discoveryData);
							Intent intent = new Intent(invokingActivity,DiscoveryCompleteActivity.class);
							invokingActivity.startActivity(intent);

							/*
							 * an HTTP status code of 400 or over indicates an
							 * error
							 */
						} else if (httpResponse.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
							errorResponse = json;
						}

					} // JSONObject test
				} // have converted into an object
			} // content type is application/json

			/*
			 * convert the various internal error types to displayable errors
			 */
		} catch (IOException e) {
			errorResponse = JsonUtils.simpleError("IOException",
					"IOException - " + e.getMessage());
		} catch (JSONException e) {
			errorResponse = JsonUtils.simpleError("JSONException",
					"JSONException - " + e.getMessage());
		}

		return errorResponse;
	}

	/*
	 * on completion of this background task either this task has started the
	 * next part of the process or an error has occurred.
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(JSONObject errorResponse) {
		/*
		 * if there is an error display to the end user
		 */
		if (errorResponse != null) {
			/*
			 * extract the error fields
			 */
			String error = JsonUtils.getJSONStringElement(errorResponse,
					"error");
			String errorDescription = JsonUtils.getJSONStringElement(
					errorResponse, "error_description");
			Log.d(TAG, "error=" + error);
			Log.d(TAG, "error_description=" + errorDescription);

			/*
			 * display to the user
			 */
			MainActivity.mainActivityInstance.displayError(error,
					errorDescription);
		}
	}
}

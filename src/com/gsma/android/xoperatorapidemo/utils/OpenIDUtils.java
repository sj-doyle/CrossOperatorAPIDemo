package com.gsma.android.xoperatorapidemo.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

/**
 * simple utilities to assist with OpenID 2 processes used in the OperatorID API
 */
public class OpenIDUtils {
	private static final String TAG = "OpenIDUtils";

	/**
	 * make an association request to the specific OperatorID (OpenID 2)
	 * endpoint and return the key value pairs from the response
	 * 
	 * @param endpoint
	 * @return
	 * @throws IOException
	 */
	public static ParameterList associationRequest(String endpoint)
			throws IOException {

		/*
		 * build the string to store the response text from the server
		 */
		ParameterList response = new ParameterList();

		/*
		 * set up the OpenID request parameters - see
		 * http://openid.net/specs/openid-authentication-2_0.html for details
		 */
		ParameterList bodyFields = new ParameterList();
		bodyFields.put("openid.ns", "http://specs.openid.net/auth/2.0");
		bodyFields.put("openid.mode", "associate");
		bodyFields.put("openid.assoc_type", "HMAC-SHA256");
		bodyFields.put("openid.session_type", "no-encryption");
//		bodyFields.put("openid.ns.ui",
//				"http://specs.openid.net/extensions/ui/1.0");
//		bodyFields.put("openid.ui.mode", "x-mobile");

		/*
		 * encode the parameters into a form suitable to send using an HTTP GET
		 * request
		 */

		String paramsString = bodyFields.encodeUriParameters(null);

		try {

			Log.d(TAG, "Sending post request to " + endpoint);
			Log.d(TAG, "params = " + paramsString);

			/*
			 * make an HTTP POST request to the OperatorID service
			 */
			
			HttpURLConnection connection;

			if (endpoint.startsWith("https")) {
				connection = (HttpsURLConnection) (new URL(
						endpoint)).openConnection();
				
			} else {
				connection = (HttpURLConnection) (new URL(
						endpoint)).openConnection();
				
			}
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			/*
			 * send the POST parameters (the OpenID 2 key value pairs from
			 * above)
			 */
			OutputStream os = connection.getOutputStream();
			os.write(paramsString.getBytes("UTF-8"));

			connection.connect();

			Log.d(TAG, "Response code " + connection.getResponseCode());

			/*
			 * a successful response should be indicated by HTTP status code 200
			 * (OK)
			 */
			if (connection.getResponseCode() == 200) {
				// start listening to the stream
				Scanner inStream = new Scanner(connection.getInputStream());
				
				// process the response data - line by line
				while (inStream.hasNextLine()) {
					String line = inStream.nextLine();
					Log.d(TAG, "Read: " + line);
					/*
					 * each line should contain a key value pair, read the next
					 * and store in the result map
					 */
					response.getKeyValuePairFromPlainTextLine(line);
				}
			} else {

				/*
				 * an error occurred - for the moment output to the log
				 */
				Scanner inStream = new Scanner(connection.getInputStream());

				// read the stream line by line and output
				while (inStream.hasNextLine()) {
					String line = inStream.nextLine();
					Log.d(TAG, "Read: " + line);
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * from the provided parameters (as well as standard OpenID 2 parameters)
	 * form a Uri that can be used for the user to sign-in using OperatorID
	 * 
	 * @param baseUri
	 * @param assoc_handle
	 * @param return_url
	 * @param realm
	 * @return
	 */
	public static String formOperatorIdUri(String baseUri, String assoc_handle,
			String return_url, String realm) {

		/*
		 * these are the OpenID 2 parameters that will need to be sent
		 */
		ParameterList bodyFields = new ParameterList();
		bodyFields.put("openid.ns", "http://specs.openid.net/auth/2.0");
		bodyFields.put("openid.mode", "checkid_setup");
		bodyFields.put("openid.claimed_id",
				"http://specs.openid.net/auth/2.0/identifier_select");
		bodyFields.put("openid.identity",
				"http://specs.openid.net/auth/2.0/identifier_select");
		bodyFields.put("openid.assoc_handle", assoc_handle);
		bodyFields.put("openid.realm", realm);
		bodyFields.put("openid.return_to", return_url);
		bodyFields.put("openid.ns.ui",
				"http://specs.openid.net/extensions/ui/1.0");
		bodyFields.put("openid.ui.mode", "x-mobile");
		
		bodyFields.put("openid.ns.ax", "http://openid.net/srv/ax/1.0");
		bodyFields.put("openid.ax.mode", "fetch_request");
		
		bodyFields.put("openid.ax.required", "email,firstname,lastname");
		bodyFields.put("openid.ax.if_available", "prefix,city,postaladdress,postalcode,country,language");
		
		bodyFields.put("openid.ax.type.email", "http://openid.net/schema/contact/internet/email");
		bodyFields.put("openid.ax.type.firstname", "http://openid.net/schema/namePerson/first");
		bodyFields.put("openid.ax.type.lastname", "http://openid.net/schema/namePerson/last");
		
		bodyFields.put("openid.ax.type.prefix", "http://openid.net/schema/namePerson/prefix");
		bodyFields.put("openid.ax.type.city", "http://openid.net/schema/contact/city/home");
		bodyFields.put("openid.ax.type.postaladdress", "http://openid.net/schema/contact/postaladdress/home");
		bodyFields.put("openid.ax.type.postalcode", "http://openid.net/schema/contact/postalcode/home");
		bodyFields.put("openid.ax.type.country", "http://openid.net/schema/contact/country/home");
		bodyFields.put("openid.ax.type.language", "http://openid.net/schema/language/pref");

		/*
		 * form the resultant Uri from the baseUri and all the OpenID parameters
		 * above
		 */
		return bodyFields.encodeUriParameters(baseUri);
	}

}

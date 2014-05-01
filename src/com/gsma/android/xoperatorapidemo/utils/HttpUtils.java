package com.gsma.android.xoperatorapidemo.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

/**
 * simple helper functions for HTTP networking
 */
public class HttpUtils {
	private static final String TAG = "HttpUtils";

	public static final int REGISTRATION_TIMEOUT = 15 * 1000; // connection
																// timeout
																// period (mS)
	public static final int WAIT_TIMEOUT = 30 * 1000; // socket timeout period
														// (mS)

	/**
	 * 
	 * this function adds support for pre-emptive HTTP Authentication for an
	 * HttpClient
	 * 
	 * @param httpClient
	 */
	public static void makeAuthenticationPreemptive(HttpClient httpClient) {
		/*
		 * add a request interceptor which will add authentication credentials
		 * to HTTP requests
		 */
		HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				AuthState authState = (AuthState) context
						.getAttribute(ClientContext.TARGET_AUTH_STATE);
				CredentialsProvider credsProvider = (CredentialsProvider) context
						.getAttribute(ClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost) context
						.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

				if (authState.getAuthScheme() == null) {
					AuthScope authScope = new AuthScope(
							targetHost.getHostName(), targetHost.getPort());
					Credentials creds = credsProvider.getCredentials(authScope);
					if (creds != null) {
						authState.setAuthScheme(new BasicScheme());
						authState.setCredentials(creds);
					}
				}
			}
		};

		((AbstractHttpClient) httpClient).addRequestInterceptor(preemptiveAuth,
				0);
	}

	/**
	 * extract the domain and if specified the port number to create an
	 * authorization scope to use with HTTP Request authorization
	 * 
	 * @param serviceUri
	 * @return
	 */
	public static AuthScope getAuthscopeFor(String serviceUri) {
		int defaultPort = 80;
		if (serviceUri.startsWith("https://")) {
			defaultPort = 443;
		}
		String[] phase1 = serviceUri.split("://", 2);
		String[] phase2 = phase1[1].split("/", 2);
		String[] phase3 = phase2[0].split(":", 2);
		String domain = phase3[0];
		if (phase3.length == 2) {
			try {
				int port = Integer.parseInt(phase3[1]);
				if (port > 0)
					defaultPort = port;
			} catch (NumberFormatException nfe) {
			}
		}
		return new AuthScope(domain, defaultPort);
	}

	/**
	 * convert a set of URL parameters into key/value pairs
	 * 
	 * @param url
	 * @return
	 */
	public static HashMap<String, String> getKeyValuesFromUrl(String url) {
		HashMap<String, String> valueMap = new HashMap<String, String>();
		String[] urlParts = url.split("[\\?\\&]");
		for (int i = 1; i < urlParts.length; i++) {
			String part = urlParts[i];
			String[] kv = part.split("=", 2);
			if (kv.length == 2) {
				String key = kv[0];
				String value = kv[1];
				try {
					key = URLDecoder.decode(key, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
				try {
					value = URLDecoder.decode(value, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}

				Log.d(TAG, "Returned " + key + " = " + value);
				valueMap.put(key, value);
			} else if (kv.length == 1) {
				String key = kv[0];
				try {
					key = URLDecoder.decode(key, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
				valueMap.put(key, null);
			}
		}
		return valueMap;
	}

	/**
	 * create an instance of an HttpClient with default settings
	 * 
	 * @param serviceUri
	 * @param consumerKey
	 * @return
	 */
	public static HttpClient getHttpClient(String serviceUri, String consumerKey) {
		return getHttpClient(serviceUri, consumerKey, null);
	}
	public static HttpClient getHttpClient(String serviceUri, String consumerKey, String consumerSecret) {
		HttpClient httpClient = new DefaultHttpClient();

		HttpParams httpParams = httpClient.getParams();

		/*
		 * set connection and socket timeout intervals
		 */
		HttpConnectionParams.setConnectionTimeout(httpParams,
				REGISTRATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, WAIT_TIMEOUT);
		ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);

		/*
		 * by default it is undesirable to follow redirects as these contain
		 * data needed in the discovery process
		 */
		httpParams.setBooleanParameter("http.protocol.handle-redirects", false);

		/*
		 * set up HTTP Basic Authorization on this HttpClient instance
		 */
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				consumerKey, consumerSecret!=null?consumerSecret:"");

		((AbstractHttpClient) httpClient).getCredentialsProvider()
				.setCredentials(HttpUtils.getAuthscopeFor(serviceUri), creds);

		HttpUtils.makeAuthenticationPreemptive(httpClient);

		return httpClient;
	}
	
	@Deprecated
	public static HttpClient getHttpClient(String serviceUri) {
		return getHttpClient();
	}
	
	public static HttpClient getHttpClient() {
		HttpClient httpClient = new DefaultHttpClient();

		HttpParams httpParams = httpClient.getParams();

		/*
		 * set connection and socket timeout intervals
		 */
		HttpConnectionParams.setConnectionTimeout(httpParams,
				REGISTRATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, WAIT_TIMEOUT);
		ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);

		/*
		 * by default it is undesirable to follow redirects as these contain
		 * data needed in the discovery process
		 */
		httpParams.setBooleanParameter("http.protocol.handle-redirects", false);

		return httpClient;
	}

	/**
	 * read the text contents from an InputStream
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String getContentsFromInputStream(InputStream is)
			throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (is != null) {
			byte[] readbuf = new byte[1024];

			int n;

			while ((n = is.read(readbuf)) >= 0) {
				baos.write(readbuf, 0, n);
			}
		}

		return baos.toString();
	}

	/**
	 * check if the contentType header indicates JSON content
	 * 
	 * @param contentType
	 * @return
	 */
	public static boolean isJSON(String contentType) {
		return (contentType != null && contentType.toLowerCase().startsWith(
				"application/json"));
	}

	/**
	 * check if the contentType header indicates HTML content
	 * 
	 * @param contentType
	 * @return
	 */
	public static boolean isHTML(String contentType) {
		return (contentType != null && contentType.toLowerCase().startsWith(
				"text/html"));
	}

	/**
	 * read the headers of an HTTP response and return. Note that header names
	 * are forced to lower case
	 * 
	 * @param httpResponse
	 * @return
	 */
	public static HashMap<String, String> getHeaders(HttpResponse httpResponse) {
		HashMap<String, String> headerMap = new HashMap<String, String>();

		Header[] responseHeaders = httpResponse.getAllHeaders();
		
		Log.d(TAG, "Status code="+httpResponse.getStatusLine().getStatusCode());

		if (responseHeaders != null) {
			Log.d(TAG, "Response headers");
			for (Header headerName : responseHeaders) {
				String name = headerName.getName();
				String value = headerName.getValue();

				headerMap.put(name.toLowerCase(), value);

				Log.d(TAG, name + " = " + value);
			}
		}

		return headerMap;
	}
	
	public static String encodeUriParameter(String parameter) {
		String encoded="";
		try {
			encoded=URLEncoder.encode(parameter, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}	
		return encoded;
	}
	
	public static String addUriParameter(String baseUrl, String paramName, String paramValue) {
		String url=baseUrl;
		if (paramName!=null && paramValue!=null) {
			if (url.indexOf("?")>-1) {
				url=url+"&"+encodeUriParameter(paramName)+"="+encodeUriParameter(paramValue);
			} else {
				url=url+"?"+encodeUriParameter(paramName)+"="+encodeUriParameter(paramValue);
			}
		}
		return url;
	}
	
	public static String getContentsFromHttpResponse(HttpResponse httpResponse) throws IOException {
		InputStream inputStream = httpResponse.getEntity().getContent();
		StringBuffer jsonResponse=new StringBuffer();
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = null;
        while((line = bufferedReader.readLine()) != null) {
            jsonResponse.append(line);
        }
        inputStream.close();
        return jsonResponse.toString();
	}

}

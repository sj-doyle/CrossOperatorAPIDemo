package com.gsma.android.xoperatorapidemo.activity;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gsma.android.xoperatorapidemo.R;
import com.gsma.android.xoperatorapidemo.activity.discovery.ActiveDiscoveryTask;
import com.gsma.android.xoperatorapidemo.activity.discovery.PassiveDiscoveryTask;
import com.gsma.android.xoperatorapidemo.activity.identity.DisplayIdentityWebsiteActivity;
import com.gsma.android.xoperatorapidemo.activity.payment.PaymentStartActivity;
import com.gsma.android.xoperatorapidemo.discovery.Api;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryData;
import com.gsma.android.xoperatorapidemo.discovery.DiscoveryStartupSettings;
import com.gsma.android.xoperatorapidemo.discovery.Response;
import com.gsma.android.xoperatorapidemo.logo.LogoCache;
import com.gsma.android.xoperatorapidemo.logo.LogoLoaderTask;
import com.gsma.android.xoperatorapidemo.utils.PhoneState;
import com.gsma.android.xoperatorapidemo.utils.PhoneUtils;
import com.gsma.android.xoperatorapidemo.utils.PreferencesUtils;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	public static MainActivity mainActivityInstance = null;
		
	private static DiscoveryData discoveryData=null;	
	
	public static final int DISCOVERY_COMPLETE=1;
	public static final int SETTINGS_COMPLETE=2;
	public static final int LOGOS_UPDATED=100;

	/*
	 * has discovery been started - used to avoid making a duplicate request
	 */
	boolean started = false;
	boolean discovered = false;
	static boolean justDiscovered = false;

	Button discoveryButton = null;
	TextView vMCC = null;
	TextView vMNC = null;
	TextView vStatus = null;
	TextView vDiscoveryStatus = null;
	
	Button startOperatorId = null;
	Button startPayment1 = null;
	Button startPayment2 = null;
	
	private static SharedPreferences  mPrefs=null;

	static Handler discoveryHandler = null;
	static Handler logoUpdateHandler = null;
	
	PassiveDiscoveryTask passiveDiscoveryTask = null;
	ActiveDiscoveryTask initialDiscoveryTask = null;

	/*
	 * method called when the application first starts.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.d(TAG, "onCreate called");

		vMCC = (TextView) findViewById(R.id.valueMCC);
		vMNC = (TextView) findViewById(R.id.valueMNC);
		vStatus = (TextView) findViewById(R.id.valueStatus);
		vDiscoveryStatus = (TextView) findViewById(R.id.valueDiscoveryStatus);
		
		discoveryButton = (Button) findViewById(R.id.discoveryButton);
		startOperatorId = (Button) findViewById(R.id.startOperatorId);
		startPayment1 = (Button) findViewById(R.id.startPayment1);
		startPayment2 = (Button) findViewById(R.id.startPayment2);

		/*
		 * load defaults from preferences file
		 */
		PreferencesUtils.loadPreferences(this);
		
		/*
		 * load settings from private local storage
		 */
		SettingsActivity.loadSettings(this);
		
		
		LogoCache.loadCache(this);
		
		setLogos(LogoLoaderTask.DefaultLogosOperator);
		
		mainActivityInstance = this;
		mPrefs = mainActivityInstance.getPreferences(MODE_PRIVATE);

		CookieSyncManager.createInstance(this.getApplicationContext());
		CookieManager.getInstance().setAcceptCookie(true);

		discoveryHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Log.d(TAG, "Discovery result processing. "+msg.what);
				
				vDiscoveryStatus.setText(getString(msg.what));
				setButtonStates((DiscoveryData) msg.obj);
				discoveryButton.setEnabled(true);
			}
		};

		final Handler phoneStatusHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				vStatus.setText(getString(msg.what));
			}
		};
		
		logoUpdateHandler = new Handler() {
			public void handleMessage(Message msg) {
				handleLogoUpdate();
			}
		};
	    
		new Thread(new Runnable() { 
            public void run(){
            	boolean running=true;
            	while (running) {
					TelephonyManager telephonyManager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					PhoneState state = PhoneUtils.getPhoneState(telephonyManager, connectivityManager);

					boolean connected = state.isConnected(); // Is the device connected to
					// the Internet
					boolean usingMobileData = state.isUsingMobileData(); // Is the device
					// connected using cellular/mobile data
					boolean roaming = state.isRoaming(); // Is the device roaming
					
					int status = R.string.statusDisconnected;
					if (roaming) {
						status = R.string.statusRoaming;
					} else if (usingMobileData) {
						status = R.string.statusOnNet;
					} else if (connected) {
						status = R.string.statusOffNet;
					}
					phoneStatusHandler.sendEmptyMessage(status);
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						running=false;
					}
            	}
            }
		}).start();
		
		Log.d(TAG, "starting logo API request for default logos");
		new LogoLoaderTask(mainActivityInstance, 
				SettingsActivity.getDeveloperOperator().getLogoEndpoint(),
				SettingsActivity.getDeveloperOperator().getAppKey(), 
				SettingsActivity.getDeveloperOperator().getAppSecret(),
				null /* mcc */, null /* mnc */, SettingsActivity.isCookiesSelected(), 
				SettingsActivity.getServingOperator().getIpaddress(), "small").execute();
	}
	
	public void handleLogoUpdate() {
		Log.d(TAG, "called handleLogoUpdate");
		boolean set=false;
		if (discoveryData!=null && discoveryData.getResponse()!=null) {
			String operator=discoveryData.getResponse().getSubscriber_operator();
			set=setLogos(operator);
		}
		if (!set) {
			set=setLogos(LogoLoaderTask.DefaultLogosOperator);
		}
		if (!set) {
			startPayment1.setBackgroundDrawable(null);
			startPayment2.setBackgroundDrawable(null);
			startPayment1.setText(R.string.startPayment1);
			startPayment2.setText(R.string.startPayment2);
			startOperatorId.setBackgroundDrawable(null);
			startOperatorId.setText(R.string.startOperatorId);
			set=true;
		}
	}
	
	private boolean setLogos(String operator) {
		boolean set=false;
		Log.d(TAG, "Trying logos for operator = "+operator);
		Bitmap paymentImage=LogoCache.getBitmap(operator, "payment", "en", "small"); 
		Bitmap operatorIdImage=LogoCache.getBitmap(operator, "operatorid", "en", "small");
		
		if (paymentImage!=null) {
			Drawable d = new BitmapDrawable(paymentImage);
			startPayment1.setBackgroundDrawable(d);
			startPayment2.setBackgroundDrawable(d);
			startPayment1.setText("");
			startPayment2.setText("");
			set=true;
		}

		if (operatorIdImage!=null) {
			Drawable d = new BitmapDrawable(operatorIdImage);
			startOperatorId.setBackgroundDrawable(d);
			startOperatorId.setText("");
			set=true;
		}
		return set;
	}
	
	/*
	 * on start or return to the main screen reset the screen so that discovery
	 * can be started
	 */
	@Override
	public void onStart() {
		super.onStart();
		
		Log.d(TAG, "onStart called");
		
		boolean cacheGood=false;
		
		/* Reset the flag that stops a duplicate discovery request to be made */
		started = false;
		
		if (!justDiscovered) {
			startOperatorId.setVisibility(View.INVISIBLE);
			startPayment1.setVisibility(View.INVISIBLE);
			startPayment2.setVisibility(View.INVISIBLE);

			Log.d(TAG, "Checking for cached discovery response");
			String discoveryDataSerialised=mPrefs.getString("DiscoveryData", null);
			Log.d(TAG, "Cached "+discoveryDataSerialised);
			if (discoveryDataSerialised!=null) {
				ObjectMapper mapper=new ObjectMapper();
				try {
					Log.d(TAG, "Converting to discoverydata object");
					DiscoveryData cachedData=mapper.readValue(discoveryDataSerialised, DiscoveryData.class);
					Log.d(TAG, "Have "+cachedData+" TTL="+(cachedData!=null?cachedData.getTtl():null));
					if (cachedData!=null && cachedData.getTtl()!=null) {
						Log.d(TAG, "Converting TTL="+cachedData.getTtl());
						long ttl=Long.valueOf(cachedData.getTtl());
						java.util.Date now=new java.util.Date();
						Log.d(TAG, "Checking TTL "+ttl+" against now="+(now.getTime()));
						if (now.getTime()<ttl) {
							Log.d(TAG, "Cache is good");
							discoveryData=cachedData;
							discovered=true;
							vDiscoveryStatus.setText(getString(R.string.discoveryStatusCached));
							cacheGood=true;
							discoveryButton.setEnabled(false);
							setButtonStates(cachedData);
							discoveryButton.setEnabled(true);
						}
					}
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			String mcc=SettingsActivity.getMcc();
			String mnc=SettingsActivity.getMnc();
			
			vMCC.setText(mcc!=null?mcc:getText(R.string.valueUnknown));
			vMNC.setText(mnc!=null?mnc:getText(R.string.valueUnknown));
	
			Log.d(TAG, "starting logo API request for current operator logos");
			new LogoLoaderTask(mainActivityInstance, 
					SettingsActivity.getDeveloperOperator().getLogoEndpoint(),
					SettingsActivity.getDeveloperOperator().getAppKey(), 
					SettingsActivity.getDeveloperOperator().getAppSecret(),
					mcc, mnc, SettingsActivity.isCookiesSelected(), 
					SettingsActivity.getServingOperator().getIpaddress(), "small").execute();
			
			DiscoveryStartupSettings startupOption=SettingsActivity.getDiscoveryStartupSettings();
			if (startupOption==DiscoveryStartupSettings.STARTUP_OPTION_PASSIVE) {
				if (!cacheGood) {
					discoveryButton.setEnabled(false);
					vDiscoveryStatus.setText(getString(R.string.discoveryStatusStarted));
					
					passiveDiscoveryTask =
							new PassiveDiscoveryTask(mainActivityInstance, 
									SettingsActivity.getDeveloperOperator().getEndpoint(),
								SettingsActivity.getDeveloperOperator().getAppKey(), 
								SettingsActivity.getDeveloperOperator().getAppSecret(),
								mcc, mnc, SettingsActivity.isCookiesSelected(), 
								SettingsActivity.getServingOperator().getIpaddress());
					passiveDiscoveryTask.execute();
				}
			} else if (startupOption==DiscoveryStartupSettings.STARTUP_OPTION_PREEMPTIVE) {
				if (!cacheGood) {
					discoveryButton.setEnabled(false);
					vDiscoveryStatus.setText(getString(R.string.discoveryStatusStarted));
					initialDiscoveryTask = 
						new ActiveDiscoveryTask(mainActivityInstance, 
								SettingsActivity.getDeveloperOperator().getEndpoint(),
								SettingsActivity.getDeveloperOperator().getAppKey(), 
								SettingsActivity.getDeveloperOperator().getAppSecret(),
								mcc, mnc, SettingsActivity.isCookiesSelected(), 
								SettingsActivity.getServingOperator().getIpaddress());
					initialDiscoveryTask.execute();
				}
			}

		} else {
			justDiscovered=false;
			
			String mcc=SettingsActivity.getMcc();
			String mnc=SettingsActivity.getMnc();

			Log.d(TAG, "starting logo API request for current operator logos");
			new LogoLoaderTask(mainActivityInstance, 
					SettingsActivity.getDeveloperOperator().getLogoEndpoint(),
					SettingsActivity.getDeveloperOperator().getAppKey(), 
					SettingsActivity.getDeveloperOperator().getAppSecret(),
					mcc, mnc, SettingsActivity.isCookiesSelected(), 
					SettingsActivity.getServingOperator().getIpaddress(), "small").execute();
		}
	}

	/*
	 * default method to add a menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void setButtonStates(DiscoveryData discoveryData) {
		Log.d(TAG, "Setting button states");
		boolean payment1Enabled=false;
		boolean payment2Enabled=false;
		boolean operatorIdEnabled=false;
		
		Log.d(TAG, "discoveryData="+discoveryData);
		if (discoveryData!=null && discoveryData.getResponse()!=null) {
			Response resp=discoveryData.getResponse();
			Api operatorId=resp.getApi("operatorid");
			Api payment=resp.getApi("payment");
			Log.d(TAG, "operatorid="+operatorId);
			Log.d(TAG, "payment="+payment);
			if (operatorId!=null) {
				operatorIdEnabled=operatorId.getHref("authorize")!=null;
			}
			if (payment!=null) {
				payment1Enabled=payment.getHref("charge")!=null;
				payment2Enabled=payment.getHref("reserve")!=null;
			}
		}
		
		Log.d(TAG, "OperatorID enabled="+operatorIdEnabled);
		Log.d(TAG, "Payment1 enabled="+payment1Enabled);
		Log.d(TAG, "Payment2 enabled="+payment2Enabled);
		startOperatorId.setVisibility(operatorIdEnabled?View.VISIBLE:View.INVISIBLE);
		startPayment1.setVisibility(payment1Enabled?View.VISIBLE:View.INVISIBLE);
		startPayment2.setVisibility(payment2Enabled?View.VISIBLE:View.INVISIBLE);

	}

	/*
	 * handles a restart/ refresh of the discovery process
	 */
	public void restart(View view) {
		/* Reset text on start button */
		discoveryButton.setText(getString(R.string.start));

		/* Reset the discovery process lock */
		started = false;
	}

	
	public static void updateDiscoveryData(DiscoveryData discovered) {
		Log.d(TAG, "Updating discovery data");
		discoveryData=discovered;
		justDiscovered=true;
		Message msg=new Message();
		msg.what=R.string.discoveryStatusCompleted;
		msg.obj=discovered;
		discoveryHandler.sendMessage(msg);
		
		try {
			Editor editor=mPrefs.edit();
			ObjectMapper mapper=new ObjectMapper();
			String serialised = discovered!=null?mapper.writeValueAsString(discovered):null;
			editor.putString("DiscoveryData", serialised);
			Log.d(TAG, "Serialised as "+serialised);
			editor.commit();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void clearDiscoveryData() {
		Log.d(TAG, "Clearing discovery data");
		discoveryData=null;
		Message msg=new Message();
		msg.what=R.string.discoveryStatusPending;
		msg.obj=null;
		discoveryHandler.sendMessage(msg);
		
		Editor editor=mPrefs.edit();
		editor.putString("DiscoveryData", null);
		editor.commit();
	}

	public static DiscoveryData getDiscoveryData() {
		return MainActivity.discoveryData;
	}

	/*
	 * if there is an error any time during discovery it will be displayed via
	 * the displayError function
	 */
	public void displayError(String error, String errorDescription) {
		Toast toast = Toast.makeText(getBaseContext(), errorDescription,
				Toast.LENGTH_LONG);
		toast.show();
	}
	
	public String getServingOperatorName() {
		return SettingsActivity.getServingOperator().getName();
	}
	
	public void startDemos(View view) {
		Intent intent = new Intent(
				this,
				DemoActivity.class);		
		startActivity(intent);
	}

	public void startSettings(View view) {
		cancelOutstandingDiscoveryTasks();
		Intent intent = new Intent(
				this,
				SettingsActivity.class);		
		startActivity(intent);
	}

	public void handleDiscovery(View view) {
		if (discoveryButton.isEnabled()) {
			String mcc=null;
			String mnc=null;
			
			if (SettingsActivity.getServingOperator().isAutomatic()) {
				TelephonyManager telephonyManager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				PhoneState state = PhoneUtils.getPhoneState(telephonyManager, connectivityManager);
				mcc=state.getMcc();
				mnc=state.getMnc();
			} else {
				mcc=SettingsActivity.getServingOperator().getMcc();
				mnc=SettingsActivity.getServingOperator().getMnc();
			}

			discoveryButton.setEnabled(false);
			vDiscoveryStatus.setText(getString(R.string.discoveryStatusStarted));
			initialDiscoveryTask=
				new ActiveDiscoveryTask(mainActivityInstance, 
						SettingsActivity.getDeveloperOperator().getEndpoint(),
						SettingsActivity.getDeveloperOperator().getAppKey(), 
						SettingsActivity.getDeveloperOperator().getAppSecret(),
						mcc, mnc, SettingsActivity.isCookiesSelected(), 
						SettingsActivity.getServingOperator().getIpaddress());
			initialDiscoveryTask.execute();
		}
	}

	public void startOperatorId(View view) {
		cancelOutstandingDiscoveryTasks();
		Api operatoridEndpoint=discoveryData.getResponse()!=null?discoveryData.getResponse().getApi("operatorid"):null;
		
		String openIDConnectScopes=PreferencesUtils.getPreference("OpenIDConnectScopes");
		
		String returnUri=PreferencesUtils.getPreference("OpenIDConnectReturnUri");
		
		Intent intent = new Intent(
				this,
				DisplayIdentityWebsiteActivity.class);
		intent.putExtra("authUri", operatoridEndpoint.getHref("authorize"));
		intent.putExtra("tokenUri", operatoridEndpoint.getHref("token"));
		intent.putExtra("userinfoUri", operatoridEndpoint.getHref("userinfo"));
		intent.putExtra("clientId", discoveryData.getResponse().getClient_id());
		intent.putExtra("clientSecret", discoveryData.getResponse().getClient_secret());
		intent.putExtra("scopes", openIDConnectScopes);
		intent.putExtra("returnUri", returnUri);
		
		startActivity(intent);
	}

	public void startPayment1(View view) {
		cancelOutstandingDiscoveryTasks();
		Intent intent = new Intent(
				this,
				PaymentStartActivity.class);
		intent.putExtra("method", PaymentStartActivity.METHOD_1_PHASE);
		startActivity(intent);
	}

	public void startPayment2(View view) {
		cancelOutstandingDiscoveryTasks();
		Intent intent = new Intent(
				this,
				PaymentStartActivity.class);
		intent.putExtra("method", PaymentStartActivity.METHOD_2_PHASE);
		startActivity(intent);
	}

	public static void processLogoUpdates() {
		logoUpdateHandler.sendEmptyMessage(LOGOS_UPDATED);
	}
	
	private void cancelOutstandingDiscoveryTasks() {
		if (initialDiscoveryTask!=null) {
			initialDiscoveryTask.cancel(true);
			initialDiscoveryTask=null;
		}
		if (passiveDiscoveryTask!=null) {
			passiveDiscoveryTask.cancel(true);
			passiveDiscoveryTask=null;
		}
	}
}

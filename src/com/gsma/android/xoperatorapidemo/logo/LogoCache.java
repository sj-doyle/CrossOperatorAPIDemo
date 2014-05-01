package com.gsma.android.xoperatorapidemo.logo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class LogoCache {
	private static final String TAG = "LogoCache";

	private static SharedPreferences  mPrefs=null;
	private static final String preferenceName="LogoCache";
	private static ArrayList<LogoCacheItem> logoCache=null;
	private static HashMap<String, LogoCacheItem> logoQuickCache=null;
	private static ContextWrapper contextWrapper=null;
    private static final String filepath = "LogoStorage";
    
	public static void loadCache(Activity activity) {
		mPrefs = activity.getPreferences(Activity.MODE_PRIVATE);
		logoCache=new ArrayList<LogoCacheItem>();
		logoQuickCache=new HashMap<String, LogoCacheItem>();
        contextWrapper = new ContextWrapper(activity.getApplicationContext());
		
		Log.d(TAG, "Recovering logo cache");
		String logoCacheSerialised=mPrefs.getString(preferenceName, null);
		if (logoCacheSerialised!=null) {
			Log.d(TAG, "Logo cache data="+logoCacheSerialised);
			LogoCacheItem[] storedLogos;
			try {
				Log.d(TAG, "Converting from string to object");
				storedLogos = LogoCacheItem.fromSerialisedArrayString(logoCacheSerialised);
				Log.d(TAG, "Object = "+storedLogos);
				if (storedLogos!=null) {
					Log.d(TAG, "Cache contains "+storedLogos.length+" entries");
					for (int i=0; i<storedLogos.length; i++) {
						Log.d(TAG, "["+i+"] = "+storedLogos[i].getLogo().getUrl()+" "+storedLogos[i].getLocalFile());
						Bitmap logoBitmap=BitmapFactory.decodeFile(storedLogos[i].getLocalFile());
						if (logoBitmap!=null) {
							Log.d(TAG, "Read bitmap");
							storedLogos[i].setImageFile(logoBitmap);
							logoCache.add(storedLogos[i]);
							logoQuickCache.put(storedLogos[i].getLogo().getUrl(), storedLogos[i]);
						}
					}
				}
			} catch (JSONException e) { 
				Log.d(TAG, "Exception="+e.getMessage());
			}
		}
	}
	
	private static void save() {
		String logoCacheSerialised=toSerialisedString();
		Log.d(TAG, "Serialised data="+logoCacheSerialised);
		SharedPreferences.Editor editor = mPrefs.edit();
	    editor.putString(preferenceName, logoCacheSerialised);
	    editor.commit();
	}
	
	public static void clearCache() {
		logoCache.clear();
		logoQuickCache.clear();
		save();
	}
	
	public static Bitmap getBitmap(String operatorId, String apiName, String language, String size) {
		Bitmap bitmap=null;
		for (int i=0; i<logoCache.size() && bitmap==null; i++) {
			LogoCacheItem lci=logoCache.get(i);
			LogoResponse lr=lci.getLogo();
			if (operatorId.equals(lr.operatorId) && apiName.equals(lr.getApiName()) && language.equals(lr.getLanguage()) && size.equals(lr.getSize())) {
				bitmap=lci.getImageFile();
			}
		}
		return bitmap;
	}
	
	public static String toSerialisedString() {
		String rv=null;
		try {
			JSONArray obj = toArray();
			rv=obj.toString();
		} catch (JSONException e) {
		}
		return rv;
	}

	public static JSONArray toArray() throws JSONException {
		JSONArray obj=new JSONArray();
		if (logoCache!=null) {
			for (int i=0; i<logoCache.size(); i++) {
				obj.put(i, logoCache.get(i).toObject());
			}
		}
		return obj;
	}

	public static void addLogoResponse(LogoResponse logoResponse) {
		if (logoResponse!=null) {
			if (logoQuickCache.containsKey(logoResponse.getUrl())) {
				LogoCacheItem lci=logoQuickCache.get(logoResponse.getUrl());
				Log.d(TAG, "Already in cache - checking resource");
				try {
					URL url=new URL(logoResponse.getUrl());
					URLConnection urlC=url.openConnection();
					if (lci.getETag()!=null) {
						urlC.addRequestProperty("If-None-Match", lci.getETag());
					} else if (lci.getLastModifiedTimestamp()>0) {
						urlC.setIfModifiedSince(lci.getLastModifiedTimestamp());
					}

					String eTag=urlC.getHeaderField("ETag");
					String lastModified=urlC.getHeaderField("Last-Modified");
					long lastModifiedTimestamp=urlC.getLastModified();

					boolean updated=false;
					Log.d(TAG, "eTag="+eTag);
					Log.d(TAG, "lastModified="+lastModified);
					if (eTag!=null && !eTag.equals(lci.getETag())) {
						updated=true;
					} else if (lastModifiedTimestamp>0 && lastModifiedTimestamp>lci.getLastModifiedTimestamp()) {
						updated=true;
					}
					
					Log.d(TAG, "Update? = "+updated);
					if (updated) {
						InputStream in = urlC.getInputStream();
			            Bitmap logoBitmap = BitmapFactory.decodeStream(in);
			            File localImageFile = new File(lci.getLocalFile());
			            FileOutputStream fos = new FileOutputStream(localImageFile);
			            logoBitmap.compress(CompressFormat.PNG, 100, fos);
			            fos.close();
			            lci.setETag(eTag);
			            lci.setLastModified(lastModified);
			            lci.setImageFile(logoBitmap);
			            lci.setLastModifiedTimestamp(lastModifiedTimestamp);
			            save();
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else {
				LogoCacheItem lci=new LogoCacheItem();
				lci.setLogo(logoResponse);
				
				try {
					Log.d(TAG, "Reading from "+logoResponse.getUrl());
					URL url=new URL(logoResponse.getUrl());
					URLConnection urlC=url.openConnection();
					String eTag=urlC.getHeaderField("ETag");
					String lastModified=urlC.getHeaderField("Last-Modified");
					Log.d(TAG, "eTag = "+eTag);
					Log.d(TAG, "lastModified = "+lastModified);
		            InputStream in = urlC.getInputStream();
		            Bitmap logoBitmap = BitmapFactory.decodeStream(in);
		            
		            File directory = contextWrapper.getDir(filepath, Context.MODE_PRIVATE);
		            MessageDigest digest=null;
		            digest = MessageDigest.getInstance("SHA-256");
		            digest.reset();
		            String localFilename=bin2hex(digest.digest(logoResponse.getUrl().getBytes("UTF-8")))+".png";
		            Log.d(TAG, "Local file name="+localFilename);
		            
		            File localImageFile = new File(directory, localFilename);
		            FileOutputStream fos = new FileOutputStream(localImageFile);
		            logoBitmap.compress(CompressFormat.PNG, 100, fos);
		            fos.close();
		            Log.d(TAG, "Written to="+localImageFile.getAbsolutePath());
		            
		            lci.setETag(eTag);
		            lci.setLastModified(lastModified);
		            lci.setLocalFile(localImageFile.getAbsolutePath());
		            lci.setImageFile(logoBitmap);
		            lci.setLastModifiedTimestamp(urlC.getLastModified());
		            
		            Log.d(TAG, "Storing to cache");
		            logoCache.add(lci);
		            logoQuickCache.put(logoResponse.getUrl(), lci);
		            
		            save();
		            
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
	
	static String bin2hex(byte[] data) {
	    return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
	}
}

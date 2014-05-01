package com.gsma.android.xoperatorapidemo.logo;

import org.json.JSONArray;
import org.json.JSONException;

public class LogoResponseArray {
	LogoResponse[] logos;

	public LogoResponse[] getLogos() {
		return logos;
	}

	public void setLogos(LogoResponse[] logos) {
		this.logos = logos;
	}
	
	public LogoResponseArray() {
		
	}
	
	public LogoResponseArray(Object source) throws JSONException {
		if (source!=null) {
			if (source instanceof JSONArray) {
				JSONArray sa=(JSONArray) source;
				logos=new LogoResponse[sa.length()];
				for (int i=0; i<sa.length(); i++) {
					logos[i]=new LogoResponse(sa.get(i));
				}
			}
		}
	}
	
}

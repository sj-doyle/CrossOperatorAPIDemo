package com.gsma.android.xoperatorapidemo.discovery;


public class DiscoveryServingOperatorSettings {
	
	private static final ServingOperatorSetting automatic=new ServingOperatorSetting("Automatic - from device", true, null, null, null);
	private static final ServingOperatorSetting atel=new ServingOperatorSetting("Atel 000-02", false, "000", "02", null);
	private static final ServingOperatorSetting etel=new ServingOperatorSetting("Etel 000-01", false, "000", "01", null);
	private static final ServingOperatorSetting atelip=new ServingOperatorSetting("Atel IP 150.0.0.1 ", false, null, null, "150.0.0.1");
	private static final ServingOperatorSetting etelip=new ServingOperatorSetting("Etel IP 75.0.0.1", false, null, null, "75.0.0.1");
	private static final ServingOperatorSetting none=new ServingOperatorSetting("No auto assist", false, null, null, "5.5.5.5");
//	private static final ServingOperatorSetting dt26206=new ServingOperatorSetting("DT Germany 262-06", false, "262", "06");
//	private static final ServingOperatorSetting att31030=new ServingOperatorSetting("AT&T USA 310-30", false, "310", "30");
//	private static final ServingOperatorSetting att310150=new ServingOperatorSetting("AT&T USA 310-150", false, "310", "150");
//	private static final ServingOperatorSetting att310170=new ServingOperatorSetting("AT&T USA 310-170", false, "310", "170");
//
//	private static final ServingOperatorSetting dt26201=new ServingOperatorSetting("DT Germany 262-01", false, "262", "01");
//
//	private static final ServingOperatorSetting vod26201=new ServingOperatorSetting("Vodafone Germany 262-02", false, "262", "02");
//
//	private static final ServingOperatorSetting orange20801=new ServingOperatorSetting("Orange France 208-01", false, "208", "01");
	
	private static final ServingOperatorSetting[] operators={atel, etel, atelip, etelip, automatic, none/*, automatic, dt26206, att31030, att310150, att310170, dt26201, vod26201, orange20801*/};
	
	private static String[] operatorNames=null;
	
	static {
		operatorNames=new String[operators.length];
		int index=0;
		for (ServingOperatorSetting operator:operators) {
			operatorNames[index++]=operator.getName();
		}
	}
	
	public static String[] getOperatorNames() { return operatorNames; }
	
	public static ServingOperatorSetting getOperator(int index) { return operators[index]; }
}
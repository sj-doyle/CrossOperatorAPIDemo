package com.gsma.android.xoperatorapidemo.discovery;


public class DiscoveryDeveloperOperatorSettings {

//	private static final DeveloperOperatorSetting atelDev=new DeveloperOperatorSetting("ATel Development", "https://integration-sb2.apiexchange.org/v1/discovery", 
//				"dev2-test-app-test-key");
//	
//	private static final DeveloperOperatorSetting attDev=new DeveloperOperatorSetting("AT&T Development", "https://integration-att.apiexchange.org/v1/discovery", 
//				"att-dev1-payment-app-test-key");
//
//	private static final DeveloperOperatorSetting attProd=new DeveloperOperatorSetting("AT&T Production", "https://integration-att.apiexchange.org/v1/discovery", 
//			   "att-dev1-payment-app-prod-key");
//	
//	private static final DeveloperOperatorSetting dtDev=new DeveloperOperatorSetting("DT Development", "https://integration-dt.apiexchange.org/v1/discovery", 
//			   "dt-dev1-payment-app-test-key");
//	
//	private static final DeveloperOperatorSetting dtProd=new DeveloperOperatorSetting("DT Production", "https://integration-dt.apiexchange.org/v1/discovery", 
//			   "dt-dev1-payment-app-prod-key");
//	
//	private static final DeveloperOperatorSetting vodDev=new DeveloperOperatorSetting("Vodafone Development", "https://integration-vod.apiexchange.org/v1/discovery",
//			   "vod-dev1-payment-app-test-key");
//	
//	private static final DeveloperOperatorSetting vodProd=new DeveloperOperatorSetting("Vodafone Production", "https://integration-vod.apiexchange.org/v1/discovery", 
//			   "vod-dev1-payment-app-prod-key");
//	
//	private static final DeveloperOperatorSetting[] operators={atelDev, attDev, attProd, dtDev, dtProd, vodDev, vodProd};
	
//	private static final DeveloperOperatorSetting testDev=new DeveloperOperatorSetting("MWC 2014 Demo", 
//			"https://etelco-prod.apigee.net/v1/discovery", "DmaPIXFihqJhHhVQwpk9NHd7BzIzQxOe", "Doul7PiXVFCNI77g");

//	private static final DeveloperOperatorSetting testDev=new DeveloperOperatorSetting("ETel Demo", 
//			"https://etelco-prod.apigee.net/v1/discovery", "BJL7na81ZEaaFuoz1bbqT3CyS5x9CAFS", "Mt6HAx5Ujb39Sbs0", "https://etelco-prod.apigee.net/v1/logo");	

	private static final DeveloperOperatorSetting testDev=new DeveloperOperatorSetting("ETel Demo", 
			"https://etelco-prod.apigee.net/v1/discovery", "BJL7na81ZEaaFuoz1bbqT3CyS5x9CAFS", "Mt6HAx5Ujb39Sbs0", "https://integration-dt.apiexchange.org/v1/logo");	

	
	private static String[] operatorNames=null;
	private static final DeveloperOperatorSetting[] operators={testDev};
	
	static {
		operatorNames=new String[operators.length];
		int index=0;
		for (DeveloperOperatorSetting operator:operators) {
			operatorNames[index++]=operator.getName();
		}
	}
	
	public static String[] getOperatorNames() { return operatorNames; }
	
	public static DeveloperOperatorSetting getOperator(int index) { return operators[index]; }
}
package com.cybersource.paymentapp.security;

import java.util.Properties;

public class Configuration {
  
  private Configuration() {
    
  }
  
	public static Properties getMerchantDetails() {
		Properties props = new Properties();

		// HTTP_Signature = http_signature and JWT = jwt
		props.setProperty("authenticationType", "http_signature");
		props.setProperty("merchantID", "ravi_cognizant");
		props.setProperty("runEnvironment", "CyberSource.Environment.SANDBOX");
		props.setProperty("requestJsonPath", "src/main/resources/request.json");

		// JWT Parameters
		/*props.setProperty("keyAlias", "testrest");
		props.setProperty("keyPass", "testrest");
		props.setProperty("keyFileName", "testrest");*/

		// P12 key path. Enter the folder path where the .p12 file is located.

		props.setProperty("keysDirectory", "src/main/resources");
		// HTTP Parameters
		props.setProperty("merchantKeyId", "a58c1ade-2f08-40d5-98a6-e7ce6a2caee2");
		props.setProperty("merchantsecretKey", "ftgYFzy0OZ2WxwQjekjBfH/9UANsOJ7UK5xRzCRaBGI=");
		// Logging to be enabled or not.
		props.setProperty("enableLog", "true");
		// Log directory Path
		props.setProperty("logDirectory", "log");
		props.setProperty("logFilename", "cybs");

		// Log file size in KB
		props.setProperty("logMaximumSize", "5M");

		return props;

	}

}

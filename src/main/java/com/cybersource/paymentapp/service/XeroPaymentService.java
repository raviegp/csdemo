package com.cybersource.paymentapp.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.cybersource.paymentapp.util.AppProperties;
import com.cybersource.paymentapp.xero.model.Tenant;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xero.models.accounting.Account;
import com.xero.models.accounting.Accounts;
import com.xero.models.accounting.Invoice;
import com.xero.models.accounting.Invoices;

@Configuration
@RequestMapping("xeroconfig")
public class XeroPaymentService {

  @Autowired
  AppProperties myAppProperties;

  private String access_token = null;

  private String refresh_token = null;

  private String tenant_id = null;

  private FileInputStream in;

  private FileOutputStream out;

  private Properties props;

  String clientId = "7A2F7AD9A5D845A7B3949BC935DF4FDF";

  String clientSecret = "HW0GwJwFco7M-Jr64ybOhuz6Muu8oBPluBCcmrPNGUwB_1A8";

  String token_url = "https://identity.xero.com/connect/token";

  String connection_url = "https://api.xero.com/connections";

  String invoice_url = "https://api.xero.com/api.xro/2.0/Invoices/";

  String accounts_url = "https://api.xero.com/api.xro/2.0/Accounts";

  String payments_url = "https://api.xero.com/api.xro/2.0/payments";
  
  String app_config_file = "C:\\CMS_Code-24-10-19\\paymentapp\\src\\main\\resources\\application.properties";
  
  public void getAccessCodeUsingRedirectURI() {

    System.out.println("Inside XeroPaymentService getAccessCodeUsingRedirectURI method");
    // String urlString = "https://login.xero.com/identity/connect/authorize";
    String urlString = "https://login.xero.com/identity/connect/authorize?response_type=code&client_id=7A2F7AD9A5D845A7B3949BC935DF4FDF&redirect_uri=https://www.google.com/&scope=offline_access openid profile email&state=123";

    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("Accept", "application/json");
      params.put("Content-Type", "application/json");

      HttpHeaders headers = new HttpHeaders();
      headers.set("Accept", "text/html");
      headers.set("Content-Type", "application/json");

      RestTemplate restTemplate = new RestTemplate();
      // String result = restTemplate.getForObject(urlString, String.class, params);
      HttpEntity entity = new HttpEntity(headers);
      HttpEntity<String> response = restTemplate.exchange(urlString, HttpMethod.GET, entity, String.class, params);
      // System.out.println(" response header"+response.getHeaders().toString());
      // HttpEntity entity = new HttpEntity(param,headers);
      // HttpEntity<String> response = restTemplate.exchange(urlString, HttpMethod.GET, entity, String.class);
      System.out.println("GET result : " + response.getBody());

      // System.out.println("GET result : " +result.toString());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void retrieveTokenDetails() throws IOException, Exception {

    //
    System.out.println("XeroPaymentService retrieveTokenDetails method ");

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/x-www-form-urlencoded");
    FormHttpMessageConverter converter = new FormHttpMessageConverter();
    MediaType mediaType = new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8"));
    converter.setSupportedMediaTypes(Arrays.asList(mediaType));
    restTemplate.getMessageConverters().add(converter);

    String encodedData = DatatypeConverter.printBase64Binary((clientId + ":" + clientSecret).getBytes("UTF-8"));
    String authorizationHeaderString = "Basic " + encodedData;
    headers.set("authorization", authorizationHeaderString);

    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.set("grant_type", "authorization_code");
    map.set("code", "1e16bf08bc06a3108fdeb90e58487fd875c6e0a9ce87eba16188fea249901569");
    map.set("redirect_uri", "https://www.google.com/");
    // map.set("refresh_token", "0b32dfc910dbeb173d87952d6e4bc7bf1cd2828cafdab3534e357a77deddd886");

    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
    ResponseEntity<String> result = restTemplate.postForEntity(token_url, request, String.class);

    String splirarr[] = result.toString().split(",");

    String refreshtoken = splirarr[5].split(":")[1];
    String accesstoken = splirarr[2].split(":")[1];

    this.access_token = accesstoken.substring(1, accesstoken.length() - 1);
    this.refresh_token = refreshtoken = refreshtoken.substring(1, refreshtoken.length() - 1);

    System.out.println(" refreshtoken " + this.refresh_token);

  }

  public void retrieveTenantDetails() throws IOException, Exception {

    System.out.println("XeroPaymentService retrieveTenantDetails method ");

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/json");
    String authStr = "Bearer " + this.access_token;

    headers.set("Authorization", authStr);
    //
    HttpEntity entity = new HttpEntity(headers);
    HttpEntity<String> response = restTemplate.exchange(connection_url, HttpMethod.GET, entity, String.class);

    String tenantJson = response.getBody();
    Gson gson = new Gson();

    Tenant[] tenantDetails = gson.fromJson(tenantJson, Tenant[].class);

    this.tenant_id = tenantDetails[0].getTenantId();

  }

  public Invoice getInvoiceDetails(String invoiceNo) throws Exception {

    System.out.println("XeroPaymentService getInvoiceDetails method ");

    refreshToken(this.refresh_token);
    retrieveTenantDetails();
    invoice_url += invoiceNo;

    RestTemplate restTemplate = new RestTemplate();
    // System.out.println("GET access_token retrieved previously : "+this.access_token);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/json");
    String authStr = "Bearer " + this.access_token;
    // System.out.println("GET access_token retrieved previously : "+authStr);
    // System.out.println("GET tennant_id retrieved previously : "+this.tenant_id);
    headers.set("Authorization", authStr);
    headers.set("xero-tenant-id", this.tenant_id);

    HttpEntity entity = new HttpEntity(headers);
    HttpEntity<Invoices> response = restTemplate.exchange(invoice_url, HttpMethod.GET, entity, Invoices.class);

    Invoice invoice = response.getBody().getInvoices().get(0);
    System.out.println(" Invoice no : " + invoice.getInvoiceNumber());
    System.out.println(" Invoice ID : " + invoice.getInvoiceID().toString());
    System.out.println(" Contact  : " + invoice.getContact().getName());
    System.out.println(" Amount due  : " + invoice.getAmountDue());
    return invoice;
  }

  public Account getAccountDetails() throws Exception {

    refreshToken(this.refresh_token);
    retrieveTenantDetails();

    System.out.println("XeroPaymentService getAccountDetails method ");

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/json");
    String authStr = "Bearer " + this.access_token;

    headers.set("Authorization", authStr);
    headers.set("xero-tenant-id", this.tenant_id);
    HttpEntity entity = new HttpEntity(headers);
    HttpEntity<Accounts> response = restTemplate.exchange(accounts_url, HttpMethod.GET, entity, Accounts.class);
    Account paymentAcct = response.getBody().getAccounts().get(0);
    System.out.println("Enable payment : " + paymentAcct.getEnablePaymentsToAccount());
    System.out.println("Code : " + paymentAcct.getCode());
    return paymentAcct;

  }

  public void postPaymentToXero(String invoiceId, String account, String date, String amount) {

    System.out.println("XeroPaymentService postPaymentToXero method ");

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/json");

    String authStr = "Bearer " + this.access_token;
    headers.set("Authorization", authStr);
    headers.set("xero-tenant-id", this.tenant_id);

    JsonObject invObj = new JsonObject();
    invObj.addProperty("InvoiceID", invoiceId);
    JsonObject acctObj = new JsonObject();
    acctObj.addProperty("Code", account);
    JsonObject paymenttObj = new JsonObject();
    paymenttObj.add("Invoice", invObj);
    paymenttObj.add("Account", acctObj);
    paymenttObj.addProperty("Date", date);
    paymenttObj.addProperty("Amount", amount);

    System.out.println(" payment json " + paymenttObj.toString());

    HttpEntity<String> request = new HttpEntity<String>(paymenttObj.toString(), headers);
    ResponseEntity<String> result = restTemplate.postForEntity(payments_url, request, String.class);
    System.out.println("POST result : " + result);

  }

  public void getOrganizationDetails() {

    System.out.println("XeroPaymentService getOrganizationDetails method ");
  }

  public void refreshToken(String storedToken) throws IOException, Exception {

    System.out.println("XeroPaymentService refreshToken method ");

    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/x-www-form-urlencoded");
    FormHttpMessageConverter converter = new FormHttpMessageConverter();
    MediaType mediaType = new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8"));
    converter.setSupportedMediaTypes(Arrays.asList(mediaType));

    restTemplate.getMessageConverters().add(converter);

    String encodedData = DatatypeConverter.printBase64Binary((clientId + ":" + clientSecret).getBytes("UTF-8"));
    String authorizationHeaderString = "Basic " + encodedData;
    headers.set("authorization", authorizationHeaderString);

    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.set("grant_type", "refresh_token");
    map.set("refresh_token", storedToken);
   
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
    ResponseEntity<String> result = restTemplate.postForEntity(token_url, request, String.class);

    String splirarr[] = result.toString().split(",");

    System.out.println(" 2 " + splirarr[2]);
    System.out.println(" 3 " + splirarr[3]);
    System.out.println(" 5 " + splirarr[5]);
    String refreshtoken = splirarr[5].split(":")[1];
    String accesstoken = splirarr[2].split(":")[1];

    this.access_token = accesstoken.substring(1, accesstoken.length() - 1);
    this.refresh_token = refreshtoken = refreshtoken.substring(1, refreshtoken.length() - 1);
    // myAppProperties.setRefresh_token(this.refresh_token); //store it in app properties for next run

  }

  public void getInvoice_refresh() throws Exception {

    refreshToken(this.refresh_token);
    retrieveTenantDetails();
    getInvoiceDetails("INV-0004");
  }

  public void retrieveTokenfromFile() {

    try {
      in = new FileInputStream(app_config_file);
      props = new Properties();
      props.load(in);
      in.close();
      this.refresh_token = props.getProperty("xeroconfig.refresh_token");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void saveTokentoFile() {

    try {
      out = new FileOutputStream(app_config_file);
      props.setProperty("xeroconfig.refresh_token", this.refresh_token);
      props.store(out, null);
      out.close();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String args[]) throws Exception {

    XeroPaymentService service = new XeroPaymentService();
    // service.checkAppValue();
    // service.getAccessCodeUsingRedirectURI(); // Not working
    service.retrieveTokenDetails(); // For the first time
    // service.refreshToken("1291e13753e9c89aeebdc2494e91f6d66f562a18305b2aa43a3b241297ea71c6");
    // service.retrieveTenantDetails();
    // service.getInvoiceDetails();
  }

  public RestTemplate restTemplate() {

    RestTemplate restTemplate = new RestTemplate();
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("Accept", "application/json");
      headers.set("Content-Type", "application/json");

      String authStr = "Bearer " + this.access_token;
      headers.set("Authorization", authStr);
      headers.set("xero-tenant-id", this.tenant_id);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return restTemplate;
  }

}

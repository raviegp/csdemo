package com.cybersource.paymentapp.service;

import java.util.Properties;

import com.cybersource.authsdk.core.MerchantConfig;
import com.cybersource.paymentapp.model.CreditCardDetails;
import com.cybersource.paymentapp.model.PaymentResponse;
import com.cybersource.paymentapp.security.Configuration;
import com.xero.models.accounting.Invoice;

import Api.PaymentsApi;
import Invokers.ApiClient;
import Model.CreatePaymentRequest;
import Model.PtsV2PaymentsPost201Response;
import Model.Ptsv2paymentsOrderInformation;
import Model.Ptsv2paymentsOrderInformationAmountDetails;
import Model.Ptsv2paymentsOrderInformationBillTo;
import Model.Ptsv2paymentsPaymentInformation;
import Model.Ptsv2paymentsPaymentInformationCard;

@org.springframework.context.annotation.Configuration
public class CyberSourcePaymentService {

  private static Properties merchantProp;

  private static String responseCode = null;

  private static String status = null;

  private static PtsV2PaymentsPost201Response response;

  public PaymentResponse processCreditCardPayment(CreditCardDetails cardDetails, Invoice invoice, String amount,
      String currency) throws Exception {

    System.out.println("CyberSourcePaymentService process method ");

    // Populate Card details
    Ptsv2paymentsPaymentInformationCard card = new Ptsv2paymentsPaymentInformationCard();
    card.expirationYear(cardDetails.getExpirationYear());
    card.number(cardDetails.getCardNumber());
    card.securityCode(cardDetails.getSecurityCode());
    card.expirationMonth(cardDetails.getExpirationMonth());

    Ptsv2paymentsPaymentInformation paymentInformation = new Ptsv2paymentsPaymentInformation();
    paymentInformation.card(card);

    // Populate Bill Details
    Ptsv2paymentsOrderInformationBillTo billTo = new Ptsv2paymentsOrderInformationBillTo();
    System.out.println("Preparing OrderInformation ");
    System.out.println("Country " + invoice.getContact().getAddresses().get(0).getCountry());
    System.out.println(
        "first name , last name " + invoice.getContact().getFirstName() + "," + invoice.getContact().getLastName());
    System.out.println("City " + invoice.getContact().getAddresses().get(0).getCity());
    System.out.println("region" + invoice.getContact().getAddresses().get(0).getRegion());
    System.out.println("Address line " + invoice.getContact().getAddresses().get(0).getAddressLine1());
    System.out.println("postal code " + invoice.getContact().getAddresses().get(0).getPostalCode());
    billTo.country(invoice.getContact().getAddresses().get(0).getCountry());
    billTo.firstName(invoice.getContact().getFirstName());
    billTo.lastName(invoice.getContact().getLastName());
    billTo.address1(invoice.getContact().getAddresses().get(0).getAddressLine1());
    billTo.postalCode("94105");
    billTo.locality(invoice.getContact().getAddresses().get(0).getCity());
    billTo.administrativeArea(invoice.getContact().getAddresses().get(0).getRegion());
    billTo.email("test@cybs.com");

    // Populate amount details
    Ptsv2paymentsOrderInformationAmountDetails amountDetails = new Ptsv2paymentsOrderInformationAmountDetails();
    System.out.println("amount " + amount);
    System.out.println("currency" + currency);
    amountDetails.totalAmount(amount);
    amountDetails.currency("USD");

    ApiClient apiClient = new ApiClient();
    merchantProp = Configuration.getMerchantDetails();
    MerchantConfig merchantConfig = new MerchantConfig(merchantProp);

    apiClient.merchantConfig = merchantConfig;

    PaymentsApi paymentApi = new PaymentsApi(apiClient);

    Ptsv2paymentsOrderInformation orderInformation = new Ptsv2paymentsOrderInformation();
    orderInformation.billTo(billTo);
    orderInformation.amountDetails(amountDetails);

    CreatePaymentRequest request = new CreatePaymentRequest();
    request.setOrderInformation(orderInformation);
    request.paymentInformation(paymentInformation);

    response = paymentApi.createPayment(request);

    responseCode = apiClient.responseCode;
    status = apiClient.status;

    System.out.println(" Status : " + response.getStatus());
    PaymentResponse cardResponse = new PaymentResponse();
    cardResponse.setStatus(response.getStatus());
    if (response.getErrorInformation() != null) {
      System.out.println(" Error reason : " + response.getErrorInformation().getReason());
      System.out.println(" Error Message : " + response.getErrorInformation().getMessage());
      cardResponse.setReasonCode(response.getErrorInformation().getReason());
      cardResponse.setReasonMsg(response.getErrorInformation().getMessage());
    }
    if (response.getStatus().equalsIgnoreCase("AUTHORIZED")) {
      System.out.println(" ReconciliationId : " + response.getReconciliationId());
      System.out.println(" Approval Code : " + response.getProcessorInformation().getApprovalCode());
      System.out.println(" Amount : " + response.getOrderInformation().getAmountDetails().getAuthorizedAmount());
      cardResponse.setReconcilationid(response.getReconciliationId());
      cardResponse.setApprovalCode(response.getProcessorInformation().getApprovalCode());
      cardResponse
          .setApprovedAmount(new Double(response.getOrderInformation().getAmountDetails().getAuthorizedAmount()));
    }
    return cardResponse;
  }

  public PaymentResponse errorInvoice(String reason, String message) {

    PaymentResponse cardResponse = new PaymentResponse();
    cardResponse.setReasonCode(reason);
    cardResponse.setReasonMsg(message);
    cardResponse.setStatus("INVALID");
    return cardResponse;
  }

  public static void main(String args[]) {

    try {
      new CyberSourcePaymentService().processCreditCardPayment(null, null, null, null);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}

package com.cybersource.paymentapp;

import java.util.Date;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cybersource.paymentapp.model.CreditCardDetails;
import com.cybersource.paymentapp.model.PaymentResponse;
import com.cybersource.paymentapp.service.CyberSourcePaymentService;
import com.cybersource.paymentapp.service.XeroPaymentService;
import com.xero.models.accounting.Account;
import com.xero.models.accounting.Invoice;

@Controller
public class PaymentController {

	//	/?invoiceNo=INV-0003&currency=INR&amount=105.00&shortCode=!h9vf9 
	@Autowired
	private CyberSourcePaymentService paymentService;
	
	@Autowired
	private XeroPaymentService xeroService;
	

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		System.out.println("***Inside PaymentController ****");
		model.addAttribute("name", name);
		System.out.println("*** calling checkvalue.. ");
		try {
//		 
			xeroService.retrieveTokenfromFile();
			xeroService.getInvoice_refresh();
			xeroService.getAccountDetails();
			xeroService.saveTokentoFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "greeting";
	}
	
	
	@GetMapping("/paynow")
	public void paymentForm(@RequestParam(name="invoiceNo", required=false, defaultValue="") String invoiceId,
			               @RequestParam(name="currency", required=false, defaultValue="") String currency,
			               @RequestParam(name="amount", required=false, defaultValue="") String amount,
			               @RequestParam(name="shortCode", required=false, defaultValue="") String shortCode,
										Model model, HttpSession session) {
		System.out.println("***Inside paymentForm ****"+invoiceId);
//		CreditCardDetails details = new CreditCardDetails();
//		details.setCurrency("INR");
//		model.addAttribute("invoiceId", invoiceId);		
		model.addAttribute("carddetails", new CreditCardDetails());
		session.setAttribute("invoiceId", invoiceId);
		session.setAttribute("currency", currency);
		session.setAttribute("amount", amount);
		session.setAttribute("shortCode",shortCode);
	}
	
	@PostMapping("/paynow")
	public String paymentSubmit(@ModelAttribute CreditCardDetails carddetails,Model model,HttpSession session) {
		
		PaymentResponse gatewayResponse = null;
		try {
			// Validate Invoice
			System.out.println(" Card invoice no from attribute " + model.getAttribute("inv"));
			String invoiceNo = (String)session.getAttribute("invoiceId");
			String amount = (String)session.getAttribute("amount");
			String currency = (String)session.getAttribute("currency");
			System.out.println(" invoiceId "+ invoiceNo);
			System.out.println(" Currency  "+ currency);
			System.out.println("amount  "+ amount);
			System.out.println("ShortCode  "+ (String)session.getAttribute("shortCode"));

			
			xeroService.retrieveTokenfromFile(); // Read the last stored token.
			Invoice invoice = xeroService.getInvoiceDetails(invoiceNo); 
			double amountdue = invoice.getAmountDue();
			if(amountdue==0) {
				System.out.println("***amountdue "+amountdue);
				
				model.addAttribute("cardresponse", paymentService.errorInvoice("INVOICE_ALREADY_PAID", "The invoice is already paid"));
//				PaymentResponse errorResponse = paymentService.errorInvoice("XXX", "The invoice is already paid");
//				System.out.println("***ReasonCode "+errorResponse.getReasonCode() + " : "+errorResponse.getReasonMsg());
				return   "response";
			}
			System.out.println("***Inside submitPayment ****");
			System.out.println(" Card Holder Name "+carddetails.getCardHolderName());
			System.out.println(" Card no "+carddetails.getCardNumber());
			System.out.println(" Card Expirty month/year "+carddetails.getExpirationMonth()+"/"+carddetails.getExpirationYear());
			System.out.println(" Card CVV "+carddetails.getSecurityCode());
			
	
			gatewayResponse = paymentService.processCreditCardPayment(carddetails,invoice,amount, currency);
			
			//Post the payment to Xero
			//Get the account details 
			Account xeroPmnstAcct = xeroService.getAccountDetails();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			
			xeroService.postPaymentToXero(invoice.getInvoiceID().toString(), xeroPmnstAcct.getCode(),formatter.format(date) , amount);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			xeroService.saveTokentoFile(); // Should be the last task to save the token into file.
		}
		model.addAttribute("cardresponse", gatewayResponse);
		return   "response";
	}
}


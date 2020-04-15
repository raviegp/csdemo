package com.cybersource.paymentapp.model;

public class PaymentResponse {
	
	private String status;
	private String reconcilationid;
	private String approvalCode;
	private double approvedAmount;
	private String reasonCode;
	private String reasonMsg;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getReconcilationid() {
		return reconcilationid;
	}
	public void setReconcilationid(String reconcilationid) {
		this.reconcilationid = reconcilationid;
	}
	public String getApprovalCode() {
		return approvalCode;
	}
	public void setApprovalCode(String approvalCode) {
		this.approvalCode = approvalCode;
	}
	public double getApprovedAmount() {
		return approvedAmount;
	}
	public void setApprovedAmount(double approvedAmount) {
		this.approvedAmount = approvedAmount;
	}
	public String getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	public String getReasonMsg() {
		return reasonMsg;
	}
	public void setReasonMsg(String reasonMsg) {
		this.reasonMsg = reasonMsg;
	}
	

}

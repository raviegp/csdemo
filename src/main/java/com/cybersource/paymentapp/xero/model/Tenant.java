package com.cybersource.paymentapp.xero.model;

public class Tenant {

	
    private String id;
    private String tenantId;
    private String tenantType;
	private String createdDateUtc;
    private String updatedDateUtc;
    
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getTenantType() {
		return tenantType;
	}
	public void setTenantType(String tenantType) {
		this.tenantType = tenantType;
	}
	public String getCreatedDateUtc() {
		return createdDateUtc;
	}
	public void setCreatedDateUtc(String createdDateUtc) {
		this.createdDateUtc = createdDateUtc;
	}
	public String getUpdatedDateUtc() {
		return updatedDateUtc;
	}
	public void setUpdatedDateUtc(String updatedDateUtc) {
		this.updatedDateUtc = updatedDateUtc;
	}

	@Override
	public String toString() {
		return "Tenant [id="
	            + id 
	            + ", tenantId="
	            + tenantId 
	            + ", tenantType="
	            + tenantType 
	            + ", createdDateUtc="
	            + createdDateUtc 
	            + ", updatedDateUtc="
	            + updatedDateUtc + "]";
	}
}

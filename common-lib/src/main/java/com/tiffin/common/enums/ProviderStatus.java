package com.tiffin.common.enums;

public enum ProviderStatus {
	
	/**
	 * Approval status of a provider (mess/restaurant).
	 *
	 * Flow:
	 * PENDING → APPROVED (admin approves)
	 * PENDING → REJECTED (admin rejects)
	 * APPROVED → SUSPENDED (admin suspends for violation)
	 */
	
	 PENDING,    // just registered, waiting for admin approval
	    APPROVED,   // admin approved, visible to customers
	    REJECTED,   // admin rejected registration
	    SUSPENDED   // was approved but suspended for violation

}

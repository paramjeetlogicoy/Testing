package com.luvbrite.web.models;

public class OrderPlaceResponse extends GenericResponse {
	
	private boolean paymentProcessed;
	private String paymentError = "";
	private boolean orderFinalizationError;
	
	public boolean isPaymentProcessed() {
		return paymentProcessed;
	}
	public void setPaymentProcessed(boolean paymentProcessed) {
		this.paymentProcessed = paymentProcessed;
	}
	public String getPaymentError() {
		return paymentError;
	}
	public void setPaymentError(String paymentError) {
		this.paymentError = paymentError;
	}
	public boolean isOrderFinalizationError() {
		return orderFinalizationError;
	}
	public void setOrderFinalizationError(boolean orderFinalizationError) {
		this.orderFinalizationError = orderFinalizationError;
	}
}

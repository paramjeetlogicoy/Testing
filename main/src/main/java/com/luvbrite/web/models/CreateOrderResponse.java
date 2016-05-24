package com.luvbrite.web.models;

public class CreateOrderResponse extends GenericResponse {
	
	private int cartCount;
	private int productCount;
	private long orderId;
	private CartOrder order;
	
	public int getCartCount() {
		return cartCount;
	}
	public void setCartCount(int cartCount) {
		this.cartCount = cartCount;
	}
	public int getProductCount() {
		return productCount;
	}
	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public CartOrder getOrder() {
		return order;
	}
	public void setOrder(CartOrder order) {
		this.order = order;
	}
}

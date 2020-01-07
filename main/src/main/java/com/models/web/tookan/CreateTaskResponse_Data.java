/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.models.web.tookan;

/**
 *
 * @author dell
 */
public class CreateTaskResponse_Data {

    private String job_id;
    private String job_hash;
    private String customer_name;
    private String customer_address;
    private String job_token;
    private String tracking_link;
    private String order_id;
    private String deliveryOrderId;
    private boolean pickupAddressNotFound;
    private boolean deliveryAddressNotFound;
    private int job_status;

    public String getCustomer_address() {
        return customer_address;
    }

    public void setCustomer_address(String customer_address) {
        this.customer_address = customer_address;
    }

    public int getJob_status() {
        return job_status;
    }

    public void setJob_status(int job_status) {
        this.job_status = job_status;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getJob_hash() {
        return job_hash;
    }

    public void setJob_hash(String job_hash) {
        this.job_hash = job_hash;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getcustomer_address() {
        return customer_address;
    }

    public void setCustomer_Address(String customer_address) {
        this.customer_address = customer_address;
    }

    public String getJob_token() {
        return job_token;
    }

    public void setJob_token(String job_token) {
        this.job_token = job_token;
    }

    public String getTracking_link() {
        return tracking_link;
    }

    public void setTracking_link(String tracking_link) {
        this.tracking_link = tracking_link;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getDeliveryOrderId() {
        return deliveryOrderId;
    }

    public void setDeliveryOrderId(String deliveryOrderId) {
        this.deliveryOrderId = deliveryOrderId;
    }

    public boolean isPickupAddressNotFound() {
        return pickupAddressNotFound;
    }

    public void setPickupAddressNotFound(boolean pickupAddressNotFound) {
        this.pickupAddressNotFound = pickupAddressNotFound;
    }

    public boolean isDeliveryAddressNotFound() {
        return deliveryAddressNotFound;
    }

    public void setDeliveryAddressNotFound(boolean deliveryAddressNotFound) {
        this.deliveryAddressNotFound = deliveryAddressNotFound;
    }

}

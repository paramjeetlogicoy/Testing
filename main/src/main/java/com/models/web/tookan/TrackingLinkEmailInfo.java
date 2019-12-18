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
public class TrackingLinkEmailInfo {

    private String orderTotal;
    private String orderDate ;
    private String trackingLink ;
    private String recipentName ;
    private String recipentEmail ;
    private String orderNumber ;

    public String getRecipentEmail() {
        return recipentEmail;
    }

    public void setRecipentEmail(String recipentEmail) {
        this.recipentEmail = recipentEmail;
    }

    public String getRecipentName() {
        return recipentName;
    }

    public void setRecipentName(String recipentName) {
        this.recipentName = recipentName;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(String orderTotal) {
        this.orderTotal = orderTotal;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getTrackingLink() {
        return trackingLink;
    }

    public void setTrackingLink(String trackingLink) {
        this.trackingLink = trackingLink;
    }

}

package com.example.smartshopsaver.models;

public class ModelOrder {
    String orderId;
    long timestamp;
    String latitude;
    String longitude;
    String orderBy;
    String buyerName;
    String buyerFcmToken;
    String orderTo;
    String sellerName;
    String orderStatus;
    double deliveryCharges;
    double subTotal;
    double total;


    public ModelOrder() {

    }


    public ModelOrder(String orderId, long timestamp, String orderBy, String buyerName, String buyerFcmToken, String orderTo, String sellerName, String orderStatus, double deliveryCharges, double subTotal, double total) {
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.orderBy = orderBy;
        this.buyerName = buyerName;
        this.buyerFcmToken = buyerFcmToken;
        this.orderTo = orderTo;
        this.sellerName = sellerName;
        this.orderStatus = orderStatus;
        this.deliveryCharges = deliveryCharges;
        this.subTotal = subTotal;
        this.total = total;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerFcmToken() {
        return buyerFcmToken;
    }

    public void setBuyerFcmToken(String buyerFcmToken) {
        this.buyerFcmToken = buyerFcmToken;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo(String orderTo) {
        this.orderTo = orderTo;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(double deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}

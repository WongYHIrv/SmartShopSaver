package com.example.smartshopsaver.models;

import java.util.ArrayList;

public class ModelCart {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String sellerUid = "";
    String sellerFcmToken = "";
    String shopName = "";
    long timestamp = 0;
    double deliveryFee;
    double subTotal = 0.0;
    double total = 0.0;
    ArrayList<ModelCartProduct> cartProductArrayList;

    /*---Empty constructor require for firebase db---*/
    public ModelCart() {

    }

    /*---Constructor with all params---*/
    public ModelCart(String sellerUid, String sellerFcmToken, String shopName, long timestamp, double deliveryFee, double subTotal, double total, ArrayList<ModelCartProduct> cartProductArrayList) {
        this.sellerUid = sellerUid;
        this.sellerFcmToken = sellerFcmToken;
        this.shopName = shopName;
        this.timestamp = timestamp;
        this.deliveryFee = deliveryFee;
        this.subTotal = subTotal;
        this.total = total;
        this.cartProductArrayList = cartProductArrayList;
    }

    /*---Getter & Setters---*/
    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }

    public String getSellerFcmToken() {
        return sellerFcmToken;
    }

    public void setSellerFcmToken(String sellerFcmToken) {
        this.sellerFcmToken = sellerFcmToken;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getDeliveryFee() {
        return deliveryFee;
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

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public ArrayList<ModelCartProduct> getCartProductArrayList() {
        return cartProductArrayList;
    }

    public void setCartProductArrayList(ArrayList<ModelCartProduct> cartProductArrayList) {
        this.cartProductArrayList = cartProductArrayList;
    }
}

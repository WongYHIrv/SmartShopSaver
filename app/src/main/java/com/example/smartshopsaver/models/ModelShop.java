package com.example.smartshopsaver.models;

public class ModelShop {
    String uid;
    String fcmToken;
    String email;
    String name;
    String shopName;
    String shopDescription;
    String deliveryFee;
    String phone;
    String country;
    String state;
    String city;
    String address;
    String latitude;
    String longitude;
    String timestamp;
    String accountType;
    String online;
    String profileImage;

    //For product comparison
    String comparingProductId = "";
    double comparingProductPrice = 0.0;
    long comparingProductTimestamp = 0;

    public ModelShop() {

    }

    public ModelShop(String uid, String fcmToken, String email, String name, String shopName, String shopDescription, String deliveryFee, String phone, String country, String state, String city, String address, String latitude, String longitude, String timestamp, String accountType, String online, String profileImage, String comparingProductId, double comparingProductPrice, long comparingProductTimestamp) {
        this.uid = uid;
        this.fcmToken = fcmToken;
        this.email = email;
        this.name = name;
        this.shopName = shopName;
        this.shopDescription = shopDescription;
        this.deliveryFee = deliveryFee;
        this.phone = phone;
        this.country = country;
        this.state = state;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.accountType = accountType;
        this.online = online;
        this.profileImage = profileImage;
        this.comparingProductId = comparingProductId;
        this.comparingProductPrice = comparingProductPrice;
        this.comparingProductTimestamp = comparingProductTimestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public void setShopDescription(String shopDescription) {
        this.shopDescription = shopDescription;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getComparingProductId() {
        return comparingProductId;
    }

    public void setComparingProductId(String comparingProductId) {
        this.comparingProductId = comparingProductId;
    }

    public double getComparingProductPrice() {
        return comparingProductPrice;
    }

    public void setComparingProductPrice(double comparingProductPrice) {
        this.comparingProductPrice = comparingProductPrice;
    }

    public long getComparingProductTimestamp() {
        return comparingProductTimestamp;
    }

    public void setComparingProductTimestamp(long comparingProductTimestamp) {
        this.comparingProductTimestamp = comparingProductTimestamp;
    }
}

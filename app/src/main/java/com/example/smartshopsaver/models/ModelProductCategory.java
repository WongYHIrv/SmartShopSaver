package com.example.smartshopsaver.models;

public class ModelProductCategory {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String productCategoryId;
    String productCategory;
    long timestamp;
    String uid;

    /*---Empty constructor require for firebase db---*/
    public ModelProductCategory() {

    }

    /*---Constructor with all params---*/
    public ModelProductCategory(String productCategoryId, String productCategory, long timestamp, String uid) {
        this.productCategoryId = productCategoryId;
        this.productCategory = productCategory;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    /*---Getter & Setters---*/
    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

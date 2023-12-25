package com.example.smartshopsaver.models;

public class ModelProductSubCategory {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String productCategoryId;
    String productSubCategory;
    String productSubCategoryId;
    long timestamp;
    String uid;

    /*---Empty constructor require for firebase db---*/
    public ModelProductSubCategory() {

    }

    /*---Constructor with all params---*/

    public ModelProductSubCategory(String productCategoryId, String productSubCategory, String productSubCategoryId, long timestamp, String uid) {
        this.productCategoryId = productCategoryId;
        this.productSubCategory = productSubCategory;
        this.productSubCategoryId = productSubCategoryId;
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

    public String getProductSubCategory() {
        return productSubCategory;
    }

    public void setProductSubCategory(String productSubCategory) {
        this.productSubCategory = productSubCategory;
    }

    public String getProductSubCategoryId() {
        return productSubCategoryId;
    }

    public void setProductSubCategoryId(String productSubCategoryId) {
        this.productSubCategoryId = productSubCategoryId;
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

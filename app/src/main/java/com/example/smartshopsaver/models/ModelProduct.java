package com.example.smartshopsaver.models;

public class ModelProduct {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String productId = "";
    String productName = "";
    String productDescription = "";
    String productBarcode = "";
    String productCategoryId = "";
    String productSubCategoryId = "";
    String productCategory = "";
    String productSubCategory = "";
    Double productPrice = 0.0;
    long productStock = 0;
    String productExpireDate = "";
    boolean discountAvailable = false;
    Double productDiscountPrice = 0.0;
    String productDiscountNote = "";
    String imageUrl = "";
    long timestamp = 0;
    String uid = "";
    boolean expired;

    /*---Empty constructor require for firebase db---*/
    public ModelProduct() {

    }

    /*---Constructor with all params---*/

    public ModelProduct(String productId, String productName, String productDescription, String productBarcode, String productCategoryId, String productSubCategoryId, String productCategory, String productSubCategory, Double productPrice, long productStock, String productExpireDate, boolean discountAvailable, Double productDiscountPrice, String productDiscountNote, String imageUrl, long timestamp, String uid, boolean expired) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productBarcode = productBarcode;
        this.productCategoryId = productCategoryId;
        this.productSubCategoryId = productSubCategoryId;
        this.productCategory = productCategory;
        this.productSubCategory = productSubCategory;
        this.productPrice = productPrice;
        this.productStock = productStock;
        this.productExpireDate = productExpireDate;
        this.discountAvailable = discountAvailable;
        this.productDiscountPrice = productDiscountPrice;
        this.productDiscountNote = productDiscountNote;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.uid = uid;
        this.expired = expired;
    }

    /*---Getter & Setters---*/
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductBarcode() {
        return productBarcode;
    }

    public void setProductBarcode(String productBarcode) {
        this.productBarcode = productBarcode;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getProductSubCategoryId() {
        return productSubCategoryId;
    }

    public void setProductSubCategoryId(String productSubCategoryId) {
        this.productSubCategoryId = productSubCategoryId;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductSubCategory() {
        return productSubCategory;
    }

    public void setProductSubCategory(String productSubCategory) {
        this.productSubCategory = productSubCategory;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public long getProductStock() {
        return productStock;
    }

    public void setProductStock(long productStock) {
        this.productStock = productStock;
    }

    public String getProductExpireDate() {
        return productExpireDate;
    }

    public void setProductExpireDate(String productExpireDate) {
        this.productExpireDate = productExpireDate;
    }

    public boolean isDiscountAvailable() {
        return discountAvailable;
    }

    public void setDiscountAvailable(boolean discountAvailable) {
        this.discountAvailable = discountAvailable;
    }

    public Double getProductDiscountPrice() {
        return productDiscountPrice;
    }

    public void setProductDiscountPrice(Double productDiscountPrice) {
        this.productDiscountPrice = productDiscountPrice;
    }

    public String getProductDiscountNote() {
        return productDiscountNote;
    }

    public void setProductDiscountNote(String productDiscountNote) {
        this.productDiscountNote = productDiscountNote;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}

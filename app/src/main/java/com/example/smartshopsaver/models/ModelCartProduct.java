package com.example.smartshopsaver.models;

public class ModelCartProduct {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String productId = "";
    String productName = "";
    String productDescription = "";
    String productCategoryId = "";
    String productSubCategoryId = "";
    String productCategory = "";
    String productSubCategory = "";
    String productPrice = "";
    String productStock = "";
    String productExpireDate = "";
    boolean discountAvailable = false;
    String productDiscountPrice = "";
    String productDiscountNote = "";
    String imageUrl = "";
    long timestamp = 0;
    String sellerUid = "";
    long productQuantity = 0;

    /*---Empty constructor require for firebase db---*/
    public ModelCartProduct() {

    }

    /*---Constructor with all params---*/
    public ModelCartProduct(String productId, String productName, String productDescription, String productCategoryId, String productSubCategoryId, String productCategory, String productSubCategory, String productPrice, String productStock, String productExpireDate, boolean discountAvailable, String productDiscountPrice, String productDiscountNote, String imageUrl, long timestamp, String sellerUid, long productQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
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
        this.sellerUid = sellerUid;
        this.productQuantity = productQuantity;
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

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductStock() {
        return productStock;
    }

    public void setProductStock(String productStock) {
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

    public String getProductDiscountPrice() {
        return productDiscountPrice;
    }

    public void setProductDiscountPrice(String productDiscountPrice) {
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

    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }

    public long getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(long productQuantity) {
        this.productQuantity = productQuantity;
    }
}

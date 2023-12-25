package com.example.smartshopsaver.models;

public class ModelOrderProduct {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String productId = "";
    String productName = "";
    String productDescription = "";
    String imageUrl = "";
    String productPrice = "";
    long productQuantity = 0;
    String sellerUid = "";


    /*---Empty constructor require for firebase db---*/
    public ModelOrderProduct() {

    }

    /*---Constructor with all params---*/

    public ModelOrderProduct(String productId, String productName, String productDescription, String imageUrl, String productPrice, long productQuantity, String sellerUid) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.imageUrl = imageUrl;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.sellerUid = sellerUid;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public long getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(long productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }
}

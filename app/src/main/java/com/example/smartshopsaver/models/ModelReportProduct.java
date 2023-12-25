package com.example.smartshopsaver.models;

public class ModelReportProduct {
    String id;
    String details;
    String productId;
    String productName;
    String productImage;
    String reportByUid;
    String reporterName;
    String sellerUid;
    String sellerName;
    long timestamp;


    public ModelReportProduct() {

    }

    public ModelReportProduct(String id, String details, String productId, String productName, String productImage, String reportByUid, String reporterName, String sellerUid, String sellerName, long timestamp) {
        this.id = id;
        this.details = details;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.reportByUid = reportByUid;
        this.reporterName = reporterName;
        this.sellerUid = sellerUid;
        this.sellerName = sellerName;
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getReportByUid() {
        return reportByUid;
    }

    public void setReportByUid(String reportByUid) {
        this.reportByUid = reportByUid;
    }

    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

package com.example.smartshopsaver.models;

public class ModelReportOrder {
    String id;
    String details;
    String orderId;
    String reportByUid;
    String reporterName;
    String sellerUid;
    String sellerName;
    long timestamp;

    public ModelReportOrder() {

    }

    public ModelReportOrder(String id, String details, String orderId, String reportByUid, String reporterName, String sellerUid, String sellerName, long timestamp) {
        this.id = id;
        this.details = details;
        this.orderId = orderId;
        this.reportByUid = reportByUid;
        this.reporterName = reporterName;
        this.sellerUid = sellerUid;
        this.sellerName = sellerName;
        this.timestamp = timestamp;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReportByUid() {
        return reportByUid;
    }

    public void setReportByUid(String reportByUid) {
        this.reportByUid = reportByUid;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

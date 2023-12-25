package com.example.smartshopsaver.models;

public class ModelBrochureCategory {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String id;
    String brochureCategory;
    long timestamp;
    String uid;

    /*---Empty constructor require for firebase db---*/
    public ModelBrochureCategory() {

    }

    /*---Constructor with all params---*/
    public ModelBrochureCategory(String id, String brochureCategory, long timestamp, String uid) {
        this.id = id;
        this.brochureCategory = brochureCategory;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    /*---Getter & Setters---*/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrochureCategory() {
        return brochureCategory;
    }

    public void setBrochureCategory(String brochureCategory) {
        this.brochureCategory = brochureCategory;
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

package com.example.smartshopsaver.models;

import java.io.File;

public class ModelBrochure {

    /*---Variables. spellings and case should be same as in firebase db---*/
    String brochureId = "";
    String brochureCategoryId = "";
    String brochureCategory = "";
    String brochureName = "";
    String brochureDescription = "";
    long viewsCount = 0;
    long downloadsCount = 0;
    String uid = "";
    String pdfUrl = "";
    File pdfFile;
    long timestamp = 0;

    /*---Empty constructor require for firebase db---*/
    public ModelBrochure() {

    }

    /*---Constructor with all params---*/
    public ModelBrochure(String brochureId, String brochureCategoryId, String brochureCategory, String brochureName, String brochureDescription, long viewsCount, long downloadsCount, String uid, String pdfUrl, File pdfFile, long timestamp) {
        this.brochureId = brochureId;
        this.brochureCategoryId = brochureCategoryId;
        this.brochureCategory = brochureCategory;
        this.brochureName = brochureName;
        this.brochureDescription = brochureDescription;
        this.viewsCount = viewsCount;
        this.downloadsCount = downloadsCount;
        this.uid = uid;
        this.pdfUrl = pdfUrl;
        this.pdfFile = pdfFile;
        this.timestamp = timestamp;
    }

    /*---Getter & Setters---*/
    public String getBrochureId() {
        return brochureId;
    }

    public void setBrochureId(String brochureId) {
        this.brochureId = brochureId;
    }

    public String getBrochureCategoryId() {
        return brochureCategoryId;
    }

    public void setBrochureCategoryId(String brochureCategoryId) {
        this.brochureCategoryId = brochureCategoryId;
    }

    public String getBrochureCategory() {
        return brochureCategory;
    }

    public void setBrochureCategory(String brochureCategory) {
        this.brochureCategory = brochureCategory;
    }

    public String getBrochureName() {
        return brochureName;
    }

    public void setBrochureName(String brochureName) {
        this.brochureName = brochureName;
    }

    public String getBrochureDescription() {
        return brochureDescription;
    }

    public void setBrochureDescription(String brochureDescription) {
        this.brochureDescription = brochureDescription;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public long getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(long downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(File pdfFile) {
        this.pdfFile = pdfFile;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

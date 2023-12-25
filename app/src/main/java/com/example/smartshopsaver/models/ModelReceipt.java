package com.example.smartshopsaver.models;

public class ModelReceipt {

    //Variables
    String title, description, location, uid, url, dateReceipt, username, userEmail, id;

    double expensesAmt;

    long timestamp;

    public ModelReceipt() {

    }

    public ModelReceipt(String title, String description, String location, String uid, String url, String dateReceipt, String username, String userEmail, String id, double expensesAmt, long timestamp) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.uid = uid;
        this.url = url;
        this.dateReceipt = dateReceipt;
        this.username = username;
        this.userEmail = userEmail;
        this.id = id;
        this.expensesAmt = expensesAmt;
        this.timestamp = timestamp;
    }

    //Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDateReceipt() {
        return dateReceipt;
    }

    public void setDateReceipt(String dateReceipt) {
        this.dateReceipt = dateReceipt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getExpensesAmt() {
        return expensesAmt;
    }

    public void setExpenseAmt(double expensesAmt) {
        this.expensesAmt = expensesAmt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

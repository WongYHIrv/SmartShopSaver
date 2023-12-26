package com.example.smartshopsaver;

public class Constants {

    public static final String FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY";
    public static final String NOTIFICATION_TYPE_ORDER_NEW = "NOTIFICATION_TYPE_ORDER_NEW";
    public static final String NOTIFICATION_TYPE_ORDER_UPDATE = "NOTIFICATION_TYPE_ORDER_UPDATE";
    public static final String NOTIFICATION_TYPE_REPORT_PRODUCT = "NOTIFICATION_TYPE_REPORT_PRODUCT";
    public static final String NOTIFICATION_TYPE_REPORT_ORDER = "NOTIFICATION_TYPE_REPORT_ORDER";


    public static final String USER_TYPE_USER = "USER";
    public static final String USER_TYPE_SELLER = "SELLER";
    public static final String USER_TYPE_ADMIN = "ADMIN";



    public static final String ORDER_STATUS_PLACED = "Placed";
    public static final String ORDER_STATUS_IN_PROGRESS = "In Progress";
    public static final String ORDER_STATUS_OUT_FOR_DELIVERY = "Out For Delivery";
    public static final String ORDER_STATUS_DELIVERED = "Delivered";
    public static final String ORDER_STATUS_CANCELLED = "Cancelled";



    public static String PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_DETAILS;
    public static String PRODUCT_ACTION_TYPE_DETAILS = "OPEN_DETAILS";
    public static String PRODUCT_ACTION_TYPE_COMPARISON = "OPEN_COMPARISON";



    public static final long MAX_BYTES_PDF = 50000000; //50MB

}

package com.example.smartshopsaver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.smartshopsaver.activities.OrderDetailsSellerActivity;
import com.example.smartshopsaver.activities.OrderDetailsUserActivity;
import com.example.smartshopsaver.activities.ReportOrderDetailsAdminActivity;
import com.example.smartshopsaver.activities.ReportProductDetailsAdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFcmService extends FirebaseMessagingService {
    //Tag to show logs  in logcat
    private static final String TAG = "FCM_SERVICE_TAG";
    //Notification Channel ID
    private static final String ADMIN_CHANNEL_ID = "ADMIN_CHANNEL_ID";

    private FirebaseAuth firebaseAuth;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        firebaseAuth = FirebaseAuth.getInstance();

        //Get data to show in notification
        String title = "" + remoteMessage.getNotification().getTitle();
        String body = "" + remoteMessage.getNotification().getBody();
        String senderUid = "" + remoteMessage.getData().get("senderUid");
        String notificationType = "" + remoteMessage.getData().get("notificationType");

        Log.d(TAG, "onMessageReceived: title: " + title);
        Log.d(TAG, "onMessageReceived: body: " + body);
        Log.d(TAG, "onMessageReceived: senderUid: " + senderUid);
        Log.d(TAG, "onMessageReceived: notificationType: " + notificationType);

        if (notificationType.equals(Constants.NOTIFICATION_TYPE_ORDER_NEW)) {
            String sendToUid = "" + remoteMessage.getData().get("sendToUid");
            String orderId = "" + remoteMessage.getData().get("orderId");
            Log.d(TAG, "onMessageReceived: sendToUid: " + sendToUid);
            Log.d(TAG, "onMessageReceived: orderId: " + orderId);
            //function call to show notification
            if (firebaseAuth.getCurrentUser() != null) {
                String myUid = "" + firebaseAuth.getUid();
                if (myUid.equals(sendToUid)) {
                    showOrderNewNotification(title, body, senderUid, orderId);
                }
            }
        } else if (notificationType.equals(Constants.NOTIFICATION_TYPE_ORDER_UPDATE)) {
            String sendToUid = "" + remoteMessage.getData().get("sendToUid");
            String orderId = "" + remoteMessage.getData().get("orderId");
            String orderStatus = "" + remoteMessage.getData().get("orderStatus");
            Log.d(TAG, "onMessageReceived: sendToUid: " + sendToUid);
            Log.d(TAG, "onMessageReceived: orderId: " + orderId);
            Log.d(TAG, "onMessageReceived: orderStatus: " + orderStatus);
            //function call to show notification
            if (firebaseAuth.getCurrentUser() != null) {
                String myUid = "" + firebaseAuth.getUid();
                if (myUid.equals(sendToUid)) {
                    showOrderUpdateNotification(title, body, senderUid, orderId, orderStatus);
                }
            }
        } else if (notificationType.equals(Constants.NOTIFICATION_TYPE_REPORT_PRODUCT)) {
            String reportId = "" + remoteMessage.getData().get("reportId");
            //function call to show notification
            if (firebaseAuth.getCurrentUser() != null) {
                showReportProductNotification(title, body, senderUid, reportId);
            }
        } else if (notificationType.equals(Constants.NOTIFICATION_TYPE_REPORT_ORDER)) {
            String reportId = "" + remoteMessage.getData().get("reportId");
            //function call to show notification
            if (firebaseAuth.getCurrentUser() != null) {
                showReportOrderNotification(title, body, senderUid, reportId);
            }
        }
    }


    private void showOrderNewNotification(String notificationTitle, String notificationDescription, String senderUid, String orderId) {
        //Generate random integer between 3000 to use as notification id
        int notificationId = new Random().nextInt(3000);

        //init NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //function call to setup notification  channel in case of Android O and above
        setupNotificationChannel(notificationManager);

        //Intet to launch ChatActivity when notification is clicked
        Intent intent = new Intent(this, OrderDetailsSellerActivity.class);
        intent.putExtra("orderId", orderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent to add in notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //Setup Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        //Show Notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void showOrderUpdateNotification(String notificationTitle, String notificationDescription, String senderUid, String orderId, String orderStatus) {
        //Generate random integer between 3000 to use as notification id
        int notificationId = new Random().nextInt(3000);

        //init NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //function call to setup notification  channel in case of Android O and above
        setupNotificationChannel(notificationManager);

        //Intent to launch ChatActivity when notification is clicked
        Intent intent = new Intent(this, OrderDetailsUserActivity.class);
        intent.putExtra("orderId", orderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent to add in notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //Setup Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        //Show Notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void showReportProductNotification(String notificationTitle, String notificationDescription, String senderUid, String reportId) {
        //Generate random integer between 3000 to use as notification id
        int notificationId = new Random().nextInt(3000);

        //init NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //function call to setup notification  channel in case of Android O and above
        setupNotificationChannel(notificationManager);

        //Intent to launch ChatActivity when notification is clicked
        Intent intent = new Intent(this, ReportProductDetailsAdminActivity.class);
        intent.putExtra("reportId", reportId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent to add in notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //Setup Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        //Show Notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void showReportOrderNotification(String notificationTitle, String notificationDescription, String senderUid, String reportId) {
        //Generate random integer between 3000 to use as notification id
        int notificationId = new Random().nextInt(3000);

        //init NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //function call to setup notification  channel in case of Android O and above
        setupNotificationChannel(notificationManager);

        //Intent to launch ChatActivity when notification is clicked
        Intent intent = new Intent(this, ReportOrderDetailsAdminActivity.class);
        intent.putExtra("reportId", reportId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent to add in notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //Setup Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        //Show Notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void setupNotificationChannel(NotificationManager notificationManager) {
        //Starting in Android 8.0 (API level 26), all notifications must be assigned to a channel https://developer.android.com/develop/ui/views/notifications/channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    ADMIN_CHANNEL_ID,
                    "CHAT_CHANNEL",
                    NotificationManager.IMPORTANCE_HIGH
            );

            notificationChannel.setDescription("Show Chat Notifications.");
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.databinding.ActivityReportOrderUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportOrderUserActivity extends AppCompatActivity {

    private ActivityReportOrderUserBinding binding;

    private static final String TAG = "REPORT_PRODUCT_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private String orderId = "";
    private String sellerUid = "";

    private ArrayList<String> adminFcmTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportOrderUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get product id from intent
        orderId = getIntent().getStringExtra("orderId");
        sellerUid = getIntent().getStringExtra("sellerUid");

        loadFcmTokens();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        //handle submitBtn click, validate data and submit report
        binding.submitBtn.setOnClickListener(view -> validateData());

    }

    private void loadFcmTokens() {
        adminFcmTokens = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo(Constants.USER_TYPE_ADMIN)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String fcmToken = "" + ds.child("fcmToken").getValue();
                            Log.d(TAG, "onDataChange: fcmToken: " + fcmToken);

                            //add to list
                            adminFcmTokens.add(fcmToken);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String details = "";

    private void validateData() {
        //get data
        details = binding.detailEt.getText().toString().trim();

        //validate data
        if (details.isEmpty()) {
            binding.detailEt.setError("Enter details...!");
            binding.detailEt.requestFocus();
        } else {
            submitReport();
        }
    }

    private void submitReport() {
        progressDialog.setMessage("Submitting Report...!");
        progressDialog.show();

        long timestamp = MyUtils.getTimestamp();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("timestamp", timestamp);
        hashMap.put("orderId", "" + orderId);
        hashMap.put("sellerUid", "" + sellerUid);
        hashMap.put("reportByUid", "" + firebaseAuth.getUid());
        hashMap.put("details", "" + details);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportOrders");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    MyUtils.toast(ReportOrderUserActivity.this, "Report Submitted...!");

                    for (String adminFcmToken : adminFcmTokens) {
                        prepareNotification(timestamp, adminFcmToken);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    MyUtils.toast(ReportOrderUserActivity.this, "Failed due to " + e.getMessage());
                });
    }


    private void prepareNotification(long timestamp, String adminFcmToken) {
        Log.d(TAG, "prepareNotification: ");
        //prepare json what to send, and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationDataJo = new JSONObject();
        JSONObject notificationNotificationJo = new JSONObject();

        try {
            //extra/custom data
            notificationDataJo.put("notificationType", "" + Constants.NOTIFICATION_TYPE_REPORT_ORDER);
            notificationDataJo.put("senderUid", "" + firebaseAuth.getUid());
            notificationDataJo.put("reportId", "" + timestamp);
            //title, description, sound
            notificationNotificationJo.put("title", "Order Report"); //"title" is reserved name in FCM API so be careful while typing
            notificationNotificationJo.put("body", "A user has reported an order: " + timestamp + "\n" + details); //"body" is reserved name in FCM API so be careful while typing
            notificationNotificationJo.put("sound", "default"); //"sound" is reserved name in FCM API so be careful while typing
            //combine all data in single JSON object
            notificationJo.put("to", "" + adminFcmToken); //"to" is reserved name in FCM API so be careful while typing
            notificationJo.put("notification", notificationNotificationJo); //"notification" is reserved name in FCM API so be careful while typing
            notificationJo.put("data", notificationDataJo);  //"data" is reserved name in FCM API so be careful while typing
        } catch (Exception e) {
            Log.e(TAG, "prepareNotification: ", e);
        }

        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //Prepare JSON Object Request to enqueue
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://fcm.googleapis.com/fcm/send",
                notificationJo,
                response -> {
                    //Notification sent
                    Log.d(TAG, "sendFcmNotification: " + response.toString());
                },
                error -> {
                    //Notification failed to send
                    Log.e(TAG, "sendFcmNotification: ", error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); //"Content-Type" is reserved name in Volley Networking API/Library
                headers.put("Authorization", "key=" + Constants.FCM_SERVER_KEY); //"Authorization" is reserved name in Volley Networking API/Library, value against it must be like "key=fcm_server_key_here"

                return headers;
            }
        };

        //enqueue the JSON Object Request
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }
}
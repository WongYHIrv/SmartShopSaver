package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityReportOrderDetailsAdminBinding;
import com.example.smartshopsaver.models.ModelReportOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReportOrderDetailsAdminActivity extends AppCompatActivity {

    private ActivityReportOrderDetailsAdminBinding binding;

    private static final String TAG = "REPORT_P_DETAILS_TAG";

    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportOrderDetailsAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reportId = getIntent().getStringExtra("reportId");

        loadReportOrderInfo();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());
    }

    private void loadReportOrderInfo() {
        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportOrders");
        ref.child(reportId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelReportOrder modelReportProduct = snapshot.getValue(ModelReportOrder.class);

                            String details = modelReportProduct.getDetails();
                            long timestamp = modelReportProduct.getTimestamp();

                            String formattedDate = MyUtils.formatTimestampDate(timestamp);

                            loadBuyerInfo(modelReportProduct);
                            loadSellerInfo(modelReportProduct);
                            loadOrderInfo(modelReportProduct);

                            binding.dateTv.setText(formattedDate);
                            binding.reportTv.setText(details);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadSellerInfo(ModelReportOrder modelOrder) {
        String sellerUid = "" + modelOrder.getSellerUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        modelOrder.setSellerName(shopName);

                        binding.sellerTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo(ModelReportOrder modelOrder) {
        String reporterUid = "" + modelOrder.getReportByUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(reporterUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String reporterName = "" + snapshot.child("name").getValue();
                        modelOrder.setReporterName(reporterName);

                        binding.reporterTv.setText(reporterName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderInfo(ModelReportOrder modelReportProduct) {
        String orderId = "" + modelReportProduct.getOrderId();

        binding.orderIdTv.setText(orderId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
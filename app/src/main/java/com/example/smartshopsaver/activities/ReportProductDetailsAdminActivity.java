package com.example.smartshopsaver.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityReportProductDetailsAdminBinding;
import com.example.smartshopsaver.models.ModelReportProduct;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReportProductDetailsAdminActivity extends AppCompatActivity {

    private ActivityReportProductDetailsAdminBinding binding;

    private static final String TAG = "REPORT_P_DETAILS_TAG";

    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportProductDetailsAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reportId = getIntent().getStringExtra("reportId");

        loadReportProductInfo();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());
    }

    private void loadReportProductInfo() {
        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportProducts");
        ref.child(reportId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelReportProduct modelReportProduct = snapshot.getValue(ModelReportProduct.class);

                            String details = modelReportProduct.getDetails();
                            long timestamp = modelReportProduct.getTimestamp();

                            String formattedDate = MyUtils.formatTimestampDate(timestamp);

                            loadBuyerInfo(modelReportProduct);
                            loadSellerInfo(modelReportProduct);
                            loadProductInfo(modelReportProduct);

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


    private void loadSellerInfo(ModelReportProduct modelOrder) {
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

    private void loadBuyerInfo(ModelReportProduct modelOrder) {
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

    private void loadProductInfo(ModelReportProduct modelReportProduct) {
        String productId = "" + modelReportProduct.getProductId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productName = "" + snapshot.child("productName").getValue();
                        String imageUrl = "" + snapshot.child("imageUrl").getValue();

                        modelReportProduct.setProductName(productName);
                        modelReportProduct.setProductImage(imageUrl);

                        binding.productNameTv.setText(productName);

                        try {
                            Glide.with(ReportProductDetailsAdminActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.cart_white)
                                    .into(binding.productImageIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
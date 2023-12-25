package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.adapters.AdapterOrderProductUser;
import com.example.smartshopsaver.databinding.ActivityOrderDetailsSellerBinding;
import com.example.smartshopsaver.models.ModelOrderProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private ActivityOrderDetailsSellerBinding binding;

    private static final String TAG = "ORDER_DETAILS_TAG";

    private FirebaseAuth firebaseAuth;

    private String orderId = "";
    private String buyerLatitude = "";
    private String buyerLongitude = "";

    private ArrayList<ModelOrderProduct> orderProductArrayList;
    private AdapterOrderProductUser adapterOderProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("orderId");
        Log.d(TAG, "onCreate: orderId: " + orderId);

        firebaseAuth = FirebaseAuth.getInstance();

        orderDetails();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        //handle toolbarMapBtn toolbarMapBtn, open buyer location in map
        binding.toolbarMapBtn.setOnClickListener(view -> {
            MyUtils.openMap(this, ""+buyerLatitude, ""+buyerLongitude);
        });
    }

    private void orderDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.child("" + orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String orderId = "" + snapshot.child("orderId").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String orderBy = "" + snapshot.child("orderBy").getValue();
                        String orderTo = "" + snapshot.child("orderTo").getValue();
                        String orderStatus = "" + snapshot.child("orderStatus").getValue();
                        String deliveryCharges = "" + snapshot.child("deliveryCharges").getValue();
                        String subTotal = "" + snapshot.child("subTotal").getValue();
                        String total = "" + snapshot.child("total").getValue();

                        if (total.equals("") || total.equals("null")) {
                            total = "0.0";
                        }

                        if (timestamp.equals("") || timestamp.equals("null")) {
                            timestamp = "" + MyUtils.getTimestamp();
                        }

                        String formattedDate = MyUtils.formatTimestampDate(Long.parseLong(timestamp));

                        loadBuyerInfo(orderBy);

                        binding.orderIdTv.setText(orderId);
                        binding.orderStatusTv.setText(orderStatus);
                        binding.deliveryPriceTv.setText(deliveryCharges);
                        binding.priceTv.setText(MyUtils.roundedDecimalValue(Double.parseDouble(total)));
                        binding.dateTv.setText(formattedDate);

                        //Load Order Products
                        orderProductArrayList = new ArrayList<>();
                        snapshot.getRef().child("Products")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        orderProductArrayList.clear();
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            Log.d(TAG, "onDataChange: ");
                                            try {
                                                ModelOrderProduct modelOrderProduct = ds.getValue(ModelOrderProduct.class);
                                                orderProductArrayList.add(modelOrderProduct);
                                            } catch (Exception e) {
                                                Log.e(TAG, "onDataChange: ", e);
                                            }
                                        }
                                        adapterOderProduct = new AdapterOrderProductUser(OrderDetailsSellerActivity.this, orderProductArrayList);
                                        binding.orderProductsRv.setAdapter(adapterOderProduct);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo(String orderBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "" + snapshot.child("name").getValue();
                        buyerLatitude = "" + snapshot.child("latitude").getValue();
                        buyerLongitude = "" + snapshot.child("longitude").getValue();
                        Log.d(TAG, "onDataChange: buyerLatitude: "+buyerLatitude);
                        Log.d(TAG, "onDataChange: buyerLongitude: "+buyerLongitude);

                        binding.buyerNameTv.setText(name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
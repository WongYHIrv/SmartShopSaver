package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.smartshopsaver.R;
import com.example.smartshopsaver.adapters.AdapterProductUser;
import com.example.smartshopsaver.databinding.ActivityOrderDetailsUserBinding;
import com.example.smartshopsaver.databinding.ActivityShopDetailsUserBinding;
import com.example.smartshopsaver.models.ModelProduct;
import com.example.smartshopsaver.models.ModelShop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShopDetailsUserActivity extends AppCompatActivity {

    private ActivityShopDetailsUserBinding binding;

    private static final String TAG = "SHOP_DETAILS_TAG";

    private String shopId = "";

    private ArrayList<ModelProduct> productArrayList;
    private AdapterProductUser adapterProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailsUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopId = "" + getIntent().getStringExtra("shopId");

        loadShopDetails();
        loadProducts();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelShop modelShop = snapshot.getValue(ModelShop.class);

                            String shopName = "" + modelShop.getShopName();
                            String name = "" + modelShop.getName();
                            String shopDescription = "" + modelShop.getShopDescription();

                            binding.shopNameTv.setText(shopName);
                            binding.shopOwnerTv.setText(name);
                            binding.descriptionTv.setText(shopDescription);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProducts() {
        //init list before starting adding data into it
        productArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.orderByChild("uid").equalTo(shopId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it, each time there is a change in list
                        productArrayList.clear();

                        //load data into list from firebase db
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                ModelProduct modelBrochureCategory = ds.getValue(ModelProduct.class);
                                productArrayList.add(modelBrochureCategory);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        //init/setup adapter and set to recyclerview
                        adapterProductSeller = new AdapterProductUser(ShopDetailsUserActivity.this, productArrayList);
                        binding.orderProductsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}
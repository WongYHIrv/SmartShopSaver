package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.smartshopsaver.adapters.AdapterProductSubCategoryAdmin;
import com.example.smartshopsaver.databinding.ActivityProductSubCategoryListAdminBinding;
import com.example.smartshopsaver.models.ModelProductSubCategory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductSubCategoryListAdminActivity extends AppCompatActivity {

    //View Binding
    private ActivityProductSubCategoryListAdminBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "PRODUCT_SUB_CAT_TAG";

    private String productCategoryId;


    private ArrayList<ModelProductSubCategory> productCategoryArrayList;
    private AdapterProductSubCategoryAdmin adapterProductCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductSubCategoryListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productCategoryId = getIntent().getStringExtra("productCategoryId");

        loadProductCategoryDetails();
        loadProductSubCategories();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductCategory.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle addBrochureFab click, start BrochureCategoryAddActivity
        binding.addProductSubCategoryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductSubCategoryListAdminActivity.this, ProductSubCategoryAddAdminActivity.class);
                intent.putExtra("productCategoryId", productCategoryId);
                intent.putExtra("isEdit", false);
                startActivity(intent);
            }
        });

    }

    private void loadProductCategoryDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productCategory = "" + snapshot.child("productCategory").getValue();
                        binding.toolbarSubTitleTv.setText(productCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductSubCategories() {
        productCategoryArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId)
                .child("ProductSubCategories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productCategoryArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                ModelProductSubCategory modelBrochureCategory = ds.getValue(ModelProductSubCategory.class);
                                productCategoryArrayList.add(modelBrochureCategory);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        adapterProductCategory = new AdapterProductSubCategoryAdmin(ProductSubCategoryListAdminActivity.this, productCategoryArrayList);
                        binding.productCategoryRv.setAdapter(adapterProductCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
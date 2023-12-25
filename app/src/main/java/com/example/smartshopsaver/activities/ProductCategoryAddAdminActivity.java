package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.databinding.ActivityProductCategoryAddAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProductCategoryAddAdminActivity extends AppCompatActivity {

    //View Binding
    private ActivityProductCategoryAddAdminBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "PRODUCT_ADD_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private boolean isEdit = false;
    private String productCategoryIdToEdit = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityProductCategoryAddAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //To check if we're here to add new or update existing
        isEdit = getIntent().getBooleanExtra("isEdit", false);

        //Check if we're here to add new or update existing
        if (isEdit) {
            binding.toolbarTitleTv.setText("Edit Product Category");
            binding.saveBtn.setText("Update");

            //get brochure id to edit
            productCategoryIdToEdit = getIntent().getStringExtra("productCategoryId");

            loadProductCategoryDetails();
        } else {
            binding.toolbarTitleTv.setText("Add Product Category");
            binding.saveBtn.setText("Save");
        }

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //handle saveBtn click: validate data, save to firebase
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private void loadProductCategoryDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryIdToEdit)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String brochureCategory = "" + snapshot.child("productCategory").getValue();
                        binding.productCategoryEt.setText(brochureCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productCategory = "";

    private void validateData() {
        //get data
        productCategory = binding.productCategoryEt.getText().toString().trim();

        //validate data
        if (productCategory.isEmpty()) {
            binding.productCategoryEt.setError("Enter product category!");
            binding.productCategoryEt.requestFocus();
        } else {
            if (isEdit) {
                updateProductCategory();
            } else {
                saveProductCategory();
            }
        }
    }

    private void saveProductCategory() {
        //show progress
        progressDialog.setMessage("Saving product category...!");
        progressDialog.show();

        long timestamp = MyUtils.getTimestamp();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productCategoryId", "" + timestamp);
        hashMap.put("productCategory", "" + productCategory);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Added successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(ProductCategoryAddAdminActivity.this, "Added Successfully...!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProductCategoryAddAdminActivity.this, "Failed to add due to " + e.getMessage());
                    }
                });
    }

    private void updateProductCategory() {
        //show progress
        progressDialog.setMessage("Updating product category...!");
        progressDialog.show();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productCategory", "" + productCategory);

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child("" + productCategoryIdToEdit)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Updated successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(ProductCategoryAddAdminActivity.this, "Updated Successfully...!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProductCategoryAddAdminActivity.this, "Failed to update due to " + e.getMessage());
                    }
                });
    }

}
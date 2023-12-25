package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.databinding.ActivityProductSubCategoryAddAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProductSubCategoryAddAdminActivity extends AppCompatActivity {

    //View Binding
    private ActivityProductSubCategoryAddAdminBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "PRODUCT_SUB_CAT_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private boolean isEdit = false;
    private String productCategoryId = "";
    private String productSubCategoryIdToEdit = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityProductSubCategoryAddAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //To check if we're here to add new or update existing
        isEdit = getIntent().getBooleanExtra("isEdit", false);
        productCategoryId = getIntent().getStringExtra("productCategoryId");

        loadProductCategoryDetails();

        //Check if we're here to add new or update existing
        if (isEdit) {
            binding.toolbarTitleTv.setText("Edit Product Sub Category");
            binding.saveBtn.setText("Update");

            //get product subcategory id to edit
            productSubCategoryIdToEdit = getIntent().getStringExtra("productSubCategoryId");

            loadProductSubCategoryDetails();
        } else {
            binding.toolbarTitleTv.setText("Add Product Sub Category");
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

    private void loadProductSubCategoryDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId)
                .child("ProductSubCategories")
                .child(productSubCategoryIdToEdit)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productSubCategory = "" + snapshot.child("productSubCategory").getValue();
                        binding.productSubCategoryEt.setText(productSubCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productSubCategory = "";

    private void validateData() {
        //get data
        productSubCategory = binding.productSubCategoryEt.getText().toString().trim();

        //validate data
        if (productSubCategory.isEmpty()) {
            binding.productSubCategoryEt.setError("Enter product sub category!");
            binding.productSubCategoryEt.requestFocus();
        } else {
            if (isEdit) {
                updateProductSubCategory();
            } else {
                saveProductSubCategory();
            }
        }
    }

    private void saveProductSubCategory() {
        //show progress
        progressDialog.setMessage("Saving product sub category...!");
        progressDialog.show();

        long timestamp = MyUtils.getTimestamp();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productCategoryId", "" + productCategoryId);
        hashMap.put("productSubCategoryId", "" + timestamp);
        hashMap.put("productSubCategory", "" + productSubCategory);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId)
                .child("ProductSubCategories")
                .child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Added successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(ProductSubCategoryAddAdminActivity.this, "Added Successfully...!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProductSubCategoryAddAdminActivity.this, "Failed to add due to " + e.getMessage());
                    }
                });
    }

    private void updateProductSubCategory() {
        //show progress
        progressDialog.setMessage("Updating product category...!");
        progressDialog.show();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productSubCategory", "" + productSubCategory);

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId)
                .child("ProductSubCategories")
                .child("" + productSubCategoryIdToEdit)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Updated successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(ProductSubCategoryAddAdminActivity.this, "Updated Successfully...!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProductSubCategoryAddAdminActivity.this, "Failed to update due to " + e.getMessage());
                    }
                });
    }

}
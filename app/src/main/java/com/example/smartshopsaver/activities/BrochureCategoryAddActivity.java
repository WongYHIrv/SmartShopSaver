package com.example.smartshopsaver.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.databinding.ActivityBrochureCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BrochureCategoryAddActivity extends AppCompatActivity {

    //View Binding
    private ActivityBrochureCategoryAddBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "BROCHURE_ADD_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private boolean isEdit = false;
    private String brochureIdToEdit = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityBrochureCategoryAddBinding.inflate(getLayoutInflater());
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
            binding.toolbarTitleTv.setText("Edit Brochure Category");
            binding.saveBtn.setText("Update");

            //get brochure id to edit
            brochureIdToEdit = getIntent().getStringExtra("brochureId");

            loadBrochureCategoryDetails();
        } else {
            binding.toolbarTitleTv.setText("Add Brochure Category");
            binding.saveBtn.setText("Save");
        }

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        //handle saveBtn click: validate data, save to firebase
        binding.saveBtn.setOnClickListener(v -> validateData());

    }

    private void loadBrochureCategoryDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrochureCategories");
        ref.child(brochureIdToEdit)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String brochureCategory = "" + snapshot.child("brochureCategory").getValue();
                        binding.brochureCategoryEt.setText(brochureCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String brochureCategory = "";

    private void validateData() {
        //get data
        brochureCategory = binding.brochureCategoryEt.getText().toString().trim();

        //validate data
        if (brochureCategory.isEmpty()) {
            binding.brochureCategoryEt.setError("Enter brochure category!");
            binding.brochureCategoryEt.requestFocus();
        } else {
            if (isEdit) {
                updateBrochure();
            } else {
                saveBrochure();
            }
        }
    }

    private void saveBrochure() {
        //show progress
        progressDialog.setMessage("Saving Brochure...!");
        progressDialog.show();

        long timestamp = MyUtils.getTimestamp();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("brochureCategory", "" + brochureCategory);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrochureCategories");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Added successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureCategoryAddActivity.this, "Added Successfully...!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureCategoryAddActivity.this, "Failed to add due to " + e.getMessage());
                    }
                });
    }

    private void updateBrochure() {
        //show progress
        progressDialog.setMessage("Updating Brochure...!");
        progressDialog.show();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("brochureCategory", "" + brochureCategory);

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrochureCategories");
        ref.child("" + brochureIdToEdit)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Updated successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureCategoryAddActivity.this, "Updated Successfully...!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureCategoryAddActivity.this, "Failed to update due to " + e.getMessage());
                    }
                });
    }

}
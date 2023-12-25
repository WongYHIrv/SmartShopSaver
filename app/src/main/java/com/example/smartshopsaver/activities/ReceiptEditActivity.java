package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityReceiptEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ReceiptEditActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ActivityReceiptEditBinding binding;

    private String receiptId;

    private ProgressDialog progressDialog;

    private static final String TAG = "RECEIPT_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getUid();

        receiptId = getIntent().getStringExtra("receiptId");

        //Setup Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadReceiptInfo(uid);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private void loadReceiptInfo(String uid) {
        Log.d(TAG, "loadReceiptInfo: Loading Receipt Information...");

        DatabaseReference refReceipt = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Receipts");
        refReceipt.child(receiptId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Get Resource Info
                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String location = ""+snapshot.child("location").getValue();
                        Object expensesAmtObject = snapshot.child("expensesAmt").getValue();
                        String expensesAmt = null;
                        if (expensesAmtObject != null) {
                            // Convert the value to a String
                            expensesAmt = String.valueOf(expensesAmtObject);
                        }

                        //Set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);
                        binding.locationEt.setText(location);
                        binding.expenseAmtEt.setText(expensesAmt);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String title = "", description = "", location = "";
    double expensesAmt = 0.00;

    private void validateData() {

        String uid = firebaseAuth.getUid();

        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        location = binding.locationEt.getText().toString().trim();
        expensesAmt = Double.parseDouble(binding.expenseAmtEt.getText().toString().trim());

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Enter Location...0", Toast.LENGTH_SHORT).show();
        }
        else if (Double.isNaN(expensesAmt)) {
            Toast.makeText(this, "Amount cannot be empty...", Toast.LENGTH_SHORT).show();
        }
        else {
            updateReceipt(uid);
        }
    }

    private void updateReceipt(String uid) {
        Log.d(TAG, "updateReceipt: Starting Update...");

        progressDialog.setMessage("Updating Receipt...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("location", ""+location);
        hashMap.put("expensesAmt", expensesAmt);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Receipts");
        ref.child(receiptId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Receit Updated...");
                        progressDialog.dismiss();
                        Toast.makeText(ReceiptEditActivity.this, "Receipt updated...", Toast.LENGTH_SHORT).show() ;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ReceiptEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
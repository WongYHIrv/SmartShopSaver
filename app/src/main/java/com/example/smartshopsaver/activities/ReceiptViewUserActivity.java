package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.smartshopsaver.R;
import com.example.smartshopsaver.adapters.AdapterReceiptUser;
import com.example.smartshopsaver.databinding.ActivityReceiptViewUserBinding;
import com.example.smartshopsaver.models.ModelReceipt;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReceiptViewUserActivity extends AppCompatActivity {

    private ActivityReceiptViewUserBinding binding;

    private ArrayList<ModelReceipt> receiptArrayList;

    private AdapterReceiptUser adapterReceiptUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptViewUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadReceiptList();
    }

    private void loadReceiptList() {

        receiptArrayList = new ArrayList<>();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("Receipts");
        ref.orderByChild("id")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelReceipt model = ds.getValue(ModelReceipt.class);
                            receiptArrayList.add(model);

                        }
                        //Adapter
                        adapterReceiptUser = new AdapterReceiptUser(ReceiptViewUserActivity.this, receiptArrayList);
                        binding.receiptRv.setAdapter(adapterReceiptUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}
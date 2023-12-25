package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.example.smartshopsaver.MyApp;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityReceiptViewDetailUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ReceiptViewDetailUserActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ActivityReceiptViewDetailUserBinding binding;

    private ProgressBar progressBar;

    String receiptId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptViewDetailUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getUid();

        Intent intent = getIntent();
        receiptId = intent.getStringExtra("receiptId");

        loadReceiptDetails(uid);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadReceiptDetails(String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Receipts");
        ref.child(receiptId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String location = "" + snapshot.child("location").getValue();
                        String dateReceipt = "" + snapshot.child("dateReceipt").getValue();
                        Object expensesAmtObject = snapshot.child("expensesAmt").getValue();
                        String expensesAmt = null;
                        if (expensesAmtObject != null) {
                            // Convert the value to a String
                            expensesAmt = String.valueOf(expensesAmtObject);
                        }
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String id = "" + snapshot.child("id").getValue();
                        String url = "" + snapshot.child("url").getValue();

                        //Format Date
                        String dateAdded = MyApp.formatTimestamp(Long.parseLong(timestamp));

                        //Set Data
                        // Picasso.get().load(url).into(binding.imageView);

                        if (url != null) {
                            binding.progressBar.setVisibility(View.VISIBLE);
                            Picasso.get()
                                    .load(url)
                                    .fit().centerInside()
                                    .rotate(90)
                                    .error(R.drawable.delete_white)
                                    .into(binding.imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            binding.progressBar.setVisibility(View.INVISIBLE);
                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });
                        }

                        binding.titleTv.setText(title);
                        binding.descriptionTv.setText(description);
                        binding.locationTv.setText(location);
                        binding.dateTv.setText(dateReceipt);
                        binding.expenseAmtTv.setText(expensesAmt);
                        binding.receiptIdTv.setText(id);
                        binding.dateAddedTv.setText(dateAdded);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
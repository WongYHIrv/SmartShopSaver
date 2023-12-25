package com.example.smartshopsaver.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartshopsaver.adapters.AdapterProductUser;
import com.example.smartshopsaver.databinding.ActivityProductListUserBinding;
import com.example.smartshopsaver.models.ModelProduct;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;

public class ProductListUserActivity extends AppCompatActivity {

    private ActivityProductListUserBinding binding;

    private static final String TAG = "PRODUCT_LIST_TAG";

    private ArrayList<ModelProduct> productArrayList;
    private AdapterProductUser adapterProductUser;

    private GmsBarcodeScannerOptions gmsBarcodeScannerOptions;
    private GmsBarcodeScanner gmsBarcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gmsBarcodeScannerOptions = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .enableAutoZoom() // available on 16.1.0 and higher
                .build();
        gmsBarcodeScanner = GmsBarcodeScanning.getClient(this, gmsBarcodeScannerOptions);

        //load products
        loadProducts();

        //handle searchEt text change listener, start searching
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        //handle scanBtn click: scan barcode to search product
        binding.scanBtn.setOnClickListener(view -> scanBarcode());

    }

    private void loadProducts() {
        //init list before starting adding data into it
        productArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.addValueEventListener(new ValueEventListener() {
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
                adapterProductUser = new AdapterProductUser(ProductListUserActivity.this, productArrayList);
                binding.productsRv.setAdapter(adapterProductUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void scanBarcode() {
        gmsBarcodeScanner
                .startScan()
                .addOnSuccessListener(barcode -> {
                    // Task completed successfully
                    Log.d(TAG, "openScanner: Scanned...!");
                    String barcodeText = barcode.getRawValue();

                    try {
                        binding.searchEt.setText(barcodeText);
                        adapterProductUser.getFilter().filter(barcodeText);
                    } catch (Exception e) {
                        Log.e(TAG, "scanBarcode: ", e);
                    }
                })
                .addOnCanceledListener(() -> {
                    // Task canceled
                    Log.d(TAG, "openScanner: Cancelled...!");
                })
                .addOnFailureListener(e -> {
                    // Task failed with an exception
                    Log.e(TAG, "openScanner: ", e);
                });
    }

}
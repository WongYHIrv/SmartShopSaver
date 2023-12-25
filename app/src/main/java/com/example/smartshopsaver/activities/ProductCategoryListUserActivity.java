package com.example.smartshopsaver.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartshopsaver.adapters.AdapterProductCategoryUser;
import com.example.smartshopsaver.databinding.ActivityProductCategoryListUserBinding;
import com.example.smartshopsaver.models.ModelProductCategory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductCategoryListUserActivity extends AppCompatActivity {

    private ActivityProductCategoryListUserBinding binding;

    private static final String TAG = "PRODUCTS_TAG";

    private ArrayList<ModelProductCategory> productCategoryArrayList;
    private AdapterProductCategoryUser adapterProductCategoryUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityProductCategoryListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadProductCategories();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductCategoryUser.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void loadProductCategories() {
        //init list before starting adding data into it
        productCategoryArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it, each time there is a change in list
                productCategoryArrayList.clear();

                //load data into list from firebase db
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelProductCategory modelBrochureCategory = ds.getValue(ModelProductCategory.class);
                        productCategoryArrayList.add(modelBrochureCategory);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }
                }

                //init/setup adapter and set to recyclerview
                adapterProductCategoryUser = new AdapterProductCategoryUser(ProductCategoryListUserActivity.this, productCategoryArrayList);
                binding.productCategoryRv.setAdapter(adapterProductCategoryUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
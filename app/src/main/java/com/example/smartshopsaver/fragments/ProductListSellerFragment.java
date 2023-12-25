package com.example.smartshopsaver.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartshopsaver.activities.ProductAddActivity;
import com.example.smartshopsaver.adapters.AdapterBrochureSeller;
import com.example.smartshopsaver.adapters.AdapterProductSeller;
import com.example.smartshopsaver.databinding.FragmentProductListSellerBinding;
import com.example.smartshopsaver.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductListSellerFragment extends Fragment {

    private FragmentProductListSellerBinding binding;

    private static final String TAG = "PRODUCTS_TAG";

    private FirebaseAuth firebaseAuth;

    private Context mContext;

    private ArrayList<ModelProduct> productArrayList;
    private AdapterProductSeller adapterProductSeller;

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    public ProductListSellerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProductListSellerBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadProducts();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //handle addProductFab click, start ProductAddActivity
        binding.addProductFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProductAddActivity.class);
                intent.putExtra("isEdit", false);
                startActivity(intent);
            }
        });
    }

    private void loadProducts() {
        //init list before starting adding data into it
        productArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.orderByChild("uid").equalTo("" + firebaseAuth.getUid())
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
                        adapterProductSeller = new AdapterProductSeller(mContext, productArrayList);
                        binding.productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
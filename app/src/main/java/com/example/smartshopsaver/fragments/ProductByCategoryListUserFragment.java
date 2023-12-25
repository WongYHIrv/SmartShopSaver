package com.example.smartshopsaver.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartshopsaver.adapters.AdapterProductUser;
import com.example.smartshopsaver.databinding.FragmentProductByCategoryListUserBinding;
import com.example.smartshopsaver.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
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

public class ProductByCategoryListUserFragment extends Fragment {

    private FragmentProductByCategoryListUserBinding binding;

    private static final String TAG = "PRODUCT_BY_CAT_TAG";

    private FirebaseAuth firebaseAuth;

    private Context mContext;


    private String categoryId;
    private String subCategoryId;

    private ArrayList<ModelProduct> productArrayList;
    private AdapterProductUser adapterProductUser;

    private GmsBarcodeScannerOptions gmsBarcodeScannerOptions;
    private GmsBarcodeScanner gmsBarcodeScanner;


    public ProductByCategoryListUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param categoryId    Parameter 1.
     * @param subCategoryId Parameter 2.
     * @return A new instance of fragment ProductByCategoryListUserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductByCategoryListUserFragment newInstance(String categoryId, String subCategoryId) {
        ProductByCategoryListUserFragment fragment = new ProductByCategoryListUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("subCategoryId", subCategoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            subCategoryId = getArguments().getString("subCategoryId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProductByCategoryListUserBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gmsBarcodeScannerOptions = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .enableAutoZoom() // available on 16.1.0 and higher
                .build();
        gmsBarcodeScanner = GmsBarcodeScanning.getClient(mContext, gmsBarcodeScannerOptions);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        loadProducts();

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

        //handle scanBtn click: scan barcode to search product
        binding.scanBtn.setOnClickListener(v -> scanBarcode());

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
                        String pCategoryId = "" + modelBrochureCategory.getProductCategoryId();
                        String pSubCategoryId = "" + modelBrochureCategory.getProductSubCategoryId();

                        if (categoryId.equals(pCategoryId) && subCategoryId.equals(pSubCategoryId)) {
                            productArrayList.add(modelBrochureCategory);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }
                }

                //init/setup adapter and set to recyclerview
                adapterProductUser = new AdapterProductUser(mContext, productArrayList);
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
package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.adapters.AdapterShopComparisonUser;
import com.example.smartshopsaver.databinding.ActivityProductComparisonUserBinding;
import com.example.smartshopsaver.models.ModelProduct;
import com.example.smartshopsaver.models.ModelShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProductComparisonUserActivity extends AppCompatActivity {

    private ActivityProductComparisonUserBinding binding;

    private static final String TAG = "PRODUCT_COMPARISON_TAG";

    private String productId = "";
    private String productBarcode = "";

    private ArrayList<ModelShop> shopArrayList;
    private AdapterShopComparisonUser adapterShopComparisonUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductComparisonUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productId = getIntent().getStringExtra("productId");
        productBarcode = getIntent().getStringExtra("productBarcode");

        Log.d(TAG, "onCreate: productId: " + productId);
        Log.d(TAG, "onCreate: productBarcode: " + productBarcode);

        loadProductDetails();
        loadProductShops();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());
    }

    private void loadProductDetails() {

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child("" + productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelProduct modelProduct = snapshot.getValue(ModelProduct.class);

                        String productName = "" + modelProduct.getProductName();
                        String productDescription = "" + modelProduct.getProductDescription();
                        String productBarcode = "" + modelProduct.getProductBarcode();
                        String imageUrl = "" + modelProduct.getImageUrl();

                        binding.productNameTv.setText(productName);
                        binding.productDescriptionTv.setText(productDescription);

                        try {
                            Glide.with(ProductComparisonUserActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.cart_white)
                                    .into(binding.productImageIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onBindViewHolder: ", e);
                        }
                        loadProductCategory(modelProduct);
                        loadProductSubCategory(modelProduct);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductCategory(ModelProduct modelCategory) {
        String productCategoryId = modelCategory.getProductCategoryId();

        DatabaseReference refCategory = FirebaseDatabase.getInstance().getReference("ProductCategories");
        refCategory.child(productCategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productCategory = "" + snapshot.child("productCategory").getValue();
                        modelCategory.setProductCategory(productCategory);

                        binding.productCategoryTv.setText(productCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductSubCategory(ModelProduct modelCategory) {
        String productCategoryId = modelCategory.getProductCategoryId();
        String productSubCategoryId = modelCategory.getProductSubCategoryId();

        DatabaseReference refSubCategory = FirebaseDatabase.getInstance().getReference("ProductCategories");
        refSubCategory.child(productCategoryId).child("ProductSubCategories").child(productSubCategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productSubCategory = "" + snapshot.child("productSubCategory").getValue();
                        modelCategory.setProductSubCategory(productSubCategory);

                        binding.productSubCategoryTv.setText(productSubCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductShops() {
        Log.d(TAG, "loadProductShops: ");
        shopArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.orderByChild("productBarcode").equalTo(productBarcode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        shopArrayList.clear();

                        adapterShopComparisonUser = new AdapterShopComparisonUser(ProductComparisonUserActivity.this, shopArrayList);
                        binding.shopsRv.setAdapter(adapterShopComparisonUser);

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            Log.d(TAG, "onDataChange: ");
                            String shopUid = "" + modelProduct.getUid();


                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(shopUid)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Log.d(TAG, "onDataChange: Loading Shop Info...");
                                            ModelShop modelShop = snapshot.getValue(ModelShop.class);

                                            String productId = modelProduct.getProductId();
                                            long productTimestamp = modelProduct.getTimestamp();
                                            String productPrice = "" + modelProduct.getProductPrice();
                                            boolean discountAvailable = modelProduct.isDiscountAvailable();
                                            String productDiscountPrice = "" + modelProduct.getProductDiscountPrice();

                                            if (productPrice.isEmpty() || productPrice.equals("null")) {
                                                productPrice = "0";
                                            }
                                            if (productDiscountPrice.isEmpty() || productDiscountPrice.equals("null")) {
                                                productDiscountPrice = "0";
                                            }

                                            double priceToShow = 0.0;
                                            if (discountAvailable) {
                                                double originalPrice = Double.parseDouble(productPrice);
                                                double discountedPrice = Double.parseDouble(productDiscountPrice);
                                                double newAfterDiscountPrice = originalPrice - discountedPrice;

                                                priceToShow = newAfterDiscountPrice;
                                            } else {
                                                double originalPrice = Double.parseDouble(productPrice);

                                                priceToShow = originalPrice;
                                            }

                                            modelShop.setComparingProductId(productId);
                                            modelShop.setComparingProductPrice(priceToShow);
                                            modelShop.setComparingProductTimestamp(productTimestamp);

                                            shopArrayList.add(modelShop);

                                            adapterShopComparisonUser.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }

                        new Handler().postDelayed(() -> {
                            loadMinMaxValues();
                            sortShopsByPrices();
                        }, 1000);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMinMaxValues() {
        ArrayList<Double> pricesArray = new ArrayList<>();
        for (ModelShop modelShop : shopArrayList) {
            try {
                double comparingProductPrice = modelShop.getComparingProductPrice();
                pricesArray.add(comparingProductPrice);
            } catch (Exception e) {
                Log.e(TAG, "loadMinMaxValues: ", e);
            }
        }

        double max = pricesArray.get(0); // Assume the first element is the maximum initially
        double min = pricesArray.get(0); // Assume the first element is the minimum initially
        for (int i = 1; i < pricesArray.size(); i++) {
            if (pricesArray.get(i) > max) {
                max = pricesArray.get(i); // Update maximum value
            }
            if (pricesArray.get(i) < min) {
                min = pricesArray.get(i); // Update minimum value
            }
        }

        binding.lowestPriceTv.setText("" + min);
        binding.highestPriceTv.setText("" + max);
    }

    private void sortShopsByPrices() {
        Collections.sort(shopArrayList, (model1, model2) -> Double.compare(model1.getComparingProductPrice(), model2.getComparingProductPrice()));
        //notify changes
        adapterShopComparisonUser.notifyDataSetChanged();
    }

}
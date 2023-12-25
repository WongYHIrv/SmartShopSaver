package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityProductDetailsSellersBinding;
import com.example.smartshopsaver.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductDetailsSellersActivity extends AppCompatActivity {

    //View Binding
    private ActivityProductDetailsSellersBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "PRODUCT_DETAILS_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private String productId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityProductDetailsSellersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get brochure id to load details of it
        productId = getIntent().getStringExtra("productId");

        loadProductDetails();

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
                        try {
                            ModelProduct modelProduct = snapshot.getValue(ModelProduct.class);

                            String productName = "" + modelProduct.getProductName();
                            String productDescription = "" + modelProduct.getProductDescription();
                            String productBarcode = "" + modelProduct.getProductBarcode();
                            String productPrice = "" + modelProduct.getProductPrice();
                            String imageUrl = "" + modelProduct.getImageUrl();
                            String productStock = "" + modelProduct.getProductStock();
                            String productExpireDate = "" + modelProduct.getProductExpireDate();
                            long timestamp = modelProduct.getTimestamp();
                            boolean discountAvailable = modelProduct.isDiscountAvailable();
                            String productDiscountPrice = "" + modelProduct.getProductDiscountPrice();
                            String productDiscountNote = "" + modelProduct.getProductDiscountNote();

                            String formattedDate = MyUtils.formatTimestampDate(timestamp);

                            if (productPrice.isEmpty() || productPrice.equals("null")) {
                                productPrice = "0";
                            }
                            if (productDiscountPrice.isEmpty() || productDiscountPrice.equals("null")) {
                                productDiscountPrice = "0";
                            }

                            binding.productNameTv.setText(productName);
                            binding.productDescriptionTv.setText(productDescription);
                            binding.productBarcodeTv.setText(productBarcode);
                            binding.productDateTv.setText(formattedDate);
                            binding.productDateExpireTv.setText(productExpireDate);
                            binding.productStockTv.setText(productStock);

                            if (discountAvailable) {
                                binding.discountedPriceSymbolTv.setVisibility(View.VISIBLE);
                                binding.discountedPriceTv.setVisibility(View.VISIBLE);
                                binding.productDiscountNoteCv.setVisibility(View.VISIBLE);
                                binding.originalPriceTv.setPaintFlags(binding.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                                double originalPrice = Double.parseDouble(productPrice);
                                double discountedPrice = Double.parseDouble(productDiscountPrice);
                                double newAfterDiscountPrice = originalPrice - discountedPrice;

                                binding.originalPriceTv.setText("" + MyUtils.roundedDecimalValue(originalPrice));
                                binding.discountedPriceTv.setText("" + MyUtils.roundedDecimalValue(newAfterDiscountPrice));
                                binding.productDiscountNoteTv.setText("" + productDiscountNote);
                            } else {
                                binding.discountedPriceSymbolTv.setVisibility(View.GONE);
                                binding.discountedPriceTv.setVisibility(View.GONE);
                                binding.productDiscountNoteCv.setVisibility(View.GONE);

                                double originalPrice = Double.parseDouble(productPrice);

                                binding.originalPriceTv.setText("" + MyUtils.roundedDecimalValue(originalPrice));
                            }

                            try {
                                Glide.with(ProductDetailsSellersActivity.this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.cart_white)
                                        .into(binding.productImageIv);
                            } catch (Exception e) {
                                Log.e(TAG, "onBindViewHolder: ", e);
                            }

                            try {
                                loadChart(modelProduct);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                            loadSellerInfo(modelProduct);
                            loadProductCategory(modelProduct);
                            loadProductSubCategory(modelProduct);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadSellerInfo(ModelProduct modelProduct) {
        String sellerUid = "" + modelProduct.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        binding.productSellerTv.setText(shopName);
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

    private void loadChart1(ModelProduct modelProduct) {
        binding.priceGraphAcv.setProgressBar(binding.priceGraphPb);

        Cartesian cartesian = AnyChart.line();
        cartesian.animation(true);
        //cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.crosshair()
                .enabled(true)
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);
        cartesian.tooltip()
                .positionMode(TooltipPositionMode.POINT);
        cartesian.title("Price Change History");
        cartesian.xAxis(0)
                .title("Dates");
        cartesian.yAxis(0)
                .title("Prices");

        List<DataEntry> seriesData = new ArrayList<>();
        seriesData.add(new CustomDataEntry("12/09/2023", 129.98));
        seriesData.add(new CustomDataEntry("15/09/2023", 120.06));
        seriesData.add(new CustomDataEntry("19/09/2023", 160.21));
        seriesData.add(new CustomDataEntry("12/10/2023", 140.21));

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line lineSeries1 = cartesian.line(series1Mapping);
        lineSeries1.name(modelProduct.getProductName());
        lineSeries1.hovered().markers().enabled(true);
        lineSeries1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        lineSeries1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        binding.priceGraphAcv.setChart(cartesian);
    }

    private void loadChart(ModelProduct modelProduct) {
        binding.priceGraphAcv.setProgressBar(binding.priceGraphPb);

        Cartesian cartesian = AnyChart.line();
        cartesian.animation(true);
        //cartesian.padding(10d, 20d, 5d, 20d);
        cartesian.crosshair()
                .enabled(true)
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);
        cartesian.tooltip()
                .positionMode(TooltipPositionMode.POINT);
        cartesian.title("Price Change History");
        cartesian.xAxis(0)
                .title("Dates");
        cartesian.yAxis(0)
                .title("Prices");

        List<DataEntry> seriesData = new ArrayList<>();

        String productId = "" + modelProduct.getProductId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child(productId).child("PriceHistory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        seriesData.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String productHistoryTimestamp = "" + ds.child("productHistoryTimestamp").getValue();
                            String productPrice = "" + ds.child("productPrice").getValue();

                            if (productHistoryTimestamp.equals("") || productPrice.equals("null")) {
                                productHistoryTimestamp = "0";
                            }
                            if (productPrice.equals("") || productPrice.equals("null")) {
                                productPrice = "0";
                            }

                            String formattedDate = MyUtils.formatTimestampDate(Long.parseLong(productHistoryTimestamp));

                            Log.d("CHART_TAG", "onDataChange: formattedDate: " + formattedDate);
                            Log.d("CHART_TAG", "onDataChange: productPrice: " + productPrice);

                            seriesData.add(new CustomDataEntry("" + formattedDate, Double.parseDouble(productPrice)));
                        }

                        Set set = Set.instantiate();
                        set.data(seriesData);
                        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

                        Line lineSeries1 = cartesian.line(series1Mapping);
                        lineSeries1.name(modelProduct.getProductName());
                        lineSeries1.hovered().markers().enabled(true);
                        lineSeries1.hovered().markers()
                                .type(MarkerType.CIRCLE)
                                .size(4d);
                        lineSeries1.tooltip()
                                .position("right")
                                .anchor(Anchor.LEFT_CENTER)
                                .offsetX(5d)
                                .offsetY(5d);

                        cartesian.legend().enabled(true);
                        cartesian.legend().fontSize(13d);
                        cartesian.legend().padding(0d, 0d, 10d, 0d);

                        binding.priceGraphAcv.setChart(cartesian);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value) {
            super(x, value);
        }

    }
}
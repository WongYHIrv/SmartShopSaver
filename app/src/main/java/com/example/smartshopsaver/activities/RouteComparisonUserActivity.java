package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityRouteComparisonUserBinding;
import com.example.smartshopsaver.models.ModelShop;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RouteComparisonUserActivity extends AppCompatActivity {

    //View Binding
    private ActivityRouteComparisonUserBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "GENERATE_ROUTE_TAG";


    private ArrayList<ModelShop> shopArrayList;
    private ArrayList<String> shopTitlesArrayList;

    private ModelShop modelShop1;
    private ModelShop modelShop2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityRouteComparisonUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.selectedShop1Cv.setVisibility(View.GONE);
        binding.selectedShop2Cv.setVisibility(View.GONE);

        loadShops();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        //handle selectShop1Cv click, show shop 1 selection dialog
        binding.selectShop1Cv.setOnClickListener(view -> shopsSelectionDialog1());

        //handle selectShop2Cv click, show shop 2 selection dialog
        binding.selectShop2Cv.setOnClickListener(view -> shopsSelectionDialog2());

        binding.generateRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modelShop1 != null && modelShop2 != null) {
                    Intent intent = new Intent(RouteComparisonUserActivity.this, RouteMapsActivity.class);

                    double latitudeR1Retrieve = Double.parseDouble(modelShop1.getLatitude());
                    double longitudeR1Retrieve = Double.parseDouble(modelShop1.getLongitude());

                    double latitudeR2Retrieve = Double.parseDouble(modelShop2.getLatitude());
                    double longitudeR2Retrieve = Double.parseDouble(modelShop2.getLongitude());

                    intent.putExtra("latitudeR1",latitudeR1Retrieve);
                    intent.putExtra("longitudeR1", longitudeR1Retrieve);

                    intent.putExtra("latitudeR2", latitudeR2Retrieve);
                    intent.putExtra("longitudeR2", longitudeR2Retrieve);

                    startActivity(intent);
                }
                //startActivity(new Intent(RouteComparisonUserActivity.this, RouteMapsActivity.class));
            }
        });
    }

    private void loadShops() {
        shopArrayList = new ArrayList<>();
        shopTitlesArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo(Constants.USER_TYPE_SELLER)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        shopArrayList.clear();
                        shopTitlesArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: ");
                            try {
                                ModelShop modelShop = ds.getValue(ModelShop.class);
                                shopArrayList.add(modelShop);
                                shopTitlesArrayList.add(modelShop.getShopName());
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void shopsSelectionDialog1() {
        Log.d(TAG, "shopsSelectionDialog1: ");

        String[] shopsArray = shopTitlesArrayList.toArray(new String[0]);


        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Choose Shop 1")
                .setItems(shopsArray, (dialogInterface, i) -> {
                    modelShop1 = shopArrayList.get(i);

                    String shopName = "" + modelShop1.getShopName();
                    String profileImage = "" + modelShop1.getProfileImage();
                    String phoneNumber = "" + modelShop1.getPhone();
                    String shopAddress = "" + modelShop1.getAddress();
                    String latitudeR1 = "" + modelShop1.getLatitude();
                    String longitudeR1 = "" + modelShop1.getLongitude();

                    binding.selectedShop1Cv.setVisibility(View.VISIBLE);
                    binding.shopName1Tv.setText(shopName);
                    binding.shopPhone1Tv.setText(phoneNumber);
                    binding.shopAddress1Tv.setText(shopAddress);
                    binding.shopLatitude1Tv.setText(latitudeR1);
                    binding.shopLongitude1Tv.setText(longitudeR1);

                    try {
                        Glide.with(RouteComparisonUserActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.store_white)
                                .into(binding.selectedShop1Iv);
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: ", e);
                    }

                })
                .show();
    }

    private void shopsSelectionDialog2() {
        Log.d(TAG, "shopsSelectionDialog2: ");
        String[] shopsArray = shopTitlesArrayList.toArray(new String[shopTitlesArrayList.size()]);

        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        alertDialogBuilder.setTitle("Choose Shop 2")
                .setItems(shopsArray, (dialogInterface, i) -> {
                    modelShop2 = shopArrayList.get(i);

                    String shopName = "" + modelShop2.getShopName();
                    String profileImage = "" + modelShop2.getProfileImage();
                    String phoneNumber = "" + modelShop2.getPhone();
                    String shopAddress = "" + modelShop2.getAddress();
                    String latitudeR2 = "" + modelShop2.getLatitude();
                    String longitudeR2 = "" + modelShop2.getLongitude();

                    binding.selectedShop2Cv.setVisibility(View.VISIBLE);
                    binding.shopName2Tv.setText(shopName);
                    binding.shopPhone2Tv.setText(phoneNumber);
                    binding.shopAddress2Tv.setText(shopAddress);
                    binding.shopLatitude2Tv.setText(latitudeR2);
                    binding.shopLongitude2Tv.setText(longitudeR2);

                    try {
                        Glide.with(RouteComparisonUserActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.store_white)
                                .into(binding.selectedShop2Iv);
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: ", e);
                    }
                })
                .show();
    }
}
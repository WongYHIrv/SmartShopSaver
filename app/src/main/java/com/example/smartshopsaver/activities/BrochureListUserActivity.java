package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.smartshopsaver.adapters.AdapterBrochureCategoryAdmin;
import com.example.smartshopsaver.adapters.AdapterBrochureUser;
import com.example.smartshopsaver.databinding.ActivityBrochureListUserBinding;
import com.example.smartshopsaver.models.ModelBrochure;
import com.example.smartshopsaver.models.ModelBrochureCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BrochureListUserActivity extends AppCompatActivity {

    private ActivityBrochureListUserBinding binding;

    private static final String TAG = "BROCHURE_ADD_TAG";
    private static final String SORT_ALL = "SORT_ALL";
    private static final String SORT_BY_VIEWS = "SORT_BY_VIEWS";
    private static final String SORT_BY_DOWNLOADS = "SORT_BY_DOWNLOADS";

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelBrochureCategory> brochureCategoryArrayList;

    private ArrayList<ModelBrochure> brochureArrayList;
    private AdapterBrochureUser adapterBrochureUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrochureListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.filterTv.setText("Showing All Brochures");

        loadBrochureCategories();
        loadBrochures(SORT_ALL);

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterBrochureUser.getFilter().filter("" + s);
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

        //handle filterBtn click: filter brochures
        binding.filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOptions();
            }
        });
    }

    private void filterOptions() {
        //init popup menu param 1 is context and param 2 is the UI View (profileImagePickFab) to above/below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.filterBtn);

        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "All");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Most Viewed");
        popupMenu.getMenu().add(Menu.NONE, 3, 3, "Most Downloaded");
        for (int i = 0; i < brochureCategoryArrayList.size(); i++) {
            String brochureCategory = brochureCategoryArrayList.get(i).getBrochureCategory();
            popupMenu.getMenu().add(Menu.NONE, i + 4, i + 4, brochureCategory);
        }

        //Show Popup Menu
        popupMenu.show();

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the id of the menu item clicked
                int itemId = item.getItemId();
                if (itemId == 1) {
                    //All
                    binding.filterTv.setText("Showing All Brochures");
                    loadBrochures(SORT_ALL);
                } else if (itemId == 2) {
                    //Most Viewed
                    binding.filterTv.setText("Showing Most Viewed Brochures");
                    loadBrochures(SORT_BY_VIEWS);
                } else if (itemId == 3) {
                    //Most Downloaded
                    binding.filterTv.setText("Showing Most Downloaded Brochures");
                    loadBrochures(SORT_BY_DOWNLOADS);
                } else {
                    try {
                        String query = "" + item.getTitle();
                        binding.filterTv.setText("Showing " + query + " Brochures");
                        adapterBrochureUser.getFilter().filter("" + query);
                    } catch (Exception e) {
                        Log.e(TAG, "onTextChanged: ", e);
                    }
                }
                return true;
            }
        });
    }

    private void loadBrochureCategories() {
        //init list before starting adding data into it
        brochureCategoryArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrochureCategories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it, each time there is a change in list
                brochureCategoryArrayList.clear();

                //load data into list from firebase db
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelBrochureCategory modelBrochureCategory = ds.getValue(ModelBrochureCategory.class);
                        brochureCategoryArrayList.add(modelBrochureCategory);
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

    private void loadBrochures(String query) {
        //init list before starting adding data into it
        brochureArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it, each time there is a change in list
                brochureArrayList.clear();

                //load data into list from firebase db
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelBrochure modelBrochureCategory = ds.getValue(ModelBrochure.class);
                        brochureArrayList.add(modelBrochureCategory);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }
                }

                //init/setup adapter and set to recyclerview
                adapterBrochureUser = new AdapterBrochureUser(BrochureListUserActivity.this, brochureArrayList);
                binding.brochureRv.setAdapter(adapterBrochureUser);

                if (query.equals(SORT_BY_VIEWS)) {
                    sortByViews();
                } else if (query.equals(SORT_BY_DOWNLOADS)) {
                    sortByDownloads();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sortByViews() {
        //Delay of 1 second before sorting the list
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //sort chatsArrayList
                Collections.sort(brochureArrayList, (model1, model2) -> Long.compare(model2.getViewsCount(), model1.getViewsCount()));
                //notify changes
                adapterBrochureUser.notifyDataSetChanged();
            }
        }, 1000);

    }

    private void sortByDownloads() {
        //Delay of 1 second before sorting the list
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //sort chatsArrayList
                Collections.sort(brochureArrayList, (model1, model2) -> Long.compare(model2.getDownloadsCount(), model1.getDownloadsCount()));
                //notify changes
                adapterBrochureUser.notifyDataSetChanged();
            }
        }, 1000);

    }

}
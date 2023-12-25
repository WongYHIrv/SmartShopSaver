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

import com.example.smartshopsaver.activities.BrochureCategoryAddActivity;
import com.example.smartshopsaver.adapters.AdapterBrochureCategoryAdmin;
import com.example.smartshopsaver.databinding.FragmentBrochureCategoryListAdminBinding;
import com.example.smartshopsaver.models.ModelBrochureCategory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BrochureCategoryListAdminFragment extends Fragment {

    private FragmentBrochureCategoryListAdminBinding binding;

    private static final String TAG = "BROCHURE_TAG";

    private Context mContext;

    private ArrayList<ModelBrochureCategory> brochureCategoryArrayList;
    private AdapterBrochureCategoryAdmin adapterBrochureCategoryAdmin;

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    public BrochureCategoryListAdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBrochureCategoryListAdminBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadBrochureCategories();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterBrochureCategoryAdmin.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle addBrochureFab click, start BrochureCategoryAddActivity
        binding.addBrochureFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BrochureCategoryAddActivity.class);
                intent.putExtra("isEdit", false);
                startActivity(intent);
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

                //init/setup adapter and set to recyclerview
                adapterBrochureCategoryAdmin = new AdapterBrochureCategoryAdmin(mContext, brochureCategoryArrayList);
                binding.brochureRv.setAdapter(adapterBrochureCategoryAdmin);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
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

import com.example.smartshopsaver.activities.BrochureAddSellerActivity;
import com.example.smartshopsaver.adapters.AdapterBrochureSeller;
import com.example.smartshopsaver.databinding.FragmentBrochureListSellerBinding;
import com.example.smartshopsaver.models.ModelBrochure;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BrochureListSellerFragment extends Fragment {

    private FragmentBrochureListSellerBinding binding;

    private static final String TAG = "BROCHURE_ADD_TAG";

    private FirebaseAuth firebaseAuth;

    private Context mContext;

    private ArrayList<ModelBrochure> brochureArrayList;
    private AdapterBrochureSeller adapterBrochureSeller;

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    public BrochureListSellerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBrochureListSellerBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadBrochures();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterBrochureSeller.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle addBrochureFab click, start BrochureAddSellerActivity
        binding.addBrochureFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BrochureAddSellerActivity.class);
                intent.putExtra("isEdit", false);
                startActivity(intent);
            }
        });
    }

    private void loadBrochures() {
        //init list before starting adding data into it
        brochureArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.orderByChild("uid").equalTo("" + firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
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
                        adapterBrochureSeller = new AdapterBrochureSeller(mContext, brochureArrayList);
                        binding.brochureRv.setAdapter(adapterBrochureSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}
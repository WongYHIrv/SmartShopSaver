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

import com.example.smartshopsaver.adapters.AdapterReportOrderAdmin;
import com.example.smartshopsaver.databinding.FragmentReportOrderListAdminBinding;
import com.example.smartshopsaver.models.ModelReportOrder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReportOrderListAdminFragment extends Fragment {

    private FragmentReportOrderListAdminBinding binding;

    private static final String TAG = "PRODUCTS_TAG";

    private FirebaseAuth firebaseAuth;

    private Context mContext;

    private ArrayList<ModelReportOrder> reportOrderArrayList;
    private AdapterReportOrderAdmin adapterReportOrderAdmin;

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    public ReportOrderListAdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReportOrderListAdminBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadReportOrders();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterReportOrderAdmin.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadReportOrders() {
        //init list before starting adding data into it
        reportOrderArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportOrders");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it, each time there is a change in list
                reportOrderArrayList.clear();

                //load data into list from firebase db
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelReportOrder modelBrochureCategory = ds.getValue(ModelReportOrder.class);
                        reportOrderArrayList.add(modelBrochureCategory);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }
                }

                //init/setup adapter and set to recyclerview
                adapterReportOrderAdmin = new AdapterReportOrderAdmin(mContext, reportOrderArrayList);
                binding.reportsRv.setAdapter(adapterReportOrderAdmin);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
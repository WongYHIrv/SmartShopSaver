package com.example.smartshopsaver.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.adapters.AdapterOrderUser;
import com.example.smartshopsaver.databinding.FragmentCartUserBinding;
import com.example.smartshopsaver.databinding.FragmentOrderListUserBinding;
import com.example.smartshopsaver.models.ModelOrder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class OrderListUserFragment extends Fragment {

    private FragmentOrderListUserBinding binding;

    private static final String TAG = "ORDER_TAG";

    private FirebaseAuth firebaseAuth;

    private Context mContext;

    private ArrayList<ModelOrder> orderArrayList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    public OrderListUserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrderListUserBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        binding.filterTv.setText("Showing All Orders");

        loadOrders();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterOrderUser.getFilter().filter("" + s);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterOptions();
            }
        });

    }

    private void showFilterOptions() {
        //init popup menu param 1 is context and param 2 is the UI View (profileImagePickFab) to above/below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(mContext, binding.filterBtn);
        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "All");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "" + Constants.ORDER_STATUS_PLACED);
        popupMenu.getMenu().add(Menu.NONE, 3, 3, "" + Constants.ORDER_STATUS_IN_PROGRESS);
        popupMenu.getMenu().add(Menu.NONE, 4, 4, "" + Constants.ORDER_STATUS_OUT_FOR_DELIVERY);
        popupMenu.getMenu().add(Menu.NONE, 5, 5, "" + Constants.ORDER_STATUS_DELIVERED);
        popupMenu.getMenu().add(Menu.NONE, 6, 6, "" + Constants.ORDER_STATUS_CANCELLED);

        //Show Popup Menu
        popupMenu.show();
        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the id of the menu item clicked
                String newOrderStatus = "" + item.getTitle();

                if (newOrderStatus.equals("All")) {
                    binding.filterTv.setText("Showing All Orders");
                    try {
                        adapterOrderUser.getFilter().filter("");
                    } catch (Exception e) {
                        Log.e(TAG, "onMenuItemClick: ", e);
                    }
                } else {
                    binding.filterTv.setText("Showing Orders with Status: " + newOrderStatus);
                    try {
                        adapterOrderUser.getFilter().filter(newOrderStatus);
                    } catch (Exception e) {
                        Log.e(TAG, "onMenuItemClick: ", e);
                    }
                }

                return true;
            }
        });
    }

    private void loadOrders() {
        orderArrayList = new ArrayList<>();

        String myUid = "" + firebaseAuth.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
        ref.orderByChild("orderBy").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        orderArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                ModelOrder modelOrder = ds.getValue(ModelOrder.class);
                                orderArrayList.add(modelOrder);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        adapterOrderUser = new AdapterOrderUser(mContext, orderArrayList);
                        binding.ordersRv.setAdapter(adapterOrderUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
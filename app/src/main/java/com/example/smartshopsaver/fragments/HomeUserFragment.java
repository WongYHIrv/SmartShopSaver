package com.example.smartshopsaver.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.activities.BrochureListUserActivity;
import com.example.smartshopsaver.activities.ExpenseManagementUserActivity;
import com.example.smartshopsaver.activities.MainUserActivity;
import com.example.smartshopsaver.activities.ProductCategoryListUserActivity;
import com.example.smartshopsaver.activities.ProductListUserActivity;
import com.example.smartshopsaver.activities.ReceiptListUserActivity;
import com.example.smartshopsaver.activities.RouteComparisonUserActivity;
import com.example.smartshopsaver.databinding.FragmentHomeUserBinding;
import com.google.firebase.auth.FirebaseAuth;

public class HomeUserFragment extends Fragment {

    private FragmentHomeUserBinding binding;

    private static final String TAG = "HOME_TAG";

    private FirebaseAuth firebaseAuth;

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }


    public HomeUserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeUserBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //handle brochureCv click, start BrochureListUserActivity
        binding.brochureCv.setOnClickListener(v -> startActivity(new Intent(mContext, BrochureListUserActivity.class)));

        //handle productCategoriesCv click, start ProductCategoryListUserActivity
        binding.productCategoriesCv.setOnClickListener(v -> {
            Constants.PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_DETAILS;
            Intent intent = new Intent(mContext, ProductCategoryListUserActivity.class);
            startActivity(intent);
        });

        //handle productComparisonCv click, start ProductCategoryListUserActivity
        binding.productComparisonCv.setOnClickListener(v -> {
            Constants.PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_COMPARISON;
            Intent intent = new Intent(mContext, ProductCategoryListUserActivity.class);
            startActivity(intent);
        });

        //handle productsCv click, start ProductListUserActivity
        binding.productsCv.setOnClickListener(v -> {
            Constants.PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_DETAILS;
            startActivity(new Intent(mContext, ProductListUserActivity.class));
        });

        //handle shopsCv click, show ShopListUserFragment
        binding.shopsCv.setOnClickListener(v -> {
            Constants.PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_DETAILS;
            ((MainUserActivity) mContext).fragmentShopListUser();
        });

        //handle shopRouteComparisonCv click, start RouteComparisonUserActivity
        binding.shopRouteComparisonCv.setOnClickListener(v -> {
            startActivity(new Intent(mContext, RouteComparisonUserActivity.class));
        });

        //handle cartCv click, show CartUserFragment
        binding.cartCv.setOnClickListener(v -> {
            Constants.PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_DETAILS;
            ((MainUserActivity) mContext).fragmentCart();
        });

        //handle ordersCv click, show OrderListUserFragment
        binding.ordersCv.setOnClickListener(v -> {
            Constants.PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_DETAILS;
            ((MainUserActivity) mContext).fragmentOrders();
        });

        //Handle expenseCv click
        binding.expenseCv.setOnClickListener(v -> startActivity(new Intent(mContext, ExpenseManagementUserActivity.class)));

        //Handle receiptCv click
        binding.receiptCv.setOnClickListener(v -> startActivity(new Intent(mContext, ReceiptListUserActivity.class)));


    }
}
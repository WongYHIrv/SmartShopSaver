package com.example.smartshopsaver.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.ShopDetailsUserActivity;
import com.example.smartshopsaver.databinding.RowShopUserBinding;
import com.example.smartshopsaver.filters.FilterShopUser;
import com.example.smartshopsaver.models.ModelShop;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AdapterShopUser extends RecyclerView.Adapter<AdapterShopUser.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowShopUserBinding binding;

    private static final String TAG = "PRODUCT_SELLER_TAG";

    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelShop> shopArrayList;
    private ArrayList<ModelShop> filterList;
    private FilterShopUser filter;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    /**
     * Constructor*
     *
     * @param context       The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param shopArrayList The list of categories
     */
    public AdapterShopUser(Context context, ArrayList<ModelShop> shopArrayList) {
        this.context = context;
        this.shopArrayList = shopArrayList;
        this.filterList = shopArrayList;

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_user.xml
        binding = RowShopUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelShop modelProduct = shopArrayList.get(position);

        String shopName = "" + modelProduct.getShopName();
        String shopDescription = "" + modelProduct.getShopDescription();
        String deliveryFee = "" + modelProduct.getDeliveryFee();
        String profileImage = "" + modelProduct.getProfileImage();

        holder.shopNameTv.setText(shopName);
        holder.shopDescriptionTv.setText(shopDescription);
        holder.deliveryFeeTv.setText(deliveryFee);

        try {
            Glide.with(context)
                    .load(profileImage)
                    .placeholder(R.drawable.store_white)
                    .into(holder.shopIv);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsUserActivity.class);
                intent.putExtra("shopId", modelProduct.getUid());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        //return the size of list
        return shopArrayList.size();
    }

    @Override
    public FilterShopUser getFilter() {
        if (filter == null) {
            filter = new FilterShopUser(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_product_seller.xml
        ShapeableImageView shopIv;
        TextView shopNameTv, shopDescriptionTv, deliveryFeeTv;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            shopIv = binding.shopIv;
            shopNameTv = binding.shopNameTv;
            shopDescriptionTv = binding.shopDescriptionTv;
            deliveryFeeTv = binding.deliveryFeeTv;
        }
    }
}

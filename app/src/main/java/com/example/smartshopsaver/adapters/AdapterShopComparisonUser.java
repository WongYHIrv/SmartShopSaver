package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.ProductDetailsSellersActivity;
import com.example.smartshopsaver.activities.ShopDetailsUserActivity;
import com.example.smartshopsaver.databinding.RowShopComparisonUserBinding;
import com.example.smartshopsaver.models.ModelShop;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdapterShopComparisonUser extends RecyclerView.Adapter<AdapterShopComparisonUser.HolderBrochureSeller> {

    //View Binding
    private RowShopComparisonUserBinding binding;

    private static final String TAG = "PRODUCT_SELLER_TAG";

    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelShop> shopArrayList;

    /**
     * Constructor*
     *
     * @param context       The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param shopArrayList The list of categories
     */
    public AdapterShopComparisonUser(Context context, ArrayList<ModelShop> shopArrayList) {
        this.context = context;
        this.shopArrayList = shopArrayList;
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_user.xml
        binding = RowShopComparisonUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelShop modelShop = shopArrayList.get(position);

        String shopName = "" + modelShop.getShopName();
        String profileImage = "" + modelShop.getProfileImage();
        String shopDescription = "" + modelShop.getShopDescription();
        String comparingProductPrice = "" + modelShop.getComparingProductPrice();
        String comparingProductTimestamp = "" + modelShop.getComparingProductTimestamp();

        String formattedTimestamp = MyUtils.formatTimestampDate(Long.parseLong(comparingProductTimestamp));

        holder.shopNameTv.setText(shopName);
        holder.shopDescriptionTv.setText(shopDescription);
        holder.priceTv.setText(comparingProductPrice);
        holder.dateTv.setText(formattedTimestamp);

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
                Intent intent = new Intent(context, ProductDetailsSellersActivity.class);
                intent.putExtra("productId", modelShop.getComparingProductId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        //return the size of list
        return shopArrayList.size();
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_product_seller.xml
        ShapeableImageView shopIv;
        TextView shopNameTv, shopDescriptionTv, priceTv, dateTv;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            shopIv = binding.shopIv;
            shopNameTv = binding.shopNameTv;
            shopDescriptionTv = binding.shopDescriptionTv;
            priceTv = binding.priceTv;
            dateTv = binding.dateTv;
        }
    }
}

package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartshopsaver.activities.ProductByCategoryListUserActivity;
import com.example.smartshopsaver.databinding.RowProductCategoryUserBinding;
import com.example.smartshopsaver.filters.FilterProductCategoryUser;
import com.example.smartshopsaver.models.ModelProductCategory;

import java.util.ArrayList;

public class AdapterProductCategoryUser extends RecyclerView.Adapter<AdapterProductCategoryUser.HolderProductCategory> implements Filterable {

    //View Binding row_product_category_user.xml
    private RowProductCategoryUserBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelProductCategory> productCategoryArrayList;
    private ArrayList<ModelProductCategory> filterList;
    private FilterProductCategoryUser filter;

    /**
     * Constructor*
     *
     * @param context                  The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param productCategoryArrayList The list of categories
     */
    public AdapterProductCategoryUser(Context context, ArrayList<ModelProductCategory> productCategoryArrayList) {
        this.context = context;
        this.productCategoryArrayList = productCategoryArrayList;
        this.filterList = productCategoryArrayList;
    }

    @NonNull
    @Override
    public HolderProductCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_category_user.xml
        binding = RowProductCategoryUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderProductCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductCategory holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelProductCategory modelCategory = productCategoryArrayList.get(position);

        //get data from modelCategory
        String category = modelCategory.getProductCategory();

        //set data to UI Views of row_category.xml
        holder.productCategoryTv.setText(category);
        //holder.categoryIconIv.setBackgroundColor(color);

        //handle itemView click, start
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductByCategoryListUserActivity.class);
            intent.putExtra("productCategoryId", "" + modelCategory.getProductCategoryId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        //return the size of list
        return productCategoryArrayList.size();
    }

    @Override
    public FilterProductCategoryUser getFilter() {
        if (filter == null) {
            filter = new FilterProductCategoryUser(this, filterList);
        }
        return filter;
    }

    class HolderProductCategory extends RecyclerView.ViewHolder {

        //UI Views of the row_product_category_user.xml
        TextView productCategoryTv;

        public HolderProductCategory(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_category_user.xml
            productCategoryTv = binding.productCategoryTv;
        }
    }
}
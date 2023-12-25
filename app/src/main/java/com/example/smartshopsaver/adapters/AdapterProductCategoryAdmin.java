package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.activities.ProductCategoryAddAdminActivity;
import com.example.smartshopsaver.activities.ProductSubCategoryListAdminActivity;
import com.example.smartshopsaver.databinding.RowProductCategoryAdminBinding;
import com.example.smartshopsaver.filters.FilterProductCategoryAdmin;
import com.example.smartshopsaver.models.ModelProductCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class AdapterProductCategoryAdmin extends RecyclerView.Adapter<AdapterProductCategoryAdmin.HolderProductCategory> implements Filterable {

    //View Binding
    private RowProductCategoryAdminBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelProductCategory> productCategoryArrayList;
    private ArrayList<ModelProductCategory> filterList;
    private FilterProductCategoryAdmin filter;

    private static final String TAG = "BROCHURE_PRO_CAT_TAG";

    /**
     * Constructor*
     *
     * @param context                  The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param productCategoryArrayList The list of categories
     */
    public AdapterProductCategoryAdmin(Context context, ArrayList<ModelProductCategory> productCategoryArrayList) {
        this.context = context;
        this.productCategoryArrayList = productCategoryArrayList;
        this.filterList = productCategoryArrayList;
    }

    @NonNull
    @Override
    public HolderProductCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_category_admin.xml
        binding = RowProductCategoryAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderProductCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductCategory holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelProductCategory modelCategory = productCategoryArrayList.get(position);

        //get data from modelCategory
        String category = modelCategory.getProductCategory();

        //get random color to set as background color of the categoryIconIv
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));

        //set data to UI Views of row_category.xml
        holder.productCategoryTv.setText(category);
        //holder.categoryIconIv.setBackgroundColor(color);

        //handle itemView click, start
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductSubCategoryListAdminActivity.class);
                intent.putExtra("productCategoryId", "" + modelCategory.getProductCategoryId());
                context.startActivity(intent);
            }
        });

        //handle optionsBtn click, show more options e.g. Edit/Delete
        holder.optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsDialog(modelCategory, holder);
            }
        });


    }

    private void optionsDialog(ModelProductCategory modelCategory, HolderProductCategory holder) {
        //init popup menu param 1 is context and param 2 is the UI View (profileImagePickFab) to above/below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(context, holder.optionsBtn);
        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Edit");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Delete");
        //Show Popup Menu
        popupMenu.show();
        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the id of the menu item clicked
                int itemId = item.getItemId();
                if (itemId == 1) {
                    //Edit clicked: start BrochureCategoryAddActivity
                    Intent intent = new Intent(context, ProductCategoryAddAdminActivity.class);
                    intent.putExtra("isEdit", true);
                    intent.putExtra("productCategoryId", modelCategory.getProductCategoryId());
                    context.startActivity(intent);
                } else if (itemId == 2) {
                    //Delete clicked: confirm and delete
                    deleteProductCategory(modelCategory, holder);
                }
                return true;
            }
        });
    }

    private void deleteProductCategory(ModelProductCategory modelCategory, HolderProductCategory holder) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete " + modelCategory.getProductCategory() + " ?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String brochureCategoryId = "" + modelCategory.getProductCategoryId();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
                        ref.child(brochureCategoryId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        MyUtils.toast(context, "Deleted...!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        MyUtils.toast(context, "Failed to delete due to " + e.getMessage());
                                    }
                                });

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        //return the size of list
        return productCategoryArrayList.size();
    }

    @Override
    public FilterProductCategoryAdmin getFilter() {
        if (filter == null) {
            filter = new FilterProductCategoryAdmin(this, filterList);
        }
        return filter;
    }

    class HolderProductCategory extends RecyclerView.ViewHolder {

        //UI Views of the row_product_category_admin.xml
        TextView productCategoryTv;
        ImageButton optionsBtn;

        public HolderProductCategory(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_category.xml
            productCategoryTv = binding.productCategoryTv;
            optionsBtn = binding.optionsBtn;
        }
    }
}

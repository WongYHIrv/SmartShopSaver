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
import com.example.smartshopsaver.activities.BrochureCategoryAddActivity;
import com.example.smartshopsaver.databinding.RowBrochureCategoryAdminBinding;
import com.example.smartshopsaver.filters.FilterBrochureCategoryAdmin;
import com.example.smartshopsaver.models.ModelBrochureCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class AdapterBrochureCategoryAdmin extends RecyclerView.Adapter<AdapterBrochureCategoryAdmin.HolderBrochureCategory> implements Filterable {

    //View Binding
    private RowBrochureCategoryAdminBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelBrochureCategory> brochureCategoryArrayList;
    private ArrayList<ModelBrochureCategory> filterList;
    private FilterBrochureCategoryAdmin filter;

    private static final String TAG = "BROCHURE_CAT_TAG";

    /**
     * Constructor*
     *
     * @param context                   The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param brochureCategoryArrayList The list of categories
     */
    public AdapterBrochureCategoryAdmin(Context context, ArrayList<ModelBrochureCategory> brochureCategoryArrayList) {
        this.context = context;
        this.brochureCategoryArrayList = brochureCategoryArrayList;
        this.filterList = brochureCategoryArrayList;
    }

    @NonNull
    @Override
    public HolderBrochureCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_brochure_category_admin.xml
        binding = RowBrochureCategoryAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureCategory holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelBrochureCategory modelCategory = brochureCategoryArrayList.get(position);

        //get data from modelCategory
        String category = modelCategory.getBrochureCategory();

        //get random color to set as background color of the categoryIconIv
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));

        //set data to UI Views of row_category.xml
        holder.brochureCategoryTv.setText(category);
        //holder.categoryIconIv.setBackgroundColor(color);

        //handle optionsBtn click, show more options e.g. Edit/Delete
        holder.optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsDialog(modelCategory, holder);
            }
        });


    }

    private void optionsDialog(ModelBrochureCategory modelCategory, HolderBrochureCategory holder) {
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
                    Intent intent = new Intent(context, BrochureCategoryAddActivity.class);
                    intent.putExtra("isEdit", true);
                    intent.putExtra("brochureId", modelCategory.getId());
                    context.startActivity(intent);
                } else if (itemId == 2) {
                    //Delete clicked: confirm and delete
                    deleteBrochureCategory(modelCategory, holder);
                }
                return true;
            }
        });
    }

    private void deleteBrochureCategory(ModelBrochureCategory modelCategory, HolderBrochureCategory holder) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete " + modelCategory.getBrochureCategory() + " ?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String brochureCategoryId = "" + modelCategory.getId();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrochureCategories");
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
        return brochureCategoryArrayList.size();
    }

    @Override
    public FilterBrochureCategoryAdmin getFilter() {
        if (filter == null) {
            filter = new FilterBrochureCategoryAdmin(this, filterList);
        }
        return filter;
    }

    class HolderBrochureCategory extends RecyclerView.ViewHolder {

        //UI Views of the row_brochure_category_admin.xml
        TextView brochureCategoryTv;
        ImageButton optionsBtn;

        public HolderBrochureCategory(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_category.xml
            brochureCategoryTv = binding.brochureCategoryTv;
            optionsBtn = binding.optionsBtn;
        }
    }
}

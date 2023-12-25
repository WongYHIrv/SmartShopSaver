package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.ProductAddActivity;
import com.example.smartshopsaver.activities.ProductDetailsSellersActivity;
import com.example.smartshopsaver.databinding.RowProductSellerBinding;
import com.example.smartshopsaver.filters.FilterProductSeller;
import com.example.smartshopsaver.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowProductSellerBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelProduct> productArrayList;
    private ArrayList<ModelProduct> filterList;
    private FilterProductSeller filter;

    private static final String TAG = "PRODUCT_SELLER_TAG";

    /**
     * Constructor*
     *
     * @param context          The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param productArrayList The list of categories
     */
    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
        this.filterList = productArrayList;
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_seller.xml
        binding = RowProductSellerBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelProduct modelProduct = productArrayList.get(position);

        holder.productExpireNoteCv.setVisibility(View.GONE);

        String productName = "" + modelProduct.getProductName();
        String productDescription = "" + modelProduct.getProductDescription();
        String productPrice = "" + modelProduct.getProductPrice();
        String imageUrl = "" + modelProduct.getImageUrl();
        String productStock = "" + modelProduct.getProductStock();
        String productExpireDate = "" + modelProduct.getProductExpireDate();
        long timestamp = modelProduct.getTimestamp();
        boolean discountAvailable = modelProduct.isDiscountAvailable();
        String productDiscountPrice = "" + modelProduct.getProductDiscountPrice();
        String productDiscountNote = "" + modelProduct.getProductDiscountNote();

        String formattedDate = MyUtils.formatTimestampDate(timestamp);

        if (productPrice.isEmpty() || productPrice.equals("null")) {
            productPrice = "0";
        }
        if (productDiscountPrice.isEmpty() || productDiscountPrice.equals("null")) {
            productDiscountPrice = "0";
        }

        holder.productNameTv.setText(productName);
        holder.productDescriptionTv.setText(productDescription);
        holder.productDateAddedTv.setText(formattedDate);
        holder.productDateExpireTv.setText(productExpireDate);
        holder.stockTv.setText(productStock);

        if (discountAvailable) {
            holder.discountedPriceSymbolTv.setVisibility(View.VISIBLE);
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.productDiscountNoteCv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            double originalPrice = Double.parseDouble(productPrice);
            double discountedPrice = Double.parseDouble(productDiscountPrice);
            double newAfterDiscountPrice = originalPrice - discountedPrice;

            holder.originalPriceTv.setText("" + MyUtils.roundedDecimalValue(originalPrice));
            holder.discountedPriceTv.setText("" + MyUtils.roundedDecimalValue(newAfterDiscountPrice));
            holder.productDiscountNoteTv.setText("" + productDiscountNote);
        } else {
            holder.discountedPriceSymbolTv.setVisibility(View.GONE);
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.productDiscountNoteCv.setVisibility(View.GONE);

            double originalPrice = Double.parseDouble(productPrice);

            holder.originalPriceTv.setText("" + MyUtils.roundedDecimalValue(originalPrice));
        }

        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.cart_white)
                    .into(holder.productImageIv);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }

        loadProductCategory(modelProduct, holder);
        loadProductSubCategory(modelProduct, holder);
        checkExpireStatus(modelProduct, holder);

        //handle optionsBtn click, show more options e.g. Edit/Delete
        holder.optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsDialog(modelProduct, holder);
            }
        });

        //handle itemView click, start ProductDetailsSellersActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductDetailsSellersActivity.class);
                intent.putExtra("productId", modelProduct.getProductId());
                context.startActivity(intent);
            }
        });

    }

    private void loadProductCategory(ModelProduct modelCategory, HolderBrochureSeller holder) {
        String productCategoryId = modelCategory.getProductCategoryId();

        DatabaseReference refCategory = FirebaseDatabase.getInstance().getReference("ProductCategories");
        refCategory.child(productCategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productCategory = "" + snapshot.child("productCategory").getValue();
                        modelCategory.setProductCategory(productCategory);

                        holder.productCategoryTv.setText(productCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductSubCategory(ModelProduct modelCategory, HolderBrochureSeller holder) {
        String productCategoryId = modelCategory.getProductCategoryId();
        String productSubCategoryId = modelCategory.getProductSubCategoryId();

        DatabaseReference refSubCategory = FirebaseDatabase.getInstance().getReference("ProductCategories");
        refSubCategory.child(productCategoryId).child("ProductSubCategories").child(productSubCategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productSubCategory = "" + snapshot.child("productSubCategory").getValue();
                        modelCategory.setProductSubCategory(productSubCategory);

                        holder.productSubCategoryTv.setText(productSubCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void optionsDialog(ModelProduct modelCategory, HolderBrochureSeller holder) {
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
                    //Edit clicked: start ProductAddActivity
                    Intent intent = new Intent(context, ProductAddActivity.class);
                    intent.putExtra("isEdit", true);
                    intent.putExtra("productId", modelCategory.getProductId());
                    context.startActivity(intent);
                } else if (itemId == 2) {
                    //Delete clicked: confirm and delete
                    deleteBrochureCategory(modelCategory, holder);
                }
                return true;
            }
        });
    }

    private void deleteBrochureCategory(ModelProduct modelCategory, HolderBrochureSeller holder) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete " + modelCategory.getProductName() + " ?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String productId = "" + modelCategory.getProductId();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
                        ref.child(productId)
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

    private void checkExpireStatus(ModelProduct modelProduct, HolderBrochureSeller holder) {
        //---Expire Date---
        String expireDate = modelProduct.getProductExpireDate();
        String currentDate = MyUtils.formatTimestampDate(System.currentTimeMillis());
        //Get day, month, year from current date
        String curDay = MyUtils.formatOneDateToAnother("dd/MM/yyyy", "dd", currentDate);
        String curMonth = MyUtils.formatOneDateToAnother("dd/MM/yyyy", "MM", currentDate);
        String curYear = MyUtils.formatOneDateToAnother("dd/MM/yyyy", "yyyy", currentDate);
        //Get day, month, year from expire date
        String expDay = MyUtils.formatOneDateToAnother("dd/MM/yyyy", "dd", expireDate);
        String expMonth = MyUtils.formatOneDateToAnother("dd/MM/yyyy", "MM", expireDate);
        String expYear = MyUtils.formatOneDateToAnother("dd/MM/yyyy", "yyyy", expireDate);
        //Calendar with current date
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(curDay));
        currentCalendar.set(Calendar.MONTH, Integer.parseInt(curMonth));
        currentCalendar.set(Calendar.YEAR, Integer.parseInt(curYear));
        //Calendar with expire date
        Calendar expireCalendar = Calendar.getInstance();
        expireCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(expDay));
        expireCalendar.set(Calendar.MONTH, Integer.parseInt(expMonth));
        expireCalendar.set(Calendar.YEAR, Integer.parseInt(expYear));
        //Calculate no of days between expire date and current date
        double noOfDays = MyUtils.daysBetweenTwoTimestamps(
                currentCalendar.getTimeInMillis(),
                expireCalendar.getTimeInMillis()
        );
        if (noOfDays <= 0) {
            modelProduct.setExpired(true);
            holder.productExpireNoteCv.setVisibility(View.VISIBLE);
        } else {
            modelProduct.setExpired(false);
            holder.productExpireNoteCv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        //return the size of list
        return productArrayList.size();
    }

    @Override
    public FilterProductSeller getFilter() {
        if (filter == null) {
            filter = new FilterProductSeller(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_product_seller.xml
        ShapeableImageView productImageIv;
        MaterialCardView productDiscountNoteCv, productExpireNoteCv;
        TextView productDiscountNoteTv, productExpireNoteTv, productNameTv, productDescriptionTv, stockTv, originalPriceSymbolTv, originalPriceTv, discountedPriceSymbolTv, discountedPriceTv, productCategoryTv, productSubCategoryTv, productDateAddedTv, productDateExpireTv;
        ImageButton optionsBtn;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            productImageIv = binding.productImageIv;
            productDiscountNoteCv = binding.productDiscountNoteCv;
            productExpireNoteCv = binding.productExpireNoteCv;
            productDiscountNoteTv = binding.productDiscountNoteTv;
            productExpireNoteTv = binding.productExpireNoteTv;
            productNameTv = binding.productNameTv;
            productDescriptionTv = binding.productDescriptionTv;
            stockTv = binding.stockTv;
            originalPriceSymbolTv = binding.originalPriceSymbolTv;
            originalPriceTv = binding.originalPriceTv;
            discountedPriceSymbolTv = binding.discountedPriceSymbolTv;
            discountedPriceTv = binding.discountedPriceTv;
            productCategoryTv = binding.productCategoryTv;
            productSubCategoryTv = binding.productSubCategoryTv;
            productDateAddedTv = binding.productDateAddedTv;
            productDateExpireTv = binding.productDateExpireTv;
            optionsBtn = binding.optionsBtn;
        }
    }
}

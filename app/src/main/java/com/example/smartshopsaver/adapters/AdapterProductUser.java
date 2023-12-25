package com.example.smartshopsaver.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.ProductComparisonUserActivity;
import com.example.smartshopsaver.activities.ProductDetailsSellersActivity;
import com.example.smartshopsaver.activities.ReportProductUserActivity;
import com.example.smartshopsaver.databinding.RowProductUserBinding;
import com.example.smartshopsaver.filters.FilterProductUser;
import com.example.smartshopsaver.models.ModelProduct;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowProductUserBinding binding;

    private static final String TAG = "PRODUCT_SELLER_TAG";

    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelProduct> productArrayList;
    private ArrayList<ModelProduct> filterList;
    private FilterProductUser filter;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    /**
     * Constructor*
     *
     * @param context          The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param productArrayList The list of categories
     */
    public AdapterProductUser(Context context, ArrayList<ModelProduct> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
        this.filterList = productArrayList;

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_user.xml
        binding = RowProductUserBinding.inflate(LayoutInflater.from(context), parent, false);

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

        loadSellerInfo(modelProduct, holder);
        loadProductCategory(modelProduct, holder);
        loadProductSubCategory(modelProduct, holder);
        checkExpireStatus(modelProduct, holder);

        //handle optionsBtn click, show more options e.g. Edit/Delete
        holder.cartBtn.setOnClickListener(v -> {
            long stock = modelProduct.getProductStock();
            boolean expired = modelProduct.isExpired();
            if (stock < 1) {
                MyUtils.toast(context, "Product is out of stock!");
            } else if (expired) {
                MyUtils.toast(context, "Product is expired");
            } else {
                addToCart1(modelProduct);
            }
        });

        //handle reportBtn click, show option to report product
        holder.reportBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportProductUserActivity.class);
            intent.putExtra("productId", modelProduct.getProductId());
            intent.putExtra("sellerUid", modelProduct.getUid());
            context.startActivity(intent);
        });

        //handle itemView click, start ProductDetailsSellersActivity
        holder.itemView.setOnClickListener(v -> {
            if (Constants.PRODUCT_ACTION_TYPE.equals(Constants.PRODUCT_ACTION_TYPE_DETAILS)) {
                Intent intent = new Intent(context, ProductDetailsSellersActivity.class);
                intent.putExtra("productId", modelProduct.getProductId());
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, ProductComparisonUserActivity.class);
                intent.putExtra("productId", modelProduct.getProductId());
                intent.putExtra("productBarcode", modelProduct.getProductBarcode());
                context.startActivity(intent);
            }
        });

    }

    private void loadSellerInfo(ModelProduct modelProduct, HolderBrochureSeller holder) {
        String sellerUid = "" + modelProduct.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        holder.sellerTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void addToCart1(ModelProduct modelProduct) {
        progressDialog.setMessage("Checking cart...!");
        progressDialog.show();

        String productId = "" + modelProduct.getProductId();
        String sellerUid = "" + modelProduct.getUid();
        String myUid = "" + firebaseAuth.getUid();
        long timestamp = MyUtils.getTimestamp();
        String cartMessage = "";

        //1) Check if product is in Cart or not
        DatabaseReference refCheckInCart = FirebaseDatabase.getInstance().getReference("Users");
        refCheckInCart.child(myUid).child("Cart").child(sellerUid).child("Products").child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> hashMapProductInfo = new HashMap<>();

                        if (snapshot.exists()) {
                            //Product already in cart, we will update quantity
                            String productQuantityDb = "" + snapshot.child("productQuantity").getValue();
                            if (productQuantityDb.equals("") || productQuantityDb.equals("null")) {
                                productQuantityDb = "0";
                            }

                            long productQuantity = Long.parseLong(productQuantityDb) + 1;

                            hashMapProductInfo.put("productQuantity", productQuantity);
                            hashMapProductInfo.put("timestamp", timestamp);

                            progressDialog.setMessage("Product already in cart, updating quantity...!");
                        } else {
                            //Product not in car, add as new
                            hashMapProductInfo.put("productId", "" + productId);
                            hashMapProductInfo.put("sellerUid", "" + sellerUid);
                            hashMapProductInfo.put("productQuantity", 1);
                            hashMapProductInfo.put("timestamp", timestamp);

                            progressDialog.setMessage("Adding to cart...!");
                        }

                        HashMap<String, Object> hashMapSeller = new HashMap<>();
                        hashMapSeller.put("sellerUid", "" + sellerUid);
                        hashMapSeller.put("timestamp", timestamp);

                        //2) Add/Update to cart
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(myUid).child("Cart").child(sellerUid)
                                .updateChildren(hashMapSeller)
                                .addOnSuccessListener(unused -> {
                                    progressDialog.dismiss();
                                    MyUtils.toast(context, "Done!");

                                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                                    ref1.child(myUid).child("Cart").child(sellerUid).child("Products")
                                            .child(productId).updateChildren(hashMapProductInfo);
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    MyUtils.toast(context, "Failed due to " + e.getMessage());
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
    public FilterProductUser getFilter() {
        if (filter == null) {
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_product_seller.xml
        ShapeableImageView productImageIv;
        MaterialCardView productDiscountNoteCv, productExpireNoteCv;
        TextView productDiscountNoteTv, productExpireNoteTv, productNameTv, productDescriptionTv, stockTv, sellerTv, originalPriceSymbolTv, originalPriceTv, discountedPriceSymbolTv, discountedPriceTv, productCategoryTv, productSubCategoryTv, productDateAddedTv, productDateExpireTv;
        ImageButton cartBtn, reportBtn;

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
            sellerTv = binding.sellerTv;
            originalPriceSymbolTv = binding.originalPriceSymbolTv;
            originalPriceTv = binding.originalPriceTv;
            discountedPriceSymbolTv = binding.discountedPriceSymbolTv;
            discountedPriceTv = binding.discountedPriceTv;
            productCategoryTv = binding.productCategoryTv;
            productSubCategoryTv = binding.productSubCategoryTv;
            productDateAddedTv = binding.productDateAddedTv;
            productDateExpireTv = binding.productDateExpireTv;
            cartBtn = binding.cartBtn;
            reportBtn = binding.reportBtn;
        }
    }
}

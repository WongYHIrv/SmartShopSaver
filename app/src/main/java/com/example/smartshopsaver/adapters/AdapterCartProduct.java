package com.example.smartshopsaver.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.ProductDetailsSellersActivity;
import com.example.smartshopsaver.databinding.RowCartUserItemBinding;
import com.example.smartshopsaver.models.ModelCartProduct;
import com.example.smartshopsaver.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdapterCartProduct extends RecyclerView.Adapter<AdapterCartProduct.HolderBrochureSeller> {

    //View Binding
    private RowCartUserItemBinding binding;

    private static final String TAG = "CART_ADAPTER_TAG";

    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelCartProduct> cartArrayList;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    /**
     * Constructor*
     *
     * @param context       The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param cartArrayList The list of cart items
     */
    public AdapterCartProduct(Context context, ArrayList<ModelCartProduct> cartArrayList) {
        this.context = context;
        this.cartArrayList = cartArrayList;

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_user.xml
        binding = RowCartUserItemBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelCartProduct modelProduct = cartArrayList.get(position);

        loadProductDetails(modelProduct, holder);

        long quantity = modelProduct.getProductQuantity();
        holder.quantityTv.setText("" + quantity);

        //handle itemView click, start ProductDetailsSellersActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsSellersActivity.class);
            intent.putExtra("productId", modelProduct.getProductId());
            context.startActivity(intent);
        });

        holder.removeFromCartBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Remove Cart Item")
                    .setMessage("Are you sure you want to remove " + modelProduct.getProductName() + " from cart?")
                    .setPositiveButton("REMOVE", (dialog, which) -> removeFromCart(modelProduct))
                    .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        holder.incrementBtn.setOnClickListener(v -> {
            long prevQuantity = modelProduct.getProductQuantity();
            long stock = Integer.parseInt(modelProduct.getProductStock());
            if (prevQuantity < stock) {
                long newQuantity = prevQuantity + 1;
                cartItemUpdate(modelProduct, newQuantity);
            } else {
                Toast.makeText(context, "Max items selected", Toast.LENGTH_SHORT).show();
            }
        });

        holder.decrementBtn.setOnClickListener(v -> {
            long prevQuantity = modelProduct.getProductQuantity();
            if (prevQuantity > 1) {
                long newQuantity = prevQuantity - 1;
                cartItemUpdate(modelProduct, newQuantity);
            }
        });

    }

    private void loadProductDetails(ModelCartProduct modelCart, HolderBrochureSeller holder) {
        String productId = "" + modelCart.getProductId();
        Log.d(TAG, "loadProductDetails: productId: " + productId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            ModelProduct modelProduct1 = snapshot.getValue(ModelProduct.class);

                            String productName = "" + modelProduct1.getProductName();
                            String productDescription = "" + modelProduct1.getProductDescription();
                            String productPrice = "" + modelProduct1.getProductPrice();
                            String imageUrl = "" + modelProduct1.getImageUrl();
                            String productStock = "" + modelProduct1.getProductStock();
                            String productExpireDate = "" + modelProduct1.getProductExpireDate();
                            long timestamp = modelProduct1.getTimestamp();
                            boolean discountAvailable = modelProduct1.isDiscountAvailable();
                            String productDiscountPrice = "" + modelProduct1.getProductDiscountPrice();
                            String productDiscountNote = "" + modelProduct1.getProductDiscountNote();

                            String formattedDate = MyUtils.formatTimestampDate(timestamp);

                            if (productPrice.isEmpty() || productPrice.equals("null")) {
                                productPrice = "0";
                            }
                            if (productDiscountPrice.isEmpty() || productDiscountPrice.equals("null")) {
                                productDiscountPrice = "0";
                            }

                            modelCart.setProductName(productName);
                            modelCart.setProductDescription(productDescription);
                            modelCart.setProductPrice(productPrice);
                            modelCart.setDiscountAvailable(discountAvailable);
                            modelCart.setProductDiscountNote(productDiscountNote);
                            modelCart.setProductDiscountPrice(productDiscountPrice);
                            modelCart.setProductStock(productStock);
                            modelCart.setProductExpireDate(productExpireDate);
                            modelCart.setImageUrl(imageUrl);

                            holder.productNameTv.setText(productName);

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
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void cartItemUpdate(ModelCartProduct modelProduct, long newQuantity) {
        progressDialog.setMessage("Updating cart item quantity...!");
        progressDialog.show();

        String myUid = "" + firebaseAuth.getUid();
        String sellerUid = "" + modelProduct.getSellerUid();
        String productId = "" + modelProduct.getProductId();

        Map<String, Object> productMap = new HashMap<>();
        productMap.put("productQuantity", newQuantity);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid)
                .child("Cart")
                .child(sellerUid)
                .child("Products")
                .child(productId)
                .updateChildren(productMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Updated");
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                    }
                });
    }
    private void removeFromCart(ModelCartProduct modelProduct) {
        progressDialog.setMessage("Removing from cart...!");
        progressDialog.show();

        String myUid = "" + firebaseAuth.getUid();
        String sellerUid = "" + modelProduct.getSellerUid();
        String productId = "" + modelProduct.getProductId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid)
                .child("Cart")
                .child(sellerUid)
                .child("Products")
                .child(productId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Updated");
                    progressDialog.dismiss();

                    MyUtils.checkRemoveCartSellerUidHaveNoProducts();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                    progressDialog.dismiss();
                });
    }

    @Override
    public int getItemCount() {
        //return the size of list
        return cartArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_product_seller.xml
        ShapeableImageView productImageIv;
        MaterialCardView productDiscountNoteCv;
        TextView productDiscountNoteTv, productNameTv, originalPriceSymbolTv, originalPriceTv, discountedPriceSymbolTv, discountedPriceTv, quantityTv;
        ImageButton decrementBtn, incrementBtn, removeFromCartBtn;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            productImageIv = binding.productImageIv;
            productDiscountNoteCv = binding.productDiscountNoteCv;
            productDiscountNoteTv = binding.productDiscountNoteTv;
            productNameTv = binding.productNameTv;
            originalPriceSymbolTv = binding.originalPriceSymbolTv;
            originalPriceTv = binding.originalPriceTv;
            discountedPriceSymbolTv = binding.discountedPriceSymbolTv;
            discountedPriceTv = binding.discountedPriceTv;
            quantityTv = binding.quantityTv;
            decrementBtn = binding.decrementBtn;
            incrementBtn = binding.incrementBtn;
            removeFromCartBtn = binding.removeFromCartBtn;
        }
    }
}

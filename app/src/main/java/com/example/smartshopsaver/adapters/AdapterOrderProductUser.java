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
import com.example.smartshopsaver.databinding.RowOrderProductUserBinding;
import com.example.smartshopsaver.models.ModelCartProduct;
import com.example.smartshopsaver.models.ModelOrderProduct;
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

public class AdapterOrderProductUser extends RecyclerView.Adapter<AdapterOrderProductUser.HolderBrochureSeller> {

    //View Binding
    private RowOrderProductUserBinding binding;

    private static final String TAG = "ORDER_PRODUCT_TAG";

    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelOrderProduct> orderProductArrayList;

    private FirebaseAuth firebaseAuth;

    /**
     * Constructor*
     *
     * @param context               The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param orderProductArrayList The list of ordered product items
     */
    public AdapterOrderProductUser(Context context, ArrayList<ModelOrderProduct> orderProductArrayList) {
        this.context = context;
        this.orderProductArrayList = orderProductArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_product_user.xml
        binding = RowOrderProductUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelOrderProduct modelOrderProduct = orderProductArrayList.get(position);

        loadProductDetails(modelOrderProduct, holder);

        long quantity = modelOrderProduct.getProductQuantity();
        String price = modelOrderProduct.getProductPrice();

        holder.quantityTv.setText("" + quantity);
        holder.originalPriceTv.setText(MyUtils.roundedDecimalValue(Double.parseDouble(price)));

        //handle itemView click, start ProductDetailsSellersActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsSellersActivity.class);
            intent.putExtra("productId", modelOrderProduct.getProductId());
            context.startActivity(intent);
        });

    }

    private void loadProductDetails(ModelOrderProduct modelCart, HolderBrochureSeller holder) {
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
                            String imageUrl = "" + modelProduct1.getImageUrl();


                            modelCart.setProductName(productName);
                            modelCart.setProductDescription(productDescription);
                            modelCart.setImageUrl(imageUrl);

                            holder.productNameTv.setText(productName);
                            holder.productDescriptionTv.setText(productDescription);

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


    @Override
    public int getItemCount() {
        //return the size of list
        return orderProductArrayList.size();
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_product_seller.xml
        ShapeableImageView productImageIv;
        TextView productNameTv, productDescriptionTv, originalPriceTv, quantityTv;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            productImageIv = binding.productImageIv;
            productNameTv = binding.productNameTv;
            productDescriptionTv = binding.productDescriptionTv;
            originalPriceTv = binding.originalPriceTv;
            quantityTv = binding.quantityTv;
        }
    }
}

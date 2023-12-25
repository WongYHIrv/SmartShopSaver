package com.example.smartshopsaver.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartshopsaver.databinding.RowCartUserBinding;
import com.example.smartshopsaver.models.ModelCart;
import com.example.smartshopsaver.models.ModelCartProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterCart extends RecyclerView.Adapter<AdapterCart.HolderBrochureSeller> {

    //View Binding
    private RowCartUserBinding binding;

    private static final String TAG = "CART_ADAPTER_TAG";

    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelCart> cartArrayList;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    /**
     * Constructor*
     *
     * @param context       The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param cartArrayList The list of cart items
     */
    public AdapterCart(Context context, ArrayList<ModelCart> cartArrayList) {
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
        binding = RowCartUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelCart modelCart = cartArrayList.get(position);

        loadSellerDetails(modelCart, holder);
        loadProducts(modelCart, holder);
    }

    private void loadSellerDetails(ModelCart modelCart, HolderBrochureSeller holder) {
        String sellerUid = "" + modelCart.getSellerUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        String fcmToken = "" + snapshot.child("fcmToken").getValue();
                        if (deliveryFee.equals("") || deliveryFee.equals("null")) {
                            deliveryFee = "0.0";
                        }
                        modelCart.setDeliveryFee(Double.parseDouble(deliveryFee));
                        modelCart.setShopName(shopName);
                        modelCart.setSellerFcmToken(fcmToken);

                        holder.sellerNameTv.setText(modelCart.getShopName());
                        holder.deliveryFeeTv.setText(""+modelCart.getDeliveryFee());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProducts(ModelCart modelCart, HolderBrochureSeller holder) {
        String myUid = "" + firebaseAuth.getUid();
        String sellerUid = "" + modelCart.getSellerUid();
        ArrayList<ModelCartProduct> cartProductArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(myUid).child("Cart").child(sellerUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartProductArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelCartProduct modelCartProduct = ds.getValue(ModelCartProduct.class);
                            cartProductArrayList.add(modelCartProduct);
                        }
                        modelCart.setCartProductArrayList(cartProductArrayList);

                        Log.d(TAG, "onDataChange: Size: "+modelCart.getCartProductArrayList().size());
                        AdapterCartProduct adapterCart = new AdapterCartProduct(context, modelCart.getCartProductArrayList());
                        holder.cartItemsRv.setAdapter(adapterCart);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        //return the size of list
        return cartArrayList.size();
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_product_seller.xml
        TextView sellerNameTv, deliveryFeeTv;
        RecyclerView cartItemsRv;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            sellerNameTv = binding.sellerNameTv;
            deliveryFeeTv = binding.deliveryFeeTv;
            cartItemsRv = binding.cartItemsRv;
        }
    }
}

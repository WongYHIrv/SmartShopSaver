package com.example.smartshopsaver.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.adapters.AdapterCart;
import com.example.smartshopsaver.databinding.FragmentCartUserBinding;
import com.example.smartshopsaver.models.ModelCart;
import com.example.smartshopsaver.models.ModelCartProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartUserFragment extends Fragment {

    private FragmentCartUserBinding binding;

    private static final String TAG = "CART_TAG";

    private FirebaseAuth firebaseAuth;
    private String myUid = "";
    private String myName = "";
    private String myLatitude = "";
    private String myLongitude = "";

    private ProgressDialog progressDialog;

    private Context mContext;

    private ArrayList<ModelCart> cartArrayList;

    private AdapterCart adapterCart;

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    public CartUserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCartUserBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        MyUtils.checkRemoveCartSellerUidHaveNoProducts();
        loadCart();
        loadMyInfo();

        binding.placeOrderBtn.setOnClickListener(v -> {
            if (cartArrayList.isEmpty()) {
                Toast.makeText(mContext, "No item in cart...!", Toast.LENGTH_SHORT).show();
            } else {
                placeOrder();
            }
        });
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data from db
                        String accountType = "" + snapshot.child("accountType").getValue();
                        String address = "" + snapshot.child("address").getValue();
                        String city = "" + snapshot.child("city").getValue();
                        String country = "" + snapshot.child("country").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        myLatitude = "" + snapshot.child("latitude").getValue();
                        myLongitude = "" + snapshot.child("longitude").getValue();
                        myName = "" + snapshot.child("name").getValue();
                        String online = "" + snapshot.child("online").getValue();
                        String phone = "" + snapshot.child("phone").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String state = "" + snapshot.child("state").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        if (myLatitude.equals("") || myLatitude.equals("null")) {
                            myLatitude = "0.0";
                        }
                        if (myLongitude.equals("") || myLongitude.equals("null")) {
                            myLongitude = "0.0";
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCart() {
        cartArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + myUid).child("Cart")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cartArrayList.clear();
                        binding.placeOrderBtn.setEnabled(false);
                        Log.d(TAG, "onDataChange: Count: " + snapshot.getChildrenCount());

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            try {
                                ModelCart modelCart = ds.getValue(ModelCart.class);
                                cartArrayList.add(modelCart);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }

                        }

                        adapterCart = new AdapterCart(mContext, cartArrayList);
                        binding.cartRv.setAdapter(adapterCart);

                        Log.d(TAG, "onDataChange: ListSize: " + cartArrayList.size());

                        new Handler().postDelayed(() -> loadCalculations(), 3000);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private void loadCalculations() {
        double allSubtotal = 0.0;
        double allDeliveryCharges = 0.0;
        double allTotal = 0.0;

        //Shop Info/Calculations
        for (int i = 0; i < cartArrayList.size(); i++) {
            double subTotalShop = cartArrayList.get(i).getSubTotal();
            ModelCart modelCart = cartArrayList.get(i);
            ArrayList<ModelCartProduct> cartProductArrayList = modelCart.getCartProductArrayList();

            //Product Info/Calculations
            if (cartProductArrayList != null && !cartProductArrayList.isEmpty()) {
                for (ModelCartProduct modelCartProduct : cartProductArrayList) {
                    long quantity = modelCartProduct.getProductQuantity();
                    String productPrice = modelCartProduct.getProductPrice();
                    String productDiscountPrice = modelCartProduct.getProductDiscountPrice();
                    boolean discountAvailable = modelCartProduct.isDiscountAvailable();

                    if (productPrice.isEmpty() || productPrice.equals("null")) {
                        productPrice = "0";
                    }
                    if (productDiscountPrice.isEmpty() || productDiscountPrice.equals("null")) {
                        productDiscountPrice = "0";
                    }
                    double priceToAddInSubTotal = 0.0;
                    if (discountAvailable) {
                        double originalPrice = Double.parseDouble(productPrice);
                        double discountedPrice = Double.parseDouble(productDiscountPrice);
                        priceToAddInSubTotal = (originalPrice - discountedPrice) * quantity;
                    } else {
                        priceToAddInSubTotal = Double.parseDouble(productPrice) * quantity;
                    }

                    allSubtotal = allSubtotal + priceToAddInSubTotal;
                    subTotalShop = subTotalShop + priceToAddInSubTotal;
                }
            }

            String storeDeliveryFee = "" + modelCart.getDeliveryFee();
            if (storeDeliveryFee.equals("") || storeDeliveryFee.equals("null")) {
                storeDeliveryFee = "";
            }
            allDeliveryCharges = allDeliveryCharges + Double.parseDouble(storeDeliveryFee);

            cartArrayList.get(i).setSubTotal(subTotalShop);
            cartArrayList.get(i).setTotal(subTotalShop + cartArrayList.get(i).getDeliveryFee());
        }

        allTotal = allSubtotal + allDeliveryCharges;

        binding.subtotalTv.setText("" + MyUtils.roundedDecimalValue(allSubtotal));
        binding.deliveryChargesTv.setText("" + MyUtils.roundedDecimalValue(allDeliveryCharges));
        binding.totalChargesTv.setText("" + MyUtils.roundedDecimalValue(allTotal));

        binding.placeOrderBtn.setEnabled(true);
    }

    private void placeOrder() {
        progressDialog.show();

        Log.d(TAG, "placeOrder: CartSize: " + cartArrayList.size());
        for (ModelCart modelCart : cartArrayList) {
            new Handler().postDelayed(() -> {

                long timestamp = MyUtils.getTimestamp();
                ArrayList<ModelCartProduct> cartProductArrayList = modelCart.getCartProductArrayList();
                //Orders > OrderID > SellerUid

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("orderId", "" + timestamp);
                hashMap.put("timestamp", timestamp);
                hashMap.put("latitude", "" + myLatitude);
                hashMap.put("longitude", "" + myLongitude);
                hashMap.put("orderBy", "" + myUid);
                hashMap.put("orderTo", "" + modelCart.getSellerUid());
                hashMap.put("orderStatus", "" + Constants.ORDER_STATUS_PLACED);
                hashMap.put("deliveryCharges", modelCart.getDeliveryFee());
                hashMap.put("subTotal", modelCart.getSubTotal());
                hashMap.put("total", modelCart.getTotal());

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders");
                ref.child("" + timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "placeOrder: Order Placed: "+timestamp);

                            for (ModelCartProduct modelCartProduct : cartProductArrayList) {
                                String productId = modelCartProduct.getProductId();
                                String productPrice = modelCartProduct.getProductPrice();
                                String productDiscountPrice = modelCartProduct.getProductDiscountPrice();
                                boolean discountAvailable = modelCartProduct.isDiscountAvailable();

                                if (productPrice.isEmpty() || productPrice.equals("null")) {
                                    productPrice = "0";
                                }
                                if (productDiscountPrice.isEmpty() || productDiscountPrice.equals("null")) {
                                    productDiscountPrice = "0";
                                }

                                String priceForProduct = "";
                                double originalPrice = Double.parseDouble(productPrice);
                                if (discountAvailable) {
                                    double discountedPrice = Double.parseDouble(productDiscountPrice);
                                    double newAfterDiscountPrice = originalPrice - discountedPrice;

                                    priceForProduct = "" + newAfterDiscountPrice;
                                } else {

                                    priceForProduct = "" + originalPrice;
                                }

                                HashMap<String, Object> hashMapProduct = new HashMap<>();
                                hashMapProduct.put("productId", "" + productId);
                                hashMapProduct.put("sellerUid", "" + modelCartProduct.getSellerUid());
                                hashMapProduct.put("productQuantity", modelCartProduct.getProductQuantity());
                                hashMapProduct.put("productPrice", "" + priceForProduct);

                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Orders");
                                ref1.child("" + timestamp).child("Products").child(productId)
                                        .setValue(hashMapProduct)
                                        .addOnSuccessListener(unused1 -> {
                                            updateProductQuantity(modelCartProduct);
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e));

                            }

                            prepareNotification(modelCart, timestamp);

                            new Handler().postDelayed(this::clearCartAfterOrder, 3000);

                        })
                        .addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e));

            },500);
        }

    }

    private void clearCartAfterOrder() {
        Log.d(TAG, "clearCartAfterOrder: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + myUid).child("Cart")
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Cart cleared...!");
                        progressDialog.dismiss();
                        MyUtils.toast(mContext, "Order Placed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(mContext, "Order Placed");
                    }
                });
    }

    private void updateProductQuantity(ModelCartProduct modelCartProduct) {
        String productId = "" + modelCartProduct.getProductId();
        Log.d(TAG, "updateProductQuantity: Updating stock of " + productId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String productStock = "" + snapshot.child("productStock").getValue();
                            if (productStock.equals("") || productStock.equals("null")) {
                                productStock = "0";
                            }

                            String productQuantityCart = "" + modelCartProduct.getProductQuantity();
                            if (productQuantityCart.equals("") || productQuantityCart.equals("null")) {
                                productQuantityCart = "0";
                            }

                            long newStock = Long.parseLong(productStock) - Long.parseLong(productQuantityCart);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("productStock", newStock);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
                            ref.child(productId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(unused -> {
                                        //Stock updated
                                        Log.d(TAG, "onSuccess: Stock of " + productId + " updated");
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotification(ModelCart modelCart, long timestamp) {
        Log.d(TAG, "prepareNotification: timestamp: "+timestamp);
        //prepare json what to send, and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationDataJo = new JSONObject();
        JSONObject notificationNotificationJo = new JSONObject();

        try {
            //extra/custom data
            notificationDataJo.put("notificationType", "" + Constants.NOTIFICATION_TYPE_ORDER_NEW);
            notificationDataJo.put("senderUid", "" + firebaseAuth.getUid());
            notificationDataJo.put("sendToUid", "" + modelCart.getSellerUid());
            notificationDataJo.put("orderId", "" + timestamp);
            //title, description, sound
            notificationNotificationJo.put("title", "New Order"); //"title" is reserved name in FCM API so be careful while typing
            notificationNotificationJo.put("body", "You've a new Order from: " + myName); //"body" is reserved name in FCM API so be careful while typing
            notificationNotificationJo.put("sound", "default"); //"sound" is reserved name in FCM API so be careful while typing
            //combine all data in single JSON object
            notificationJo.put("to", "" + modelCart.getSellerFcmToken()); //"to" is reserved name in FCM API so be careful while typing
            notificationJo.put("notification", notificationNotificationJo); //"notification" is reserved name in FCM API so be careful while typing
            notificationJo.put("data", notificationDataJo);  //"data" is reserved name in FCM API so be careful while typing
        } catch (Exception e) {
            Log.e(TAG, "prepareNotification: ", e);
        }

        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //Prepare JSON Object Request to enqueue
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://fcm.googleapis.com/fcm/send",
                notificationJo,
                response -> {
                    //Notification sent
                    Log.d(TAG, "sendFcmNotification: " + response.toString());
                },
                error -> {
                    //Notification failed to send
                    Log.e(TAG, "sendFcmNotification: ", error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); //"Content-Type" is reserved name in Volley Networking API/Library
                headers.put("Authorization", "key=" + Constants.FCM_SERVER_KEY); //"Authorization" is reserved name in Volley Networking API/Library, value against it must be like "key=fcm_server_key_here"

                return headers;
            }
        };

        //enqueue the JSON Object Request
        Volley.newRequestQueue(mContext).add(jsonObjectRequest);

    }

}
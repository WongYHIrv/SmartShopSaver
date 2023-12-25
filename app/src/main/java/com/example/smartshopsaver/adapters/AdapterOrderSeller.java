package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.activities.OrderDetailsSellerActivity;
import com.example.smartshopsaver.databinding.RowOrderSellerBinding;
import com.example.smartshopsaver.filters.FilterOrderSeller;
import com.example.smartshopsaver.models.ModelOrder;
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

public class AdapterOrderSeller extends RecyclerView.Adapter<AdapterOrderSeller.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowOrderSellerBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;

    private FirebaseAuth firebaseAuth;
    //orderArrayList The list of the ORDERS
    public ArrayList<ModelOrder> orderArrayList;
    private ArrayList<ModelOrder> filterList;
    private FilterOrderSeller filter;

    private static final String TAG = "ORDER_USER_TAG";

    /**
     * Constructor*
     *
     * @param context        The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param orderArrayList The list of categories
     */
    public AdapterOrderSeller(Context context, ArrayList<ModelOrder> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
        this.filterList = orderArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_order_seller.xml
        binding = RowOrderSellerBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_order_user.xml and Handle clicks
        ModelOrder modelOrder = orderArrayList.get(position);

        String orderId = modelOrder.getOrderId();
        long timestamp = modelOrder.getTimestamp();
        String orderStatus = modelOrder.getOrderStatus();
        double total = modelOrder.getTotal();

        String formattedDate = MyUtils.formatTimestampDate(timestamp);

        loadBuyerInfo(modelOrder, holder);

        holder.orderIdTv.setText(orderId);
        holder.dateTv.setText(formattedDate);
        holder.orderStatusTv.setText(orderStatus);
        holder.priceTv.setText(MyUtils.roundedDecimalValue(total));

        //handle itemView click, start OrderDetailsUserActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
            intent.putExtra("orderId", modelOrder.getOrderId());
            context.startActivity(intent);
        });

        //handle optionsBtn click, show order status change dialog
        holder.optionsBtn.setOnClickListener(v -> optionsDialog(modelOrder, holder));

    }

    private void loadBuyerInfo(ModelOrder modelOrder, HolderBrochureSeller holder) {
        String buyerUid = "" + modelOrder.getOrderBy();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(buyerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String buyerName = "" + snapshot.child("name").getValue();
                        String fcmToken = "" + snapshot.child("fcmToken").getValue();
                        modelOrder.setBuyerName(buyerName);
                        modelOrder.setBuyerFcmToken(fcmToken);

                        holder.buyerNameTv.setText(buyerName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void optionsDialog(ModelOrder modelCategory, AdapterOrderSeller.HolderBrochureSeller holder) {
        String orderId = modelCategory.getOrderId();
        String orderStatus = modelCategory.getOrderStatus();

        //init popup menu param 1 is context and param 2 is the UI View (profileImagePickFab) to above/below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(context, holder.optionsBtn);
        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
        if (orderStatus.equals(Constants.ORDER_STATUS_PLACED)) {
            popupMenu.getMenu().add(Menu.NONE, 1, 1, "" + Constants.ORDER_STATUS_IN_PROGRESS);
            popupMenu.getMenu().add(Menu.NONE, 2, 2, "" + Constants.ORDER_STATUS_OUT_FOR_DELIVERY);
            popupMenu.getMenu().add(Menu.NONE, 3, 3, "" + Constants.ORDER_STATUS_DELIVERED);
            popupMenu.getMenu().add(Menu.NONE, 4, 4, "" + Constants.ORDER_STATUS_CANCELLED);
        } else if (orderStatus.equals(Constants.ORDER_STATUS_IN_PROGRESS)) {
            popupMenu.getMenu().add(Menu.NONE, 2, 2, "" + Constants.ORDER_STATUS_OUT_FOR_DELIVERY);
            popupMenu.getMenu().add(Menu.NONE, 3, 3, "" + Constants.ORDER_STATUS_DELIVERED);
            popupMenu.getMenu().add(Menu.NONE, 4, 4, "" + Constants.ORDER_STATUS_CANCELLED);
        } else if (orderStatus.equals(Constants.ORDER_STATUS_OUT_FOR_DELIVERY)) {
            popupMenu.getMenu().add(Menu.NONE, 3, 3, "" + Constants.ORDER_STATUS_DELIVERED);
            popupMenu.getMenu().add(Menu.NONE, 4, 4, "" + Constants.ORDER_STATUS_CANCELLED);
        }

        //Show Popup Menu
        popupMenu.show();
        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(item -> {
            //get the id of the menu item clicked
            String newOrderStatus = "" + item.getTitle();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Change Order Status")
                    .setMessage("Are you sure you want to change Order status from " + orderStatus + " to " + newOrderStatus + "?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("orderStatus", "" + newOrderStatus);

                        DatabaseReference refOrderStatus = FirebaseDatabase.getInstance().getReference("Orders");
                        refOrderStatus.child("" + orderId).updateChildren(hashMap)
                                .addOnSuccessListener(unused -> {
                                    MyUtils.toast(context, "Order status updated...!");
                                    prepareNotification(modelCategory, newOrderStatus);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        });
    }
    private void prepareNotification(ModelOrder modelOrder, String newOrderStatus) {
        Log.d(TAG, "prepareNotification: ");
        //prepare json what to send, and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationDataJo = new JSONObject();
        JSONObject notificationNotificationJo = new JSONObject();

        try {
            //extra/custom data
            notificationDataJo.put("notificationType", "" + Constants.NOTIFICATION_TYPE_ORDER_UPDATE);
            notificationDataJo.put("senderUid", "" + firebaseAuth.getUid());
            notificationDataJo.put("sendToUid", "" + modelOrder.getOrderBy());
            notificationDataJo.put("orderId", "" + modelOrder.getOrderId());
            notificationDataJo.put("orderStatus", "" + newOrderStatus);
            //title, description, sound
            notificationNotificationJo.put("title", "Order Update"); //"title" is reserved name in FCM API so be careful while typing
            notificationNotificationJo.put("body", "Your Order " + modelOrder.getOrderId() + " is Now " + newOrderStatus); //"body" is reserved name in FCM API so be careful while typing
            notificationNotificationJo.put("sound", "default"); //"sound" is reserved name in FCM API so be careful while typing
            //combine all data in single JSON object
            notificationJo.put("to", "" + modelOrder.getBuyerFcmToken()); //"to" is reserved name in FCM API so be careful while typing
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
        Volley.newRequestQueue(context).add(jsonObjectRequest);

    }

    @Override
    public int getItemCount() {
        //return the size of list
        return orderArrayList.size();
    }

    @Override
    public FilterOrderSeller getFilter() {
        if (filter == null) {
            filter = new FilterOrderSeller(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_order_seller.xml
        ImageButton optionsBtn;
        TextView orderIdTv, dateTv, buyerNameTv, orderStatusTv, priceTv;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_order_seller.xml
            optionsBtn = binding.optionsBtn;
            orderIdTv = binding.orderIdTv;
            dateTv = binding.dateTv;
            buyerNameTv = binding.buyerNameTv;
            orderStatusTv = binding.orderStatusTv;
            priceTv = binding.priceTv;
        }
    }
}

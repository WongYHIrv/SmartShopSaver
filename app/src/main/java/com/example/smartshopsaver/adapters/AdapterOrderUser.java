package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.activities.OrderDetailsUserActivity;
import com.example.smartshopsaver.activities.ReportOrderUserActivity;
import com.example.smartshopsaver.databinding.RowOrderUserBinding;
import com.example.smartshopsaver.filters.FilterOrderUser;
import com.example.smartshopsaver.models.ModelOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowOrderUserBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //orderArrayList The list of the ORDERS
    public ArrayList<ModelOrder> orderArrayList;
    private ArrayList<ModelOrder> filterList;
    private FilterOrderUser filter;

    private static final String TAG = "ORDER_USER_TAG";

    /**
     * Constructor*
     *
     * @param context        The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param orderArrayList The list of categories
     */
    public AdapterOrderUser(Context context, ArrayList<ModelOrder> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
        this.filterList = orderArrayList;
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_order_user.xml
        binding = RowOrderUserBinding.inflate(LayoutInflater.from(context), parent, false);

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

        loadSellerInfo(modelOrder, holder);

        holder.orderIdTv.setText(orderId);
        holder.dateTv.setText(formattedDate);
        holder.orderStatusTv.setText(orderStatus);
        holder.priceTv.setText(MyUtils.roundedDecimalValue(total));

        /*if (orderStatus.equals(Constants.ORDER_STATUS_PLACED)) {
            holder.orderStatusTv.setTextColor(Color.MAGENTA);
        } else if (orderStatus.equals(Constants.ORDER_STATUS_IN_PROGRESS)) {
            holder.orderStatusTv.setTextColor(Color.BLUE);
        } else if (orderStatus.equals(Constants.ORDER_STATUS_OUT_FOR_DELIVERY)) {
            holder.orderStatusTv.setTextColor(Color.YELLOW);
        } else if (orderStatus.equals(Constants.ORDER_STATUS_DELIVERED)) {
            holder.orderStatusTv.setTextColor(Color.GREEN);
        } else if (orderStatus.equals(Constants.ORDER_STATUS_CANCELLED)) {
            holder.orderStatusTv.setTextColor(Color.RED);
        }*/

        //handle itemView click, start OrderDetailsUserActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetailsUserActivity.class);
                intent.putExtra("orderId", modelOrder.getOrderId());
                context.startActivity(intent);
            }
        });



        //handle reportBtn click, show option to report product
        holder.reportBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportOrderUserActivity.class);
            intent.putExtra("orderId", modelOrder.getOrderId());
            intent.putExtra("sellerUid", modelOrder.getOrderTo());
            context.startActivity(intent);
        });

    }

    private void loadSellerInfo(ModelOrder modelOrder, HolderBrochureSeller holder) {
        String sellerUid = "" + modelOrder.getOrderTo();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        modelOrder.setSellerName(shopName);

                        holder.sellerNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        //return the size of list
        return orderArrayList.size();
    }

    @Override
    public FilterOrderUser getFilter() {
        if (filter == null) {
            filter = new FilterOrderUser(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_order_user.xml
        TextView orderIdTv, dateTv, sellerNameTv, orderStatusTv, priceTv;
        ImageButton reportBtn;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            orderIdTv = binding.orderIdTv;
            dateTv = binding.dateTv;
            sellerNameTv = binding.sellerNameTv;
            orderStatusTv = binding.orderStatusTv;
            priceTv = binding.priceTv;
            reportBtn = binding.reportBtn;
        }
    }
}

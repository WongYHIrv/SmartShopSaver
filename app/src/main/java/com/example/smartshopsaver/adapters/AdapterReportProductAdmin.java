package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.ReportProductDetailsAdminActivity;
import com.example.smartshopsaver.databinding.RowReportProductAdminBinding;
import com.example.smartshopsaver.filters.FilterReportProductAdmin;
import com.example.smartshopsaver.models.ModelReportProduct;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterReportProductAdmin extends RecyclerView.Adapter<AdapterReportProductAdmin.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowReportProductAdminBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //orderArrayList The list of the ORDERS
    public ArrayList<ModelReportProduct> reportProductArrayList;
    private ArrayList<ModelReportProduct> filterList;
    private FilterReportProductAdmin filter;

    private static final String TAG = "ORDER_USER_TAG";

    /**
     * Constructor*
     *
     * @param context                The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param reportProductArrayList The list of categories
     */
    public AdapterReportProductAdmin(Context context, ArrayList<ModelReportProduct> reportProductArrayList) {
        this.context = context;
        this.reportProductArrayList = reportProductArrayList;
        this.filterList = reportProductArrayList;
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_order_user.xml
        binding = RowReportProductAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_order_user.xml and Handle clicks
        ModelReportProduct modelReportProduct = reportProductArrayList.get(position);

        String reportId = modelReportProduct.getId();
        String details = modelReportProduct.getDetails();
        long timestamp = modelReportProduct.getTimestamp();

        String formattedDate = MyUtils.formatTimestampDate(timestamp);

        loadBuyerInfo(modelReportProduct, holder);
        loadSellerInfo(modelReportProduct, holder);
        loadProductInfo(modelReportProduct, holder);

        holder.dateTv.setText(formattedDate);
        holder.reportTv.setText(details);

        //handle itemView click, start ReportProductDetailsAdminActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportProductDetailsAdminActivity.class);
            intent.putExtra("reportId", modelReportProduct.getId());
            context.startActivity(intent);
        });

    }

    private void loadSellerInfo(ModelReportProduct modelOrder, HolderBrochureSeller holder) {
        String sellerUid = "" + modelOrder.getSellerUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        modelOrder.setSellerName(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo(ModelReportProduct modelOrder, HolderBrochureSeller holder) {
        String reporterUid = "" + modelOrder.getReportByUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(reporterUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String reporterName = "" + snapshot.child("name").getValue();
                        modelOrder.setReporterName(reporterName);

                        holder.reporterTv.setText(reporterName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductInfo(ModelReportProduct modelReportProduct, HolderBrochureSeller holder) {
        String productId = "" + modelReportProduct.getProductId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productName = "" + snapshot.child("productName").getValue();
                        String imageUrl = "" + snapshot.child("imageUrl").getValue();

                        modelReportProduct.setProductName(productName);
                        modelReportProduct.setProductImage(imageUrl);

                        holder.productNameTv.setText(productName);

                        try {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.cart_white)
                                    .into(holder.productImageIv);
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
        return reportProductArrayList.size();
    }

    @Override
    public FilterReportProductAdmin getFilter() {
        if (filter == null) {
            filter = new FilterReportProductAdmin(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_order_user.xml
        ShapeableImageView productImageIv;
        TextView productNameTv, reportTv, reporterTv, dateTv;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            productImageIv = binding.productImageIv;
            productNameTv = binding.productNameTv;
            reportTv = binding.reportTv;
            reporterTv = binding.reporterTv;
            dateTv = binding.dateTv;
        }
    }
}

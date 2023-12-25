package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.activities.ReportOrderDetailsAdminActivity;
import com.example.smartshopsaver.databinding.RowReportOrderAdminBinding;
import com.example.smartshopsaver.filters.FilterReportOrderAdmin;
import com.example.smartshopsaver.models.ModelReportOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterReportOrderAdmin extends RecyclerView.Adapter<AdapterReportOrderAdmin.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowReportOrderAdminBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //orderArrayList The list of the ORDERS
    public ArrayList<ModelReportOrder> reportOrderArrayList;
    private ArrayList<ModelReportOrder> filterList;
    private FilterReportOrderAdmin filter;

    private static final String TAG = "ORDER_USER_TAG";

    /**
     * Constructor*
     *
     * @param context                The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param reportOrderArrayList The list of categories
     */
    public AdapterReportOrderAdmin(Context context, ArrayList<ModelReportOrder> reportOrderArrayList) {
        this.context = context;
        this.reportOrderArrayList = reportOrderArrayList;
        this.filterList = reportOrderArrayList;
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_order_user.xml
        binding = RowReportOrderAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_order_user.xml and Handle clicks
        ModelReportOrder modelReportProduct = reportOrderArrayList.get(position);

        String reportId = modelReportProduct.getId();
        String details = modelReportProduct.getDetails();
        long timestamp = modelReportProduct.getTimestamp();

        String formattedDate = MyUtils.formatTimestampDate(timestamp);

        loadBuyerInfo(modelReportProduct, holder);
        loadSellerInfo(modelReportProduct, holder);

        holder.dateTv.setText(formattedDate);
        holder.reportTv.setText(details);

        //handle itemView click, start ReportOrderDetailsAdminActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportOrderDetailsAdminActivity.class);
            intent.putExtra("reportId", modelReportProduct.getId());
            context.startActivity(intent);
        });

    }

    private void loadSellerInfo(ModelReportOrder modelOrder, HolderBrochureSeller holder) {
        String sellerUid = "" + modelOrder.getSellerUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        modelOrder.setSellerName(shopName);

                        holder.sellerTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo(ModelReportOrder modelOrder, HolderBrochureSeller holder) {
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

    @Override
    public int getItemCount() {
        //return the size of list
        return reportOrderArrayList.size();
    }

    @Override
    public FilterReportOrderAdmin getFilter() {
        if (filter == null) {
            filter = new FilterReportOrderAdmin(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_order_user.xml
        TextView reportTv, reporterTv, sellerTv, dateTv;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_product_seller.xml
            reportTv = binding.reportTv;
            reporterTv = binding.reporterTv;
            sellerTv = binding.sellerTv;
            dateTv = binding.dateTv;
        }
    }
}

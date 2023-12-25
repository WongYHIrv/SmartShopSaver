package com.example.smartshopsaver.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartshopsaver.MyApp;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.ReceiptEditActivity;
import com.example.smartshopsaver.activities.ReceiptViewDetailUserActivity;
import com.example.smartshopsaver.databinding.RowReceiptUserBinding;
import com.example.smartshopsaver.filters.FilterReceipt;
import com.example.smartshopsaver.models.ModelReceipt;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterReceiptUser extends RecyclerView.Adapter<AdapterReceiptUser.HolderReceiptUser> {

    private Context context;

    public ArrayList<ModelReceipt> receiptArrayList;

    private RowReceiptUserBinding binding;

    private ProgressDialog progressDialog;

    private FilterReceipt filter;

    public AdapterReceiptUser(Context context, ArrayList<ModelReceipt> receiptArrayList) {
        this.context = context;
        this.receiptArrayList = receiptArrayList;

        //Initialize Progress Dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }


    @NonNull
    @Override
    public HolderReceiptUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowReceiptUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderReceiptUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterReceiptUser.HolderReceiptUser holder, int position) {
        ModelReceipt model = receiptArrayList.get(position);
        String id = model.getId();
        String uid = model.getUid();
        String title = model.getTitle();
        String description = model.getDescription();
        String location = model.getLocation();
        String url =  model.getUrl();
        String dateReceipt = model.getDateReceipt();
        double expensesAmt = model.getExpensesAmt();
        long timestamp = model.getTimestamp();

        //Picasso.get().load(url).into(holder.imageView);
        Picasso.get()
                .load(url)
                .fit().centerInside()
                .rotate(90)
                .error(R.drawable.delete_white)
                .into(binding.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // onSuccess trace command output
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.locationTv.setText(location);
        holder.expenseAmountTv.setText("RM "+expensesAmt);
        holder.dateTv.setText(dateReceipt);

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        //Handle item click, open ReceiptViewDetailUserActivity activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReceiptViewDetailUserActivity.class);
                intent.putExtra("receiptId", id);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelReceipt model, HolderReceiptUser holder) {
        String uid = model.getUid();
        String id = model.getId();
        String url = model.getUrl();
        String description = model.getDescription();

        //Options to show in the dialog, moreBtn on lick
        String[] options = {"Edit", "Delete"};

        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //Edit Button
                            Intent intent = new Intent(context, ReceiptEditActivity.class);
                            intent.putExtra("receiptId", id);
                            context.startActivity(intent);
                        }
                        else if (which == 1) {
                            //Delete button
                            MyApp.deleteReceipt(
                                    context,
                                    ""+id,
                                    ""+url,
                                    ""+description,
                                    ""+uid
                            );
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return receiptArrayList.size();
    }

    class HolderReceiptUser extends RecyclerView.ViewHolder {

        ImageButton moreBtn;

        TextView titleTv, descriptionTv, locationTv, expenseAmountTv, dateTv;

        ImageView imageView;

        public HolderReceiptUser(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            imageView = binding.imageView;
            locationTv = binding.locationTv;
            dateTv = binding.dateTv;
            expenseAmountTv = binding.expenseAmountTv;
            moreBtn = binding.moreBtn;


        }
    }

}

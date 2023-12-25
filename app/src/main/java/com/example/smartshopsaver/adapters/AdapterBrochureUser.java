package com.example.smartshopsaver.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.activities.BrochureDetailsActivity;
import com.example.smartshopsaver.databinding.RowBrochureUserBinding;
import com.example.smartshopsaver.filters.FilterBrochureUser;
import com.example.smartshopsaver.models.ModelBrochure;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class AdapterBrochureUser extends RecyclerView.Adapter<AdapterBrochureUser.HolderBrochureSeller> implements Filterable {

    //View Binding
    private RowBrochureUserBinding binding;
    //Context of activity/fragment from where instance of AdapterCategory class is created
    private Context context;
    //categoryArrayList The list of the categories
    public ArrayList<ModelBrochure> brochureArrayList;
    private ArrayList<ModelBrochure> filterList;
    private FilterBrochureUser filter;

    private static final String TAG = "BROCHURE_SELLER_TAG";

    /**
     * Constructor*
     *
     * @param context           The context of activity/fragment from where instance of AdapterCategory class is created *
     * @param brochureArrayList The list of categories
     */
    public AdapterBrochureUser(Context context, ArrayList<ModelBrochure> brochureArrayList) {
        this.context = context;
        this.brochureArrayList = brochureArrayList;
        this.filterList = brochureArrayList;
    }

    @NonNull
    @Override
    public HolderBrochureSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the row_brochure_user.xml
        binding = RowBrochureUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBrochureSeller(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBrochureSeller holder, int position) {
        //get data from particular position of list and set to the UI Views of row_category.xml and Handle clicks
        ModelBrochure modelCategory = brochureArrayList.get(position);

        //get data from modelCategory
        String brochureName = modelCategory.getBrochureName();
        String brochureDescription = modelCategory.getBrochureDescription();
        long timestamp = modelCategory.getTimestamp();

        String formattedTimestamp = MyUtils.formatTimestampDate(timestamp);

        //get random color to set as background color of the categoryIconIv
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));

        //set data to UI Views of row_category.xml
        holder.brochureNameTv.setText(brochureName);
        holder.brochureDescriptionTv.setText(brochureDescription);
        holder.brochureDateTv.setText(formattedTimestamp);
        //holder.categoryIconIv.setBackgroundColor(color);

        loadBrochureCategory(modelCategory, holder);
        loadPdfThumbnail(modelCategory, holder);

        //handle itemView click, start BrochureDetailsActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BrochureDetailsActivity.class);
                intent.putExtra("brochureId", modelCategory.getBrochureId());
                context.startActivity(intent);
            }
        });

    }

    private void loadBrochureCategory(ModelBrochure modelCategory, HolderBrochureSeller holder) {
        String brochureCategoryId = modelCategory.getBrochureCategoryId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrochureCategories");
        ref.child(brochureCategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String brochureCategory = "" + snapshot.child("brochureCategory").getValue();
                        modelCategory.setBrochureCategory(brochureCategory);

                        holder.brochureCategoryTv.setText(brochureCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPdfThumbnail(ModelBrochure modelCategory, HolderBrochureSeller holder) {
        holder.pdfThumbnailPd.setVisibility(View.VISIBLE);
        holder.pdfThumbnailIv.setImageResource(R.color.gray);

        String pdfUrl = modelCategory.getPdfUrl();
        Log.d(TAG, "loadPdfThumbnail: pdfUrl: " + pdfUrl);

        File rootFolder = new File(context.getExternalFilesDir(null), "PDF_FILES");
        // Create the storage directory if it does not exist
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }

        String fileName = modelCategory.getBrochureId() + ".pdf";
        File file = new File(rootFolder, fileName);
        modelCategory.setPdfFile(file);

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getFile(modelCategory.getPdfFile())
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "loadPdfThumbnail: onSuccess: ");
                    try {
                        ParcelFileDescriptor mFileDescriptor = ParcelFileDescriptor.open(modelCategory.getPdfFile(), ParcelFileDescriptor.MODE_READ_ONLY);
                        // This is the PdfRenderer we use to render the PDF.
                        PdfRenderer mPdfRenderer = new PdfRenderer(mFileDescriptor);
                        //page count
                        long pageCount = mPdfRenderer.getPageCount();
                        //check if pdf have no pages then we can't show pdf thumbnail
                        if (pageCount <= 0) {
                            //No pages in pdf, can't show thumbnail
                            Log.d(TAG, "loadThumbnailFromPdfFile: No pages in pdf");
                            holder.pdfThumbnailPd.setVisibility(View.GONE);
                        } else {
                            //There are page(s) in pdf, can show pdf thumbnail
                            //Use `openPage` to open a specific page in PDF.
                            PdfRenderer.Page mCurrentPage = mPdfRenderer.openPage(0);
                            // Important: the destination bitmap must be ARGB (not RGB).
                            Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(), Bitmap.Config.ARGB_8888);
                            // Here, we render the page onto the Bitmap.
                            // To render a portion of the page, use the second and third parameter. Pass nulls to get
                            // the default result.
                            // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
                            mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                            // We are ready to show the Bitmap to user.
                            //UI Thread work here
                            try {
                                Glide.with(context)
                                        .load(bitmap)
                                        .fitCenter()
                                        .placeholder(R.color.gray)
                                        .into(holder.pdfThumbnailIv);
                                holder.pdfThumbnailPd.setVisibility(View.GONE);
                            } catch (Exception e) {
                                Log.e(TAG, "loadThumbnailFromPdfFile: ", e);
                                holder.pdfThumbnailPd.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "loadPdfThumbnail: ", e);
                        holder.pdfThumbnailPd.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadPdfThumbnail: onFailure: ", e);
                    holder.pdfThumbnailPd.setVisibility(View.GONE);
                });

    }

    private void deleteBrochureCategory(ModelBrochure modelCategory, HolderBrochureSeller holder) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete " + modelCategory.getBrochureName() + " ?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String brochureId = "" + modelCategory.getBrochureId();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
                        ref.child(brochureId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        MyUtils.toast(context, "Deleted...!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        MyUtils.toast(context, "Failed to delete due to " + e.getMessage());
                                    }
                                });

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        //return the size of list
        return brochureArrayList.size();
    }

    @Override
    public FilterBrochureUser getFilter() {
        if (filter == null) {
            filter = new FilterBrochureUser(this, filterList);
        }
        return filter;
    }

    class HolderBrochureSeller extends RecyclerView.ViewHolder {

        //UI Views of the row_brochure_user.xml
        ShapeableImageView pdfThumbnailIv;
        TextView brochureNameTv, brochureDescriptionTv, brochureCategoryTv, brochureDateTv;
        ProgressBar pdfThumbnailPd;

        public HolderBrochureSeller(@NonNull View itemView) {
            super(itemView);

            //init UI Views of the row_brochure_seller.xml
            pdfThumbnailIv = binding.pdfThumbnailIv;
            brochureNameTv = binding.brochureNameTv;
            brochureDescriptionTv = binding.brochureDescriptionTv;
            brochureCategoryTv = binding.brochureCategoryTv;
            brochureDateTv = binding.brochureDateTv;
            pdfThumbnailPd = binding.pdfThumbnailPd;
        }
    }
}

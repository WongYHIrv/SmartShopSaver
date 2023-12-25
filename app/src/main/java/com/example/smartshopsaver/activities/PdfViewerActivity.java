package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityBrochureDetailsBinding;
import com.example.smartshopsaver.databinding.ActivityPdfViewerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity {

    //View Binding
    private ActivityPdfViewerBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "PDF_VIEWER_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private String brochureId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get brochure id to load details of it
        brochureId = getIntent().getStringExtra("brochureId");

        loadBrochureDetails();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadBrochureDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.child("" + brochureId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String brochureCategoryId = "" + snapshot.child("brochureCategoryId").getValue();
                        String brochureName = "" + snapshot.child("brochureName").getValue();
                        String brochureDescription = "" + snapshot.child("brochureDescription").getValue();
                        String pdfUrl = "" + snapshot.child("pdfUrl").getValue();
                        long timestamp = snapshot.child("timestamp").getValue(Long.class);

                        String formattedTimestamp = MyUtils.formatTimestampDate(timestamp);

                        File pdfRootFolder = new File(getExternalFilesDir(null), "PDF_FILES");
                        // Create the storage directory if it does not exist
                        if (!pdfRootFolder.exists()) {
                            pdfRootFolder.mkdirs();
                        }
                        String fileName = brochureId + ".pdf";
                        File pdfFile = new File(pdfRootFolder, fileName);

                        //Get Brochure File
                        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
                        ref.getFile(pdfFile)
                                .addOnSuccessListener(taskSnapshot -> {
                                    Log.d(TAG, "onDataChange: onSuccess: ");
                                    try {
                                        ParcelFileDescriptor mFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
                                        // This is the PdfRenderer we use to render the PDF.
                                        PdfRenderer mPdfRenderer = new PdfRenderer(mFileDescriptor);
                                        //page count
                                        long pageCount = mPdfRenderer.getPageCount();

                                        //check if pdf have no pages then we can't show pdf thumbnail
                                        if (pageCount <= 0) {
                                            //No pages in pdf, can't show thumbnail
                                            Log.d(TAG, "onDataChange: No pages in pdf");
                                            //binding.pdfThumbnailPb.setVisibility(View.GONE);
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
                                        }
                                        
                                    } catch (Exception e) {
                                        Log.e(TAG, "onDataChange: ", e);
                                        //binding.pdfThumbnailPb.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "onDataChange: onFailure: ", e);
                                    //binding.pdfThumbnailPb.setVisibility(View.GONE);
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
package com.example.smartshopsaver.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityBrochureDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class BrochureDetailsActivity extends AppCompatActivity {

    //View Binding
    private ActivityBrochureDetailsBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "BROCHURE_DETAILS_TAG";
    private static final String TAG_DOWNLOAD = "TAG_DOWNLOAD";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private String brochureId = "";
    private String brochureName = "";
    private String pdfUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityBrochureDetailsBinding.inflate(getLayoutInflater());
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
        //increment brochure view count, whenever this page starts
        MyUtils.incrementBrochureViewCount(brochureId);

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //handle readBrochureBtn click: Open PDF
        binding.readBrochureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pdfFile == null) {
                    MyUtils.toast(BrochureDetailsActivity.this, "File not ready yet! try again");
                } else {
                    Uri uriPdf = FileProvider.getUriForFile(BrochureDetailsActivity.this, "com.example.smartshopsaver.fileprovider", pdfFile);

                    Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setDataAndType(uriPdf, "application/pdf");
                    target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent intent = Intent.createChooser(target, "Open File");

                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        // Instruct the user to install a PDF reader here, or something
                        Toast.makeText(BrochureDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        //handle downloadBrochureBtn click, download
        binding.downloadBrochureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    MyUtils.downloadBrochure(BrochureDetailsActivity.this, "" + brochureId, "" + brochureName, "" + pdfUrl);
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

            }
        });

    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    MyUtils.downloadBrochure(this, "" + brochureId, "" + brochureName, "" + pdfUrl);
                } else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied...: ");
                    Toast.makeText(this, "Permission was denied...", Toast.LENGTH_SHORT).show();
                }
            });


    private File pdfFile = null;

    private void loadBrochureDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.child("" + brochureId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        brochureName = "" + snapshot.child("brochureName").getValue();
                        pdfUrl = "" + snapshot.child("pdfUrl").getValue();
                        String brochureCategoryId = "" + snapshot.child("brochureCategoryId").getValue();
                        String brochureDescription = "" + snapshot.child("brochureDescription").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        long timestamp = snapshot.child("timestamp").getValue(Long.class);

                        String formattedTimestamp = MyUtils.formatTimestampDate(timestamp);

                        File pdfRootFolder = new File(getExternalFilesDir(null), "PDF_FILES");
                        // Create the storage directory if it does not exist
                        if (!pdfRootFolder.exists()) {
                            pdfRootFolder.mkdirs();
                        }
                        String fileName = brochureId + ".pdf";
                        pdfFile = new File(pdfRootFolder, fileName);

                        binding.brochureNameTv.setText(brochureName);
                        binding.brochureDescriptionTv.setText(brochureDescription);
                        binding.brochureDateTv.setText(formattedTimestamp);
                        binding.brochureViewsTv.setText(viewsCount);
                        binding.brochureDownloadsTv.setText(downloadsCount);

                        //Get Brochure Category
                        DatabaseReference refBrochureCat = FirebaseDatabase.getInstance().getReference("BrochureCategories");
                        refBrochureCat.child(brochureCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String brochureCategory = "" + snapshot.child("brochureCategory").getValue();

                                        binding.brochureCategoryTv.setText(brochureCategory);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

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

                                        binding.brochurePagesTv.setText("" + pageCount);
                                        //check if pdf have no pages then we can't show pdf thumbnail
                                        if (pageCount <= 0) {
                                            //No pages in pdf, can't show thumbnail
                                            Log.d(TAG, "onDataChange: No pages in pdf");
                                            binding.pdfThumbnailPb.setVisibility(View.GONE);
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
                                                Glide.with(BrochureDetailsActivity.this)
                                                        .load(bitmap)
                                                        .fitCenter()
                                                        .placeholder(R.color.gray)
                                                        .into(binding.pdfThumbnailIv);
                                                binding.pdfThumbnailPb.setVisibility(View.GONE);
                                            } catch (Exception e) {
                                                Log.e(TAG, "onDataChange: ", e);
                                                binding.pdfThumbnailPb.setVisibility(View.GONE);
                                            }
                                        }

                                        try {
                                            double bytes = pdfFile.length();

                                            //convert bytes to KB, MB
                                            double kb = bytes / 1024;
                                            double mb = kb / 1024;

                                            if (mb >= 1) {
                                                binding.brochureSizeTv.setText(String.format("%.2f", mb) + " MB");
                                            } else if (kb >= 1) {
                                                binding.brochureSizeTv.setText(String.format("%.2f", kb) + " KB");
                                            } else {
                                                binding.brochureSizeTv.setText(String.format("%.2f", bytes) + " bytes");
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "onDataChange: ", e);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "onDataChange: ", e);
                                        binding.pdfThumbnailPb.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "onDataChange: onFailure: ", e);
                                    binding.pdfThumbnailPb.setVisibility(View.GONE);
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
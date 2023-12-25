package com.example.smartshopsaver.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.databinding.ActivityBrochureAddSellerBinding;
import com.example.smartshopsaver.models.ModelBrochureCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class BrochureAddSellerActivity extends AppCompatActivity {

    //View Binding
    private ActivityBrochureAddSellerBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "BROCHURE_ADD_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private boolean isEdit = false;
    private String brochureIdToEdit = "";

    private ArrayList<ModelBrochureCategory> brochureCategoryArrayList;
    private ArrayList<String> brochureCategoryTitlesArrayList;

    private Uri pdfUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityBrochureAddSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //To check if we're here to add new or update existing
        isEdit = getIntent().getBooleanExtra("isEdit", false);

        //Check if we're here to add new or update existing
        if (isEdit) {
            binding.toolbarTitleTv.setText("Edit Brochure");
            binding.saveBtn.setText("Update");

            //get brochure id to edit
            brochureIdToEdit = getIntent().getStringExtra("brochureId");

            loadBrochureDetails();
        } else {
            binding.toolbarTitleTv.setText("Add Brochure");
            binding.saveBtn.setText("Save");
        }

        loadBrochureCategories();

        binding.brochureCategoryAct.setOnClickListener(view -> {
            //Show Popup Menu
            popupMenuBrochureCategories.show();
            //handle popup menu item click
            popupMenuBrochureCategories.setOnMenuItemClickListener(item -> {
                //get the id of the menu item clicked
                int itemId = item.getItemId();
                String category = "" + item.getTitle();
                binding.brochureCategoryAct.setText(category);

                brochureCategoryId = brochureCategoryArrayList.get(itemId).getId();
                Log.d(TAG, "onItemClick: brochureCategoryId: " + brochureCategoryId);

                return true;
            });
        });

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //handle brochureFileAct click: pick PDF
        binding.brochureFileAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        //handle saveBtn click: validate data, save to firebase
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private PopupMenu popupMenuBrochureCategories;

    private void loadBrochureCategories() {
        //init list before starting adding data into it
        brochureCategoryArrayList = new ArrayList<>();
        brochureCategoryTitlesArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BrochureCategories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it, each time there is a change in list
                brochureCategoryArrayList.clear();
                brochureCategoryTitlesArrayList.clear();

                //init popup menu param 1 is context and param 2 is the UI View (brochureCategoryAct) to above/below we need to show popup menu
                popupMenuBrochureCategories = new PopupMenu(BrochureAddSellerActivity.this, binding.brochureCategoryAct);

                int menuItemId = 0;
                //load data into list from firebase db
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelBrochureCategory modelBrochureCategory = ds.getValue(ModelBrochureCategory.class);
                        brochureCategoryArrayList.add(modelBrochureCategory);
                        brochureCategoryTitlesArrayList.add(modelBrochureCategory.getBrochureCategory());

                        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
                        popupMenuBrochureCategories.getMenu().add(Menu.NONE, menuItemId, menuItemId, "" + modelBrochureCategory.getBrochureCategory());

                        menuItemId = menuItemId + 1;
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pdfActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pdfActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "pdfActivityResultLauncher: PDF Picked");

                        //Get uri of PDF picked
                        pdfUri = result.getData().getData();
                        Log.d(TAG, "pdfActivityResultLauncher: URI: " + pdfUri);

                        //Get name of pdf Picked
                        String fileName = MyUtils.fileNameFromUri(BrochureAddSellerActivity.this, pdfUri);

                        //Set name of pdf Picked
                        binding.brochureFileAct.setText(fileName);
                    } else {
                        Log.d(TAG, "pdfActivityResultLauncher: cancelled picking pdf");
                        MyUtils.toast(BrochureAddSellerActivity.this, "cancelled picking pdf");
                    }
                }
            });


    private String brochureName = "";
    private String brochureDescription = "";
    private String brochureCategory = "";
    private String brochureCategoryId = "";
    private String pdfTitle = "";
    private long timestamp = 0;

    private void validateData() {

        //input data
        timestamp = MyUtils.getTimestamp();
        brochureName = binding.brochureNameEt.getText().toString().trim();
        brochureDescription = binding.brochureDescriptionEt.getText().toString().trim();
        brochureCategory = binding.brochureCategoryAct.getText().toString().trim();
        pdfTitle = binding.brochureFileAct.getText().toString().trim();

        //validate data
        if (brochureName.isEmpty()) {
            binding.brochureNameEt.setError("Enter Brochure Name...!");
            binding.brochureNameEt.requestFocus();
        } else if (brochureDescription.isEmpty()) {
            binding.brochureDescriptionEt.setError("Enter Brochure Description...!");
            binding.brochureDescriptionEt.requestFocus();
        } else if (brochureCategory.isEmpty()) {
            binding.brochureCategoryAct.setError("Choose Brochure Category...!");
            binding.brochureCategoryAct.requestFocus();
        } else if (pdfTitle.isEmpty()) {
            binding.brochureFileAct.setError("Pick PDF File...!");
            binding.brochureFileAct.requestFocus();
        } else {
            if (pdfUri != null) {
                uploadPdfFile();
            } else {
                if (isEdit) {
                    updateBrochure("");
                } else {
                    saveBrochure("");
                }
            }
        }

    }

    private void uploadPdfFile() {
        Log.d(TAG, "uploadProfileImageStorage: ");
        //show progress
        progressDialog.setMessage("Uploading file...");
        progressDialog.show();
        //setup image name and path e.g. UserImages/profile_user_uid
        String filePathAndName = "BrochureFiles/" + "brochureFile_" + timestamp;
        //Storage reference to upload image
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(pdfUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Progress: " + progress);

                        progressDialog.setMessage("Uploading file. Progress: " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Image uploaded successfully, get url of uploaded image
                        Log.d(TAG, "onSuccess: Uploaded");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            if (isEdit) {
                                updateBrochure(uploadedImageUrl);
                            } else {
                                saveBrochure(uploadedImageUrl);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to upload image
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureAddSellerActivity.this, "Failed to upload due to " + e.getMessage());
                    }
                });
    }

    private void saveBrochure(String uploadedPdfUrl) {
        //show progress
        progressDialog.setMessage("Saving Brochure...!");
        progressDialog.show();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("brochureId", "" + timestamp);
        hashMap.put("brochureCategoryId", "" + brochureCategoryId);
        hashMap.put("brochureName", "" + brochureName);
        hashMap.put("brochureDescription", "" + brochureDescription);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("pdfUrl", "" + uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Added successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureAddSellerActivity.this, "Added Successfully...!");
                        pdfUri = null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureAddSellerActivity.this, "Failed to add due to " + e.getMessage());
                    }
                });
    }

    private void updateBrochure(String uploadedPdfUrl) {
        //show progress
        progressDialog.setMessage("Updating Brochure...!");
        progressDialog.show();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("brochureCategoryId", "" + brochureCategoryId);
        hashMap.put("brochureName", "" + brochureName);
        hashMap.put("brochureDescription", "" + brochureDescription);
        if (!uploadedPdfUrl.isEmpty()) {
            hashMap.put("pdfUrl", "" + uploadedPdfUrl);
        }

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.child("" + brochureIdToEdit)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Updated successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureAddSellerActivity.this, "Updated Successfully...!");
                        pdfUri = null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(BrochureAddSellerActivity.this, "Failed to update due to " + e.getMessage());
                    }
                });
    }


    private void loadBrochureDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.child("" + brochureIdToEdit)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        brochureCategoryId = "" + snapshot.child("brochureCategoryId").getValue();
                        String brochureName = "" + snapshot.child("brochureName").getValue();
                        String brochureDescription = "" + snapshot.child("brochureDescription").getValue();
                        String pdfUrl = "" + snapshot.child("pdfUrl").getValue();

                        binding.brochureNameEt.setText(brochureName);
                        binding.brochureDescriptionEt.setText(brochureDescription);

                        //Get brochure category from db
                        DatabaseReference refBrochureCat = FirebaseDatabase.getInstance().getReference("BrochureCategories");
                        refBrochureCat.child(brochureCategoryId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String brochureCategory = "" + snapshot.child("brochureCategory").getValue();

                                        binding.brochureCategoryAct.setText(brochureCategory);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        //Get brochure file name from storage
                        if (!pdfUrl.equals("") && !pdfUrl.equals("null")) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
                            storageReference.getMetadata()
                                    .addOnSuccessListener(storageMetadata -> {
                                        Log.d(TAG, "onSuccess: ");
                                        String fileName = "" + storageMetadata.getName();
                                        binding.brochureFileAct.setText(fileName);
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
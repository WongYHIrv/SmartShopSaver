package com.example.smartshopsaver.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityProductAddBinding;
import com.example.smartshopsaver.models.ModelProductCategory;
import com.example.smartshopsaver.models.ModelProductSubCategory;
import com.google.android.gms.common.moduleinstall.ModuleInstall;
import com.google.android.gms.common.moduleinstall.ModuleInstallClient;
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ProductAddActivity extends AppCompatActivity {

    //View Binding
    private ActivityProductAddBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "PRODUCT_ADD_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;
    private String myUid = "";

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    private GmsBarcodeScannerOptions gmsBarcodeScannerOptions;
    private GmsBarcodeScanner gmsBarcodeScanner;

    private ArrayList<ModelProductCategory> productCategoryArrayList;
    private ArrayList<String> productCategoryTitlesArrayList;

    private ArrayList<ModelProductSubCategory> productSubCategoryArrayList;
    private ArrayList<String> productSubCategoryTitlesArrayList;

    private boolean isEdit = false;
    private String productIdToEdit = "";

    //Uri of image picked/taken from gallery/camera
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init view binding
        binding = ActivityProductAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        ModuleInstallClient moduleInstallClient = ModuleInstall.getClient(this);
        ModuleInstallRequest moduleInstallRequest = ModuleInstallRequest.newBuilder()
                .addApi(GmsBarcodeScanning.getClient(this))
                .build();

        moduleInstallClient.installModules(moduleInstallRequest)
                .addOnSuccessListener(
                        response -> {
                            if (response.areModulesAlreadyInstalled()) {
                                // Modules are already installed when the request is sent.
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            // Handle failure...
                        });

        gmsBarcodeScannerOptions = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .enableAutoZoom() // available on 16.1.0 and higher
                .build();
        gmsBarcodeScanner = GmsBarcodeScanning.getClient(this, gmsBarcodeScannerOptions);

        //To check if we're here to add new or update existing
        isEdit = getIntent().getBooleanExtra("isEdit", false);

        //Check if we're here to add new or update existing
        if (isEdit) {
            binding.toolbarTitleTv.setText("Edit Product");
            binding.saveBtn.setText("Update");

            //get brochure id to edit
            productIdToEdit = getIntent().getStringExtra("productId");

            loadProductDetails();
        } else {
            binding.toolbarTitleTv.setText("Add Product");
            binding.saveBtn.setText("Save");
        }

        showHideDiscount();
        loadProductCategories();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        //handle barcodeScannerBtn click, open barcode scanner
        binding.barcodeScannerBtn.setOnClickListener(v -> openScanner());

        //handle productCategoryAct click, show product categories
        binding.productCategoryAct.setOnClickListener(v -> {
            //Show Popup Menu
            popupMenuCategories.show();
            //handle popup menu item click
            popupMenuCategories.setOnMenuItemClickListener(item -> {
                //get the id of the menu item clicked
                int itemId = item.getItemId();
                String category = "" + item.getTitle();
                binding.productCategoryAct.setText(category);

                binding.productSubCategoryAct.setText("");

                productCategoryId = productCategoryArrayList.get(itemId).getProductCategoryId();
                Log.d(TAG, "onItemClick: productCategoryId: " + productCategoryId);
                loadProductSubCategories();

                return true;
            });
        });

        //handle productSubCategoryAct click, show product sub categories
        binding.productSubCategoryAct.setOnClickListener(v -> {
            //Show Popup Menu
            popupMenuSubCategories.show();
            //handle popup menu item click
            popupMenuSubCategories.setOnMenuItemClickListener(item -> {
                //get the id of the menu item clicked
                int itemId = item.getItemId();
                String category = "" + item.getTitle();
                binding.productSubCategoryAct.setText(category);

                productSubCategoryId = productSubCategoryArrayList.get(itemId).getProductSubCategoryId();
                Log.d(TAG, "onItemClick: productSubCategoryId: " + productSubCategoryId);

                return true;
            });
        });

        //handle productExpireDateAct click: Show Date Picker Dialog
        binding.productExpireDateAct.setOnClickListener(v -> expireDatePickerDialog());

        //handle discountSwitch check change: Enable/Disable Discount
        binding.discountSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                binding.productDiscountNoteEt.setText("");
                binding.productDiscountPriceEt.setText("");
            }
            showHideDiscount();
        });

        //handle imagePickIv click, show image pick popup menu
        binding.imagePickIv.setOnClickListener(v -> imagePickDialog());

        //handle saveBtn click, validate data and save/update product
        binding.saveBtn.setOnClickListener(v -> validateData());

    }

    private void openScanner() {
        gmsBarcodeScanner
                .startScan()
                .addOnSuccessListener(barcode -> {
                    // Task completed successfully
                    Log.d(TAG, "openScanner: Scanned...!");
                    String barcodeText = barcode.getRawValue();
                    binding.productBarcodeEt.setText(barcodeText);
                })
                .addOnCanceledListener(() -> {
                    // Task canceled
                    Log.d(TAG, "openScanner: Cancelled...!");
                })
                .addOnFailureListener(e -> {
                    // Task failed with an exception
                    Log.e(TAG, "openScanner: ", e);
                });
    }

    private void loadProductDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child("" + productIdToEdit)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productName = "" + snapshot.child("productName").getValue();
                        String productDescription = "" + snapshot.child("productDescription").getValue();
                        String productBarcode = "" + snapshot.child("productBarcode").getValue();
                        productCategoryId = "" + snapshot.child("productCategoryId").getValue();
                        productSubCategoryId = "" + snapshot.child("productSubCategoryId").getValue();
                        String productPrice = "" + snapshot.child("productPrice").getValue();
                        String productStock = "" + snapshot.child("productStock").getValue();
                        String productExpireDate = "" + snapshot.child("productExpireDate").getValue();
                        String discountAvailable = "" + snapshot.child("discountAvailable").getValue();
                        String productDiscountPrice = "" + snapshot.child("productDiscountPrice").getValue();
                        String productDiscountNote = "" + snapshot.child("productDiscountNote").getValue();
                        String imageUrl = "" + snapshot.child("imageUrl").getValue();

                        binding.productNameEt.setText(productName);
                        binding.productDescriptionEt.setText(productDescription);
                        binding.productBarcodeEt.setText(productBarcode);
                        binding.productPriceEt.setText(productPrice);
                        binding.productStockEt.setText(productStock);
                        binding.productExpireDateAct.setText(productExpireDate);
                        binding.productDiscountPriceEt.setText(productDiscountPrice);
                        binding.productDiscountNoteEt.setText(productDiscountNote);

                        if (discountAvailable.equals("true")) {
                            binding.discountSwitch.setChecked(true);
                        } else {
                            binding.discountSwitch.setChecked(false);
                        }

                        try {
                            Glide.with(ProductAddActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.image_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }

                        DatabaseReference refCategory = FirebaseDatabase.getInstance().getReference("ProductCategories");
                        refCategory.child(productCategoryId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //TODO Edit Category ISSUE
                                        String productCategory = "" + snapshot.child("productCategory").getValue();
                                        binding.productCategoryAct.setText(productCategory);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        DatabaseReference refSubCategory = FirebaseDatabase.getInstance().getReference("ProductCategories");
                        refSubCategory.child(productCategoryId).child("ProductSubCategories").child(productSubCategoryId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //TODO Edit Sub Category ISSUE
                                        String productSubCategory = "" + snapshot.child("productSubCategory").getValue();
                                        binding.productSubCategoryAct.setText(productSubCategory);
                                        loadProductSubCategories();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private PopupMenu popupMenuCategories;

    private void loadProductCategories() {
        //init list before starting adding data into it
        productCategoryArrayList = new ArrayList<>();
        productCategoryTitlesArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it, each time there is a change in list
                productCategoryArrayList.clear();
                productCategoryTitlesArrayList.clear();

                //init popup menu param 1 is context and param 2 is the UI View (productCategoryAct) to above/below we need to show popup menu
                popupMenuCategories = new PopupMenu(ProductAddActivity.this, binding.productCategoryAct);

                int menuItemId = 0;
                //load data into list from firebase db
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        ModelProductCategory modelBrochureCategory = ds.getValue(ModelProductCategory.class);
                        productCategoryArrayList.add(modelBrochureCategory);
                        productCategoryTitlesArrayList.add(modelBrochureCategory.getProductCategory());

                        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
                        popupMenuCategories.getMenu().add(Menu.NONE, menuItemId, menuItemId, "" + modelBrochureCategory.getProductCategory());

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

    private PopupMenu popupMenuSubCategories;

    private void loadProductSubCategories() {
        //init list before starting adding data into it
        productSubCategoryArrayList = new ArrayList<>();
        productSubCategoryTitlesArrayList = new ArrayList<>();

        //DB path/reference to load/get data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId).child("ProductSubCategories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it, each time there is a change in list
                        productSubCategoryArrayList.clear();
                        productSubCategoryTitlesArrayList.clear();

                        //init popup menu param 1 is context and param 2 is the UI View (profileImagePickFab) to above/below we need to show popup menu
                        popupMenuSubCategories = new PopupMenu(ProductAddActivity.this, binding.productSubCategoryAct);

                        int menuItemId = 0;

                        //load data into list from firebase db
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                ModelProductSubCategory modelBrochureCategory = ds.getValue(ModelProductSubCategory.class);
                                productSubCategoryArrayList.add(modelBrochureCategory);
                                productSubCategoryTitlesArrayList.add(modelBrochureCategory.getProductSubCategory());

                                //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
                                popupMenuSubCategories.getMenu().add(Menu.NONE, menuItemId, menuItemId, "" + modelBrochureCategory.getProductSubCategory());

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

    private void showHideDiscount() {
        if (binding.discountSwitch.isChecked()) {
            binding.productDiscountPriceTil.setVisibility(View.VISIBLE);
            binding.productDiscountNoteTil.setVisibility(View.VISIBLE);
        } else {
            binding.productDiscountPriceTil.setVisibility(View.GONE);
            binding.productDiscountNoteTil.setVisibility(View.GONE);
        }
    }

    private void expireDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String expireDate = dayOfMonth + "/" + (month1 +1) + "/" + year1;
            Log.d(TAG, "onDateSet: Expire Date: " + expireDate);
            binding.productExpireDateAct.setText(expireDate);
        }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(MyUtils.getTimestamp());
        datePickerDialog.show();
    }

    private void imagePickDialog() {
        //init popup menu param 1 is context and param 2 is the UI View (profileImagePickFab) to above/below we need to show popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.imagePickIv);
        //add menu items to our popup menu Param#1 is GroupID, Param#2 is ItemID, Param#3 is OrderID, Param#4 is Menu Item Title
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        //Show Popup Menu
        popupMenu.show();
        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get the id of the menu item clicked
                int itemId = item.getItemId();
                if (itemId == 1) {
                    //Camera is clicked we need to check if we have permission of Camera, Storage before launching Camera to Capture image
                    Log.d(TAG, "onMenuItemClick: Camera Clicked, check if camera permission(s) granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is TIRAMISU or above. We only need Camera permission
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        //Device version is below TIRAMISU. We need Camera & Storage permissions
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (itemId == 2) {
                    //Gallery is clicked we need to check if we have permission of Storage before launching Gallery to Pick image
                    Log.d(TAG, "onMenuItemClick: Check if storage permission is granted or not");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //Device version is TIRAMISU or above. We don't need Storage permission to launch Gallery
                        pickImageGallery();
                    } else {
                        //Device version is below TIRAMISU. We need Storage permission to launch Gallery
                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return false;
            }
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Log.d(TAG, "onActivityResult: " + result.toString());
                //let's check if permissions are granted or not
                boolean areAllGranted = true;
                for (Boolean isGranted : result.values()) {

                    areAllGranted = areAllGranted && isGranted;
                }

                if (areAllGranted) {
                    //All Permissions Camera, Storage are granted, we can now launch camera to capture image
                    Log.d(TAG, "onActivityResult: All Granted e.g. Camera, Storage");
                    pickImageCamera();
                } else {
                    //Camera or Storage or Both permissions are denied, Can't launch camera to capture image
                    Log.d(TAG, "onActivityResult: All or either one is denied");
                    MyUtils.toast(ProductAddActivity.this, "Camera or Storage or both permissions denied...");
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                Log.d(TAG, "onActivityResult: isGranted: " + isGranted);
                //let's check if permission is granted or not
                if (isGranted) {
                    //Storage Permission granted, we can now launch gallery to pick image
                    pickImageGallery();
                } else {
                    //Storage Permission denied, we can't launch gallery to pick image
                    MyUtils.toast(ProductAddActivity.this, "Storage permission denied...!");
                }
            }
    );

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");
        //Setup Content values, MediaStore to capture high quality image using camera intent
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");
        //store the camera image in imageUri variable
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //Intent to launch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //Check if image is captured or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image Captured, we have image in imageUri as assigned in pickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Captured: " + imageUri);

                        //set to profileIv
                        try {
                            Glide.with(ProductAddActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //Cancelled
                        MyUtils.toast(ProductAddActivity.this, "Cancelled...");
                    }
                }
            }
    );

    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");
        //Intent to launch Image Picker e.g. Gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        //We only want to pick images
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //Check if image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //get data
                        Intent data = result.getData();
                        //get uri of image picked
                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: " + imageUri);
                        //set to profileIv
                        try {
                            Glide.with(ProductAddActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //Cancelled
                        MyUtils.toast(ProductAddActivity.this, "Cancelled...");
                    }
                }
            }
    );

    private long timestamp = 0;
    private String productName = "";
    private String productDescription = "";
    private String productBarcode = "";
    private String productCategory = "";
    private String productSubCategory = "";
    private String productPrice = "";
    private String productStock = "";
    private String productExpireDate = "";
    private boolean discountAvailable = false;
    private String productDiscountPrice = "";
    private String productDiscountNote = "";
    private String productCategoryId = "";
    private String productSubCategoryId = "";

    private void validateData() {

        //input data
        timestamp = MyUtils.getTimestamp();
        productName = binding.productNameEt.getText().toString().trim();
        productDescription = binding.productDescriptionEt.getText().toString().trim();
        productBarcode = binding.productBarcodeEt.getText().toString().trim();
        productCategory = binding.productCategoryAct.getText().toString().trim();
        productSubCategory = binding.productSubCategoryAct.getText().toString().trim();
        productPrice = binding.productPriceEt.getText().toString().trim();
        productStock = binding.productStockEt.getText().toString().trim();
        productExpireDate = binding.productExpireDateAct.getText().toString().trim();
        discountAvailable = binding.discountSwitch.isChecked();
        productDiscountPrice = binding.productDiscountPriceEt.getText().toString().trim();
        productDiscountNote = binding.productDiscountNoteEt.getText().toString().trim();

        //validate data
        if (productName.isEmpty()) {
            binding.productNameEt.setError("Enter Product Name...!");
            binding.productNameEt.requestFocus();
        } else if (productDescription.isEmpty()) {
            binding.productDescriptionEt.setError("Enter Product Description...!");
            binding.productDescriptionEt.requestFocus();
        } else if (productBarcode.isEmpty()) {
            binding.productBarcodeEt.setError("Enter Product Barcode..!");
            binding.productBarcodeEt.requestFocus();
        } else if (productCategory.isEmpty()) {
            binding.productCategoryAct.setError("Choose Product Category...!");
            binding.productCategoryAct.requestFocus();
        } else if (productSubCategory.isEmpty()) {
            binding.productSubCategoryAct.setError("Choose Product Sub Category...!");
            binding.productSubCategoryAct.requestFocus();
        } else if (productPrice.isEmpty()) {
            binding.productPriceEt.setError("Enter Product Price...!");
            binding.productPriceEt.requestFocus();
        } else if (productStock.isEmpty()) {
            binding.productStockEt.setError("Enter Product Stock...!");
            binding.productStockEt.requestFocus();
        } else if (productExpireDate.isEmpty()) {
            binding.productExpireDateAct.setError("Choose Product Expire Date...!");
            binding.productExpireDateAct.requestFocus();
        } else if (discountAvailable) {
            if (productDiscountPrice.isEmpty()) {
                binding.productDiscountPriceEt.setError("Enter Discount Price...!");
                binding.productDiscountPriceEt.requestFocus();
            } else if (Double.parseDouble(productDiscountPrice) >= Double.parseDouble(productPrice)) {
                binding.productDiscountPriceEt.setError("Discount price must be less than original price...!");
                binding.productDiscountPriceEt.requestFocus();
            } else if (productDiscountNote.isEmpty()) {
                binding.productDiscountNoteEt.setError("Enter Discount Note...!");
                binding.productDiscountNoteEt.requestFocus();
            } else {
                if (imageUri != null) {
                    uploadImageFirebaseStorage();
                } else {
                    if (isEdit) {
                        updateProduct("");
                    } else {
                        saveProduct("");
                    }
                }
            }
        } else {
            if (imageUri != null) {
                uploadImageFirebaseStorage();
            } else {
                if (isEdit) {
                    updateProduct("");
                } else {
                    saveProduct("");
                }
            }
        }

    }

    private void uploadImageFirebaseStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ");
        //show progress
        progressDialog.setMessage("Uploading product...");
        progressDialog.show();
        //setup image name and path e.g. UserImages/profile_user_uid
        String filePathAndName = "ProductFiles/" + "brochureFile_" + timestamp;
        //Storage reference to upload image
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(imageUri)
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
                                updateProduct(uploadedImageUrl);
                            } else {
                                saveProduct(uploadedImageUrl);
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
                        MyUtils.toast(ProductAddActivity.this, "Failed to upload due to " + e.getMessage());
                    }
                });
    }

    private void saveProduct(String uploadedImageUrl) {
        //show progress
        progressDialog.setMessage("Saving Product...!");
        progressDialog.show();

        if (productDiscountPrice.trim().isEmpty()) {
            productDiscountPrice = "0";
        }

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productId", "" + timestamp);
        hashMap.put("productName", "" + productName);
        hashMap.put("productDescription", "" + productDescription);
        hashMap.put("productBarcode", "" + productBarcode);
        hashMap.put("productCategoryId", "" + productCategoryId);
        hashMap.put("productSubCategoryId", "" + productSubCategoryId);
        hashMap.put("productExpireDate", "" + productExpireDate);
        hashMap.put("productStock", Long.parseLong(productStock));
        hashMap.put("productPrice", Double.parseDouble(productPrice));
        hashMap.put("discountAvailable", discountAvailable);
        hashMap.put("productDiscountPrice", Double.parseDouble(productDiscountPrice));
        hashMap.put("productDiscountNote", "" + productDiscountNote);
        hashMap.put("imageUrl", "" + uploadedImageUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + myUid);

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Added successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(ProductAddActivity.this, "Added Successfully...!");

                        imageUri = null;

                        updateProductPriceHistory("" + timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProductAddActivity.this, "Failed to add due to " + e.getMessage());
                    }
                });
    }

    private void updateProduct(String uploadedPdfUrl) {
        //show progress
        progressDialog.setMessage("Updating Product...!");
        progressDialog.show();

        if (productDiscountPrice.trim().isEmpty()) {
            productDiscountPrice = "0";
        }

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("productName", "" + productName);
        hashMap.put("productDescription", "" + productDescription);
        hashMap.put("productBarcode", "" + productBarcode);
        hashMap.put("productCategoryId", "" + productCategoryId);
        hashMap.put("productSubCategoryId", "" + productSubCategoryId);
        hashMap.put("productExpireDate", "" + productExpireDate);
        hashMap.put("productStock", Long.parseLong(productStock));
        hashMap.put("productPrice", Double.parseDouble(productPrice));
        hashMap.put("discountAvailable", discountAvailable);
        hashMap.put("productDiscountPrice", Double.parseDouble(productDiscountPrice));
        hashMap.put("productDiscountNote", productDiscountNote);
        if (!uploadedPdfUrl.isEmpty()) {
            hashMap.put("imageUrl", "" + uploadedPdfUrl);
        }

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child("" + productIdToEdit)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Updated successfully...!");
                        progressDialog.dismiss();
                        MyUtils.toast(ProductAddActivity.this, "Updated Successfully...!");

                        imageUri = null;

                        updateProductPriceHistory(productIdToEdit);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProductAddActivity.this, "Failed to update due to " + e.getMessage());
                    }
                });
    }

    private void updateProductPriceHistory(String productId) {
        long productHistoryTimestamp = MyUtils.getTimestamp();

        HashMap<String, Object> hashMapPriceHistory = new HashMap<>();
        hashMapPriceHistory.put("productPrice", Double.parseDouble(productPrice));
        hashMapPriceHistory.put("productHistoryTimestamp", productHistoryTimestamp);

        DatabaseReference refCheck = FirebaseDatabase.getInstance().getReference("Products");
        refCheck.child("" + productId).child("PriceHistory").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds: snapshot.getChildren()){
                                String productPriceHistory = "" + ds.child("productPrice").getValue();
                                if (productPriceHistory.equals("") || productPriceHistory.equals("null")){
                                    productPriceHistory = "0";
                                }

                                //Price is not same as last updated, add
                                if (!MyUtils.roundedDecimalValue(Double.parseDouble(productPriceHistory)).equals(MyUtils.roundedDecimalValue(Double.parseDouble(productPrice)))){
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
                                    ref.child("" + productId).child("PriceHistory")
                                            .child("" + productHistoryTimestamp)
                                            .setValue(hashMapPriceHistory);
                                }

                            }
                        } else {
                            //No price history, add
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
                            ref.child("" + productId).child("PriceHistory")
                                    .child("" + productHistoryTimestamp)
                                    .setValue(hashMapPriceHistory);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        /*HashMap<String, Object> hashMapPriceHistory = new HashMap<>();
        hashMapPriceHistory.put("productPrice", Double.parseDouble(productPrice));
        hashMapPriceHistory.put("productHistoryTimestamp", productHistoryTimestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Products");
        ref.child("" + productId).child("PriceHistory")
                .child("" + productHistoryTimestamp)
                .setValue(hashMapPriceHistory);*/
    }

}
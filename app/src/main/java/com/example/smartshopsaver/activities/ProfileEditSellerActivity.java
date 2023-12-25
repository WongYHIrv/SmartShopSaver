package com.example.smartshopsaver.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityProfileEditSellerBinding;
import com.example.smartshopsaver.databinding.ActivityRegisterSellerBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileEditSellerActivity extends AppCompatActivity {

    //View Binding
    private ActivityProfileEditSellerBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "PROFILE_EDIT_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    //ProgressDialog to show while performing some task
    private ProgressDialog progressDialog;

    //To get realtime location updates
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //Uri of image picked/taken from gallery/camera
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityProfileEditSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        loadMyInfo();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //handle toolbarGpsBtn click: check permission and get location
        binding.toolbarGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGPSEnabled()) {
                    String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                    requestLocationPermission.launch(permissions);
                } else {
                    MyUtils.toast(ProfileEditSellerActivity.this, "Turn on Location...!");
                }
            }
        });

        //handle imagePickIv click, show image pick popup menu
        binding.imagePickIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        //handle registerBtn click, start user registration
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    /**
     * Check if GPS/Location is enabled or not
     */
    private boolean isGPSEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "isGPSEnabled: ", e);
        }
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "isGPSEnabled: ", e);
        }
        return !(!gpsEnabled && !networkEnabled);
    }

    private ActivityResultLauncher<String[]> requestLocationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: ");
                    Log.d(TAG, "onActivityResult: " + result.toString());
                    //let's check if Camera or Storage or both permissions granted or not from permission dialog
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        Log.d(TAG, "onActivityResult: isGranted: " + isGranted);
                        areAllGranted = areAllGranted && isGranted;
                    }


                    if (areAllGranted) {
                        //Camera & Storage both permissions are granted, we can launch camera to take image
                        Log.d(TAG, "onActivityResult: All Granted e.g. Camera & Storage...");
                        getLocationUpdates();
                    } else {
                        //Camera or Storage or both permissions denied
                        Log.d(TAG, "onActivityResult: Camera or Storage or both denied...");
                        MyUtils.toast(ProfileEditSellerActivity.this, "Camera or Storage or both permissions denied...");
                    }
                }
            }
    );

    @SuppressLint("MissingPermission")
    private void getLocationUpdates() {
        // Request location updates
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

            if (locationAvailability.isLocationAvailable()) {
                MyUtils.toast(ProfileEditSellerActivity.this, "Location Available!");
            } else {
                MyUtils.toast(ProfileEditSellerActivity.this, "Location Not Available!");
            }
        }

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            // Do something with the location here
            for (Location location : locationResult.getLocations()) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                addressFromLatLng(latitude, longitude);
            }
        }
    };

    private void addressFromLatLng(double latitude, double longitude) {
        Log.d(TAG, "addressFromLatLng: ");

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            Address addressObj = addresses.get(0);
            String addressLine = addressObj.getAddressLine(0); //Complete Address
            String countryName = addressObj.getCountryName(); //Country e.g. Pakistan
            String adminArea = addressObj.getAdminArea(); //State e.g. Punjab
            String subAdminArea = addressObj.getSubAdminArea(); //City e.g. Lahore
            String locality = addressObj.getLocality(); //City e.g. Lahore
            //String subLocality = addressObj.getSubLocality(); //Neighbourhood e.g. Farooq Colony
            //String postalCode = addressObj.getPostalCode(); //Postal Code e.g. 54000

            Log.d(TAG, "addressFromLatLng: addressLine: " + addressLine);
            Log.d(TAG, "addressFromLatLng: countryName: " + countryName);
            Log.d(TAG, "addressFromLatLng: adminArea: " + adminArea);
            Log.d(TAG, "addressFromLatLng: subAdminArea: " + subAdminArea);
            Log.d(TAG, "addressFromLatLng: locality: " + locality);

            country = countryName;
            state = adminArea;
            if (subAdminArea != null && !subAdminArea.equals("null")) {
                city = "" + subAdminArea;
            } else {
                city = "" + locality;
            }
            address = "" + addressLine;

            binding.countryEt.setText(countryName);
            binding.stateEt.setText(state);
            binding.cityEt.setText(city.replace("null", ""));
            binding.addressEt.setText(address);

            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } catch (Exception e) {
            Log.e(TAG, "addressFromLatLng: ", e);
        }

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
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
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
                        MyUtils.toast(ProfileEditSellerActivity.this, "Camera or Storage or both permissions denied...");
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: " + isGranted);
                    //let's check if permission is granted or not
                    if (isGranted) {
                        //Storage Permission granted, we can now launch gallery to pick image
                        pickImageGallery();
                    } else {
                        //Storage Permission denied, we can't launch gallery to pick image
                        MyUtils.toast(ProfileEditSellerActivity.this, "Storage permission denied...!");
                    }
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
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //Check if image is captured or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image Captured, we have image in imageUri as assigned in pickImageCamera()
                        Log.d(TAG, "onActivityResult: Image Captured: " + imageUri);

                        //set to profileIv
                        try {
                            Glide.with(ProfileEditSellerActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //Cancelled
                        MyUtils.toast(ProfileEditSellerActivity.this, "Cancelled...");
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
            new ActivityResultCallback<ActivityResult>() {
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
                            Glide.with(ProfileEditSellerActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.person_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    } else {
                        //Cancelled
                        MyUtils.toast(ProfileEditSellerActivity.this, "Cancelled...");
                    }
                }
            }
    );

    private String fullName, shopName, shopDescription, deliveryFee, phoneNumber, country, state, city, address;
    private double latitude = 0.0, longitude = 0.0;

    private void validateData() {

        //input data
        fullName = binding.nameEt.getText().toString().trim();
        shopName = binding.shopNameEt.getText().toString().trim();
        deliveryFee = binding.deliveryFeeEt.getText().toString().trim();
        shopDescription = binding.shopDescriptionEt.getText().toString().trim();
        phoneNumber = binding.phoneEt.getText().toString().trim();
        country = binding.countryEt.getText().toString().trim();
        state = binding.stateEt.getText().toString().trim();
        city = binding.cityEt.getText().toString().trim();
        address = binding.addressEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(fullName)) {
            binding.nameEt.setError("Enter Name...!");
            binding.nameEt.requestFocus();
        } else if (TextUtils.isEmpty(shopName)) {
            binding.shopNameEt.setError("Enter Shop Name...!");
            binding.shopNameEt.requestFocus();
        } else if (TextUtils.isEmpty(shopDescription)) {
            binding.shopDescriptionEt.setError("Enter Shop Description...!");
            binding.shopDescriptionEt.requestFocus();
        } else if (TextUtils.isEmpty(deliveryFee)) {
            binding.deliveryFeeEt.setError("Enter Delivery Fee...!");
            binding.deliveryFeeEt.requestFocus();
        } else if (TextUtils.isEmpty(phoneNumber)) {
            binding.phoneEt.setError("Enter Phone Number...!");
            binding.phoneEt.requestFocus();
        } else if (latitude == 0.0 || longitude == 0.0) {
            MyUtils.toast(this, "Please click GPS button to detect location...");
        } else {
            if (imageUri != null) {
                uploadProfileImageStorage();
            } else {
                updateUserInfo("");
            }
        }

    }

    private void uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ");
        //show progress
        progressDialog.setMessage("Uploading seller profile image...");
        progressDialog.show();
        //setup image name and path e.g. UserImages/profile_user_uid
        String filePathAndName = "UserImages/" + "profile_" + firebaseAuth.getUid();
        //Storage reference to upload image
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Progress: " + progress);

                        progressDialog.setMessage("Uploading profile image. Progress: " + (int) progress + "%");
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
                            updateUserInfo(uploadedImageUrl);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to upload image
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditSellerActivity.this, "Failed to upload profile image due to " + e.getMessage());
                    }
                });
    }

    private void updateUserInfo(String uploadedImageUrl) {
        progressDialog.setMessage("Updating Seller Info...!");
        progressDialog.show();

        String uid = "" + firebaseAuth.getUid();

        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + fullName);
        hashMap.put("shopName", "" + shopName);
        hashMap.put("shopDescription", "" + shopDescription);
        hashMap.put("deliveryFee", "" + deliveryFee);
        hashMap.put("phone", "" + phoneNumber);
        hashMap.put("country", "" + country);
        hashMap.put("state", "" + state);
        hashMap.put("city", "" + city);
        hashMap.put("address", "" + address);
        hashMap.put("latitude", "" + latitude);
        hashMap.put("longitude", "" + longitude);
        if (!uploadedImageUrl.isEmpty()) {
            hashMap.put("profileImage", "" + uploadedImageUrl);
        }

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + uid).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //db updated
                        progressDialog.dismiss();
                        imageUri = null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating db
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditSellerActivity.this, "Failed to update due to " + e.getMessage());
                    }
                });
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data from db
                        String accountType = "" + snapshot.child("accountType").getValue();
                        String address = "" + snapshot.child("address").getValue();
                        String city = "" + snapshot.child("city").getValue();
                        String country = "" + snapshot.child("country").getValue();
                        String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String sLatitude = "" + snapshot.child("latitude").getValue();
                        String sLongitude = "" + snapshot.child("longitude").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String online = "" + snapshot.child("online").getValue();
                        String phone = "" + snapshot.child("phone").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String shopName = "" + snapshot.child("shopName").getValue();
                        String shopDescription = "" + snapshot.child("shopDescription").getValue();
                        String state = "" + snapshot.child("state").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        try {
                            latitude = Double.parseDouble(sLatitude);
                        } catch (Exception e) {
                            latitude = 0.0;
                            Log.e(TAG, "onDataChange: ", e);
                        }
                        try {
                            longitude = Double.parseDouble(sLongitude);
                        } catch (Exception e) {
                            longitude = 0.0;
                            Log.e(TAG, "onDataChange: ", e);
                        }

                        //set data to our form UI
                        binding.nameEt.setText(name);
                        binding.emailEt.setText(email);
                        binding.shopNameEt.setText(shopName);
                        binding.shopDescriptionEt.setText(shopDescription);
                        binding.deliveryFeeEt.setText(deliveryFee);
                        binding.phoneEt.setText(phone);
                        binding.countryEt.setText(country);
                        binding.stateEt.setText(state);
                        binding.cityEt.setText(city);
                        binding.addressEt.setText(address);

                        try {
                            Glide.with(ProfileEditSellerActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.person_white)
                                    .into(binding.profileIv);
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
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }
}
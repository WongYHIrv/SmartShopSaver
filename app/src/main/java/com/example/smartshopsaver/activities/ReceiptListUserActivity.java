package com.example.smartshopsaver.activities;

import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ReceiptListUserActivity extends AppCompatActivity {

    //UI View
    private Button inputImageBtn;

    private ImageButton backBtn;
    private Button recognizeTextBtn;

    private Button submitBtn;
    private ShapeableImageView imageIv;
    private EditText recognizedTextEt, receiptNameEt, receiptAmountEt, receiptLocationEt;

    private TextView dateTextView;
    private Calendar calendar;

    long dateGlobal;

    String timeInMilisGlobal,timeInMilisGlobal2;

    //TAGS
    private static final String TAG = "MAIN_TAG";

    private Uri imageUri = null;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //Progress Dialog
    private ProgressDialog progressDialog;

    //TextRecognizer
    private TextRecognizer textRecognizer;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_list_user);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init UI Views
        inputImageBtn = findViewById(R.id.inputImageBtn);
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn);
        imageIv = findViewById(R.id.imageIv);
        recognizedTextEt = findViewById(R.id.recognizedTextEt);
        dateTextView = findViewById(R.id.dateTextView);
        submitBtn = findViewById(R.id.submitBtn);
        calendar = Calendar.getInstance();
        backBtn = findViewById(R.id.backBtn);

        receiptNameEt = findViewById(R.id.receiptNameEt);
        receiptAmountEt = findViewById(R.id.receiptAmountEt);
        receiptLocationEt = findViewById(R.id.receiptLocationEt);

        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        //Init Arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES};

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //Init TextRecognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //Handle Click
        inputImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputImageDialog();
            }
        });

        recognizeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    Toast.makeText(ReceiptListUserActivity.this, "Pick Image first...", Toast.LENGTH_SHORT).show();
                }
                else {
                    recognizeTextFromImage();
                }
            }
        });

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ReceiptListUserActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                        calendar.set(year, month, day);
                        dateGlobal = calendar.getTimeInMillis();
                        timeInMilisGlobal2 = String.valueOf(dateGlobal);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String date2 = formatter.format(dateGlobal);
                        dateTextView.setText(date2);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private String title = "", date = "", transcribedText = "", location = "";

    private double expensesAmt = 0.00;

    private void validateData() {
        //Validate data
        Log.d(TAG, "validateData: Validating Data...");

        //Get Data
        title = receiptNameEt.getText().toString().trim();
        expensesAmt = Double.parseDouble(receiptAmountEt.getText().toString().trim());
        date = String.valueOf(dateTextView.getText().toString().trim());
        transcribedText = recognizedTextEt.getText().toString().trim();
        location = receiptLocationEt.getText().toString().trim();

        //Validate
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Receipt Name cannot be empty...", Toast.LENGTH_SHORT).show();
        }
        else if (date == null || date.trim().isEmpty()) {
            Toast.makeText(this, "Date cannot be null...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(String.valueOf(expensesAmt))) {
            Toast.makeText(this, "Amount cannot be empty...", Toast.LENGTH_SHORT).show();
        }
        else if (Double.isNaN(expensesAmt)) {
            Toast.makeText(this, "Amount cannot be empty...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(transcribedText)) {
            Toast.makeText(this, "Transcribed content cannot be empty...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Location cannot be empty...", Toast.LENGTH_SHORT).show();
        }
        else {
            uploadReceiptImageToReceiptDatabase();
        }
    }

    private void uploadReceiptImageToReceiptDatabase() {
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        String uid = firebaseAuth.getUid();

        String imagePathAndName = "ReceiptImages/" + timestamp;

        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference();
        usernameRef.child("Users").child(uid);

        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("name").getValue(String.class);

                StorageReference storageReference = FirebaseStorage.getInstance().getReference(imagePathAndName);
                storageReference.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful()) ;
                                String uploadedImageUrl = "" + uriTask.getResult();

                                //Upload to Firebase
                                uploadToReceiptDatabase(username, uploadedImageUrl, timestamp);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ReceiptListUserActivity.this, "Image Upload Failed due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadToReceiptDatabase(String username, String uploadedImageUrl, long timestamp) {
        Log.d(TAG, "uploadToReceiptDatabase: Uploading to Database...");

        //Show Progress
        progressDialog.setMessage("Uploading Information...");
        progressDialog.show();

        String uid = firebaseAuth.getUid();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = currentUser.getEmail();
        String userName = getIntent().getStringExtra("name");

        //Setup Data to Upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("url", ""+uploadedImageUrl);
        hashMap.put("location", ""+location);
        hashMap.put("expensesAmt", expensesAmt);
        hashMap.put("dateReceipt", ""+date);
        hashMap.put("description", ""+transcribedText);
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .child("Receipts")
                .child(String.valueOf(timestamp))
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ReceiptListUserActivity.this, "Receipt saved...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ReceiptListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void recognizeTextFromImage() {
        Log.d(TAG, "recognizeTextFromImage: ");
        progressDialog.setMessage("Preparing Text...");
        progressDialog.show();

        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
            progressDialog.setMessage("Recognizing Text...");

            Task<Text> textTaskResult = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            progressDialog.dismiss();
                            String recognizedText = text.getText();
                            Log.d(TAG, "onSuccess: recognizedText: "+recognizedText);
                            recognizedTextEt.setText(recognizedText);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onFailure: ", e);
                            Toast.makeText(ReceiptListUserActivity.this, "Failed recognizing due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            progressDialog.dismiss();
            Log.d(TAG, "recognizeTextFromImage: ", e);
            Toast.makeText(this, "Failed preparing image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, inputImageBtn);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

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

//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                int id = menuItem.getItemId();
//                if (id == 1) {
//                    Log.d(TAG, "onMenuItemClick: Camera Clicked...");
//                    if (checkCameraPermissions()) {
//                        pickImageCamera();
//                    }
//                    else {
//                        requestCameraPermissions();
//                    }
//                }
//                else if (id == 2){
//                    Log.d(TAG, "onMenuItemClick: Gallery Clicked...");
//                    if (checkStoragePermission()){
//                        pickImageGallery();
//                    }
//                    else {
//                        requestStoragePermission();
//                    }
//                }
//                return true;
//            }
//        });

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
                        MyUtils.toast(ReceiptListUserActivity.this, "Camera or Storage or both permissions denied...");
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
                        MyUtils.toast(ReceiptListUserActivity.this, "Storage permission denied...!");
                    }
                }
            }
    );

    private void pickImageGallery() {
        Log.d(TAG, "pickImageGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //Receive the image, if picked
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image picked
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageUri "+ imageUri);
                        //Set to ImageView
                        imageIv.setImageURI(imageUri);
                    }
                    else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(ReceiptListUserActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ");
        ContentValues values =  new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: imageUri" +imageUri);
                        imageIv.setImageURI(imageUri);
                    } else {
                        Log.d(TAG, "onActivityResult: cancelled");
                        Toast.makeText(ReceiptListUserActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return cameraResult && storageResult;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //Handle Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        pickImageCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage Permissions are required.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>1) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickImageGallery();
                    } else {
                        Toast.makeText(this, "Storage Permission is required.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

    }

}
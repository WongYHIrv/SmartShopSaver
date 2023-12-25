package com.example.smartshopsaver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.smartshopsaver.activities.MainAdminActivity;
import com.example.smartshopsaver.activities.MainSellerActivity;
import com.example.smartshopsaver.activities.MainUserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MyUtils {
    private static final String TAG = "MY_UTILS_TAG";

    public static void checkUserType(Activity activity) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("" + firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //check user type
                        String accountType = "" + dataSnapshot.child("accountType").getValue();
                        if (accountType.equals(Constants.USER_TYPE_USER)) {
                            //user is user
                            activity.startActivity(new Intent(activity, MainUserActivity.class));
                            activity.finishAffinity();
                        } else if (accountType.equals(Constants.USER_TYPE_SELLER)) {
                            //user is seller
                            activity.startActivity(new Intent(activity, MainSellerActivity.class));
                            activity.finishAffinity();
                        } else if (accountType.equals(Constants.USER_TYPE_ADMIN)) {
                            //user is admin
                            activity.startActivity(new Intent(activity, MainAdminActivity.class));
                            activity.finishAffinity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public static void updateFCMToken() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            String myUid = "" + firebaseAuth.getUid();
            Log.d(TAG, "updateFCMToken: myUid: " + myUid);
            //1) Get FCM Token
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> {
                        Log.d(TAG, "onSuccess: token: " + token);

                        //Setup Data (fcmToken) to update to currently logged-in user's db
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("fcmToken", token);

                        //2) Update FCM Token to Firebase DB
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(myUid)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(unused -> Log.d(TAG, "onSuccess: Token Updated...!"))
                                .addOnFailureListener(e -> Log.e(TAG, "updateFCMToken: onFailure: ", e));
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "updateFCMToken: onFailure: ", e));
        }

    }

    public static void openMap(Context context, String latitude, String longitude) {
        // Create a Uri from an intent string. Use the result to create an Intent.
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+latitude+","+longitude));
        context.startActivity(intent);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * A Function to get current timestamp
     *
     * @return Return the current timestamp as long datatype
     */
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * A Function to show Toast
     *
     * @param timestamp the timestamp of type Long that we need to format to dd/MM/yyyy
     * @return timestamp formatted to date dd/MM/yyyy
     */
    public static String formatTimestampDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();

        return date;
    }


    /**
     * A Function to show Toast
     *
     * @param timestamp the timestamp of type Long that we need to format to dd/MM/yyyy hh:mm:a
     * @return timestamp formatted to date dd/MM/yyyy hh:mm:a
     */
    public static String formatTimestampDateTime(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString();

        return date;
    }

    public static String formatOneDateToAnother(String dateFormatFrom, String dateFormatTo, String dateToConvert) {
        //Conver GMT Date Time Zone 24hrs format
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatFrom);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat(dateFormatTo);

        String out = "";
        try {
            Date date = dateFormat.parse(dateToConvert);
            out = dateFormat2.format(date);
        } catch (ParseException e) {
            out = dateToConvert;
        }

        return out;
    }

    public static double daysBetweenTwoTimestamps(long timestampStart, long timestampEnd) {
        //long noOfDays = timestampEnd - timestampStart;
        long noOfDays = timestampEnd - timestampStart;

        double mDifferenceDates = noOfDays / (24 * 60 * 60 * 1000);

        return mDifferenceDates;
    }

    /**
     * File Name From Uri
     *
     * @param context Context of Class/Activity/Fragment
     * @param uri     Uri of the file
     * @return The name of the file
     */
    public static String fileNameFromUri(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public static String roundedDecimalValue(double decimalValue) {

        String roundedDecimalValue = "0";
        try {
            roundedDecimalValue = String.format("%.2f", decimalValue);
        } catch (Exception e) {
            roundedDecimalValue = "" + decimalValue;
        }

        return roundedDecimalValue;
    }

    public static void incrementBrochureViewCount(String brochureId) {
        //1) Get book views count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.child(brochureId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get views count
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        //in case of null replace with 0
                        if (viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }

                        //2)Increment views count
                        long newViewsCount = Long.parseLong(viewsCount) + 1;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Brochures");
                        reference.child(brochureId).updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void downloadBrochure(Context context, String brochureId, String brochureTitle, String brochureUrl) {
        Log.d(TAG, "downloadBook: downloading book...");

        String nameWithExtension = brochureTitle + ".pdf";
        Log.d(TAG, "downloadBook: NAME: " + nameWithExtension);

        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading " + nameWithExtension + "..."); //e.g. Downloding ABC_Book.pdf
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //download from firebase storage using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(brochureUrl);
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(bytes -> {
                    Log.d(TAG, "onSuccess: Book Downloaded");
                    saveDownloadedBrochure(context, progressDialog, bytes, nameWithExtension, brochureId);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: Failed to download due to " + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(context, "Failed to download due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static void saveDownloadedBrochure(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String brochureId) {
        Log.d(TAG, "saveDownloadedBook: Saving downloaded book");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "saveDownloadedBook: Saved to Download Folder");
            progressDialog.dismiss();

            incrementBrochureDownloadCount(brochureId);
        } catch (Exception e) {
            Log.d(TAG, "saveDownloadedBook: Failed saving to Download Folder due to " + e.getMessage());
            Toast.makeText(context, "Failed saving to Download Folder due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBrochureDownloadCount(String brochureId) {
        Log.d(TAG, "incrementBookDownloadCount: Incrementing Book Download Count");

        //Step 1: Get previous download count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Brochures");
        ref.child(brochureId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        Log.d(TAG, "incrementBrochureDownloadCount: onDataChange: Downloads Count: " + downloadsCount);

                        if (downloadsCount.equals("") || downloadsCount.equals("null")) {
                            downloadsCount = "0";
                        }

                        //convert to long and increment 1
                        long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                        Log.d(TAG, "incrementBrochureDownloadCount: onDataChange: New Download Count: " + newDownloadsCount);

                        //setup data to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        //Step 2) Update new incremented downloads count to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Brochures");
                        reference.child(brochureId).updateChildren(hashMap)
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, "incrementBrochureDownloadCount: onSuccess: Downloads Count Updated");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "incrementBrochureDownloadCount: onFailure: ", e);
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void checkRemoveCartSellerUidHaveNoProducts() {
        String myUid = "" + FirebaseAuth.getInstance().getUid();
        ;
        //If there is no products in cart, remove cart
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
        ref1.child(myUid)
                .child("Cart")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //Get Seller UIDs
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String sellerUidInCart = "" + ds.getRef().getKey();

                            //Check if specific seller UID have Products
                            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                            ref1.child(myUid)
                                    .child("Cart")
                                    .child(sellerUidInCart)
                                    .child("Products")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            if (!snapshot.exists()) {
                                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                                                ref1.child(myUid)
                                                        .child("Cart")
                                                        .child(sellerUidInCart)
                                                        .removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static String formatCurrency(double value) {

        Locale malaysiaLocale =new Locale("ms", "MY");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(malaysiaLocale);
        return currencyFormat.format(value);
    }


}

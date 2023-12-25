package com.example.smartshopsaver.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.smartshopsaver.Constants;
import com.example.smartshopsaver.MyUtils;
import com.example.smartshopsaver.R;
import com.example.smartshopsaver.databinding.ActivityMainUserBinding;
import com.example.smartshopsaver.fragments.BrochureListSellerFragment;
import com.example.smartshopsaver.fragments.CartUserFragment;
import com.example.smartshopsaver.fragments.HomeUserFragment;
import com.example.smartshopsaver.fragments.OrderListUserFragment;
import com.example.smartshopsaver.fragments.ShopListUserFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainUserActivity extends AppCompatActivity {

    //View Binding
    private ActivityMainUserBinding binding;

    //Tag to logs in logcat
    private static final String TAG = "MAIN_USER_TAG";

    //FirebaseAuth for auth related tasks
    private FirebaseAuth firebaseAuth;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view binding
        binding = ActivityMainUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup navigation drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close);
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        fragmentHome();

        MyUtils.updateFCMToken();
        loadMyInfo();
        askNotificationPermission();

        //handle navigationView item click, preform task based on item clicked
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragmentHome();
            } else if (itemId == R.id.nav_cart) {
                fragmentCart();
            } else if (itemId == R.id.nav_orders) {
                fragmentOrders();
            } else if (itemId == R.id.nav_brochures) {
                startActivity(new Intent(this, BrochureListUserActivity.class));
            } else if (itemId == R.id.nav_route_comparison) {
                startActivity(new Intent(this, RouteComparisonUserActivity.class));
            } else if (itemId == R.id.nav_product_comparison) {
                Constants.PRODUCT_ACTION_TYPE = Constants.PRODUCT_ACTION_TYPE_COMPARISON;
                Intent intent = new Intent(this, ProductCategoryListUserActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_profile) {
                openProfile();
            } else if (itemId == R.id.nav_logout) {
                logoutUser();
            } else if (itemId == R.id.nav_expense_management) {
                startActivity(new Intent(this, ExpenseManagementUserActivity.class));
            } else if (itemId == R.id.nav_receipt_management) {
                startActivity(new Intent(this, ReceiptViewUserActivity.class));
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });

        //handle click, open/close side menu
        binding.toolbarMeuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        //handle toolbarLogoutBtn click, logout
        binding.toolbarLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        //handle toolbarProfileBtn click, start ProfileEditUserActivity
        binding.toolbarProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });
    }

    private void fragmentHome() {
        HomeUserFragment fragment = new HomeUserFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "HomeUserFragment");
        fragmentTransaction.commit();
    }

    public void fragmentShopListUser() {
        ShopListUserFragment fragment = new ShopListUserFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "ShopListUserFragment");
        fragmentTransaction.commit();
    }

    public void fragmentCart() {
        CartUserFragment fragment = new CartUserFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "CartUserFragment");
        fragmentTransaction.commit();
    }

    public void fragmentOrders() {
        OrderListUserFragment fragment = new OrderListUserFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "OrderListUserFragment");
        fragmentTransaction.commit();
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
                        String email = "" + snapshot.child("email").getValue();
                        String sLatitude = "" + snapshot.child("latitude").getValue();
                        String sLongitude = "" + snapshot.child("longitude").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String online = "" + snapshot.child("online").getValue();
                        String phone = "" + snapshot.child("phone").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String state = "" + snapshot.child("state").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        //Get UI Views from Navigation View header nav_header.xml
                        View headerView = binding.navigationView.getHeaderView(0);
                        ShapeableImageView navProfileIv = headerView.findViewById(R.id.navProfileIv);
                        TextView navNameTv = headerView.findViewById(R.id.navNameTv);
                        TextView navEmailTv = headerView.findViewById(R.id.navEmailTv);
                        TextView navAccountTypeTv = headerView.findViewById(R.id.navAccountTypeTv);

                        //Set data to UI Views
                        navNameTv.setText(name);
                        navEmailTv.setText(email);
                        navAccountTypeTv.setText(accountType);

                        try {
                            Glide.with(MainUserActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.person_white)
                                    .into(navProfileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private void openProfile() {
        startActivity(new Intent(this, ProfileEditUserActivity.class));
    }

    private void askNotificationPermission() {
        //Android 13/TIRAMISU/API_33 and above requires POST_NOTIFICATIONS permission e.g. to show push notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Check if permission is granted or not
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private ActivityResultLauncher<String> requestNotificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: Notification Permission STATUS: " + isGranted);
                }
            }
    );

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
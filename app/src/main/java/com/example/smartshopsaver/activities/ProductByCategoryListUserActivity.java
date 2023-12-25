package com.example.smartshopsaver.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.smartshopsaver.databinding.ActivityProductByCategoryListUserBinding;
import com.example.smartshopsaver.fragments.ProductByCategoryListUserFragment;
import com.example.smartshopsaver.models.ModelProductSubCategory;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductByCategoryListUserActivity extends AppCompatActivity {

    private ActivityProductByCategoryListUserBinding binding;

    private static final String TAG = "PRODUCT_BY_CAT_TAG";

    private String productCategoryId = "";

    private ArrayList<ModelProductSubCategory> productSubCategoryArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductByCategoryListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productCategoryId = "" + getIntent().getStringExtra("productCategoryId");

        loadProductCategoryDetails();
        loadProductSubCategories();

        //handle toolbarBackBtn click: go-back
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadProductCategoryDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String productCategory = "" + snapshot.child("productCategory").getValue();

                        binding.toolbarTitleTv.setText(productCategory);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductSubCategories() {
        productSubCategoryArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ProductCategories");
        ref.child(productCategoryId)
                .child("ProductSubCategories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productSubCategoryArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                ModelProductSubCategory modelBrochureCategory = ds.getValue(ModelProductSubCategory.class);
                                productSubCategoryArrayList.add(modelBrochureCategory);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }

                        binding.viewPager.setAdapter(new ViewPagerFragmentAdapter(ProductByCategoryListUserActivity.this));
                        // attaching tab mediator
                        new TabLayoutMediator(binding.subCategoriesTl, binding.viewPager, (tab, position) -> {
                            tab.setText(productSubCategoryArrayList.get(position).getProductSubCategory());
                        }).attach();
                        //set tab change orientation ORIENTATION_VERTICAL, ORIENTATION_HORIZONTAL
                        binding.viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                        //for fragment change animation
                        binding.viewPager.setPageTransformer(new HorizontalFlipTransformation());
                        binding.viewPager.setOffscreenPageLimit(productSubCategoryArrayList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private class ViewPagerFragmentAdapter extends FragmentStateAdapter {

        public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new ProductByCategoryListUserFragment().newInstance("" + productCategoryId, "" + productSubCategoryArrayList.get(position).getProductSubCategoryId());
        }

        @Override
        public int getItemCount() {
            return productSubCategoryArrayList.size();
        }
    }

    /*More Animations https://www.androidhive.info/2020/01/viewpager2-pager-transformations-intro-slider-pager-animations-pager-transformations/*/
    public class HorizontalFlipTransformation implements ViewPager2.PageTransformer {
        @Override
        public void transformPage(View page, float position) {

            page.setTranslationX(-position * page.getWidth());
            page.setCameraDistance(12000);

            if (position < 0.5 && position > -0.5) {
                page.setVisibility(View.VISIBLE);
            } else {
                page.setVisibility(View.INVISIBLE);
            }

            if (position < -1) {     // [-Infinity,-1)
                page.setAlpha(0);
            } else if (position <= 0) {    // [-1,0]
                page.setAlpha(1);
                page.setRotationY(180 * (1 - Math.abs(position) + 1));
            } else if (position <= 1) {    // (0,1]
                page.setAlpha(1);
                page.setRotationY(-180 * (1 - Math.abs(position) + 1));
            } else {
                page.setAlpha(0);
            }
        }
    }

}
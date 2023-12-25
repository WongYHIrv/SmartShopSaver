package com.example.smartshopsaver.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartshopsaver.HorizontalFlipTransformation;
import com.example.smartshopsaver.databinding.FragmentReportCasesAdminBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class ReportCasesAdminFragment extends Fragment {

    private FragmentReportCasesAdminBinding binding;

    private static final String TAG = "CASES_TAG";

    private Context mContext;


    String[] tabTitles = {"Products", "Orders"};


    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    public ReportCasesAdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentReportCasesAdminBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.viewPager.setAdapter(new ViewPagerFragmentAdapter(this));
        // attaching tab mediator
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
        //set tab change orientation ORIENTATION_VERTICAL, ORIENTATION_HORIZONTAL
        binding.viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        //for fragment change animation
        binding.viewPager.setPageTransformer(new HorizontalFlipTransformation());
    }

    private class ViewPagerFragmentAdapter extends FragmentStateAdapter {


        public ViewPagerFragmentAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ReportProductListAdminFragment();
                case 1:
                    return new ReportOrderListAdminFragment();
            }
            return new ReportProductListAdminFragment();
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
}
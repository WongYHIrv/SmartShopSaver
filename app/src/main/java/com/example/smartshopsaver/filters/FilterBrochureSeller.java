package com.example.smartshopsaver.filters;

import android.widget.Filter;

import com.example.smartshopsaver.adapters.AdapterBrochureSeller;
import com.example.smartshopsaver.models.ModelBrochure;

import java.util.ArrayList;

public class FilterBrochureSeller extends Filter {

    //declaring AdapterAd and ArrayList<ModelAd> instance that will be initialized in constructor of this class
    private AdapterBrochureSeller adapter;
    private ArrayList<ModelBrochure> filterList;

    /**
     * Filter Ad Constructor
     *
     * @param adapter    AdapterBrochure instance to be passed when this constructor is created
     * @param filterList ad arraylist to be passed when this constructor is created
     */
    public FilterBrochureSeller(AdapterBrochureSeller adapter, ArrayList<ModelBrochure> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        //perform filter based on what user type

        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            //the search query is not null and not empty, we can perform filter
            //convert the typed query to upper case to make search not case sensitive e.g. Samsung S23 Ultra -> SAMSUNG S23 ULTRA
            constraint = constraint.toString().toUpperCase();
            //hold the filtered list of Ads based on user searched query
            ArrayList<ModelBrochure> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //Ad filter based on Brand, Category, Condition, Title. If any of these matches add it to the filteredModels list
                if (filterList.get(i).getBrochureName().toUpperCase().contains(constraint)) {
                    //Filter matched add to filteredModels list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            //the search query is either null or empty, we can't perform filter. Return full/original list
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //publish the filtered result

        adapter.brochureArrayList = (ArrayList<ModelBrochure>) results.values;

        adapter.notifyDataSetChanged();
    }
}

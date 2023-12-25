package com.example.smartshopsaver.filters;

import android.widget.Filter;

import com.example.smartshopsaver.adapters.AdapterReceiptUser;
import com.example.smartshopsaver.models.ModelReceipt;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

public class FilterReceipt extends Filter {

    ArrayList<ModelReceipt> filterList;

    AdapterReceiptUser adapterReceiptUser;

    public FilterReceipt(ArrayList<ModelReceipt> filterList, AdapterReceiptUser adapterReceiptUser) {
        this.filterList = filterList;
        this.adapterReceiptUser = adapterReceiptUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        //Value is not null and empty
        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelReceipt> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //Validation
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    //Added to the filterList
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values =  filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //Apply filtered change
        adapterReceiptUser.receiptArrayList = (ArrayList<ModelReceipt>)results.values;

        //Notify Receipt View
        adapterReceiptUser.notifyDataSetChanged();

    }
}

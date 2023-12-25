package com.example.smartshopsaver.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.smartshopsaver.DBHelper;
import com.example.smartshopsaver.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseManagementUserActivity extends AppCompatActivity {

    Button addExpensesButton,viewAllExpensesButton,viewExpensesByDateButton;
    DBHelper db;
    LinearLayout mainLinearLayout;
    AnyChartView chart;
    List<Map<String,String>> data = null;
    ArrayList<Double> price = new ArrayList<Double>();
    ArrayList<String> category = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_management_user);
        init();
        queryForPie();
        fillPie();

        addExpensesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(ExpenseManagementUserActivity.this, AddExpenses.class));
            }
        });

        viewAllExpensesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Cursor res = db.getdata();
                if(res.getCount()==0){
                    Toast.makeText(ExpenseManagementUserActivity.this, "No Entry Exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while(res.moveToNext()){
                    buffer.append("Category: "+res.getString(1)+"\n");
                    buffer.append("Price: RM"+res.getString(2)+"\n");
                    long l = Long.parseLong(res.getString(3));
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String dateQuery = formatter.format(new Date(l));
                    buffer.append("Date: "+dateQuery+"\n\n");
                }
                // AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ExpenseManagementUserActivity.this, R.style.AlertDialogCustom));
                builder.setCancelable(true);
                builder.setTitle("Cash Management Tracker");
                builder.setMessage(buffer.toString());

                builder.show();
            }
        });

        viewExpensesByDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(ExpenseManagementUserActivity.this, ViewByDateExpense.class));
            }
        });
    }

    public void queryForPie(){
        Cursor res = db.getdata();
        if(res.getCount()==0){
            chart.setVisibility(View.GONE);
            Toast.makeText(ExpenseManagementUserActivity.this, "No Entry Exists", Toast.LENGTH_SHORT).show();
        }
        StringBuffer buffer = new StringBuffer();
        while(res.moveToNext()) {
            String s = res.getString(1);
            int index = 0;
            if (category.contains(s)) {
                index = category.indexOf(s);
                double priceLocal = price.get(index);
                priceLocal += Double.parseDouble(res.getString(2));
                price.set(index, priceLocal);
            } else {
                category.add(res.getString(1));
                price.add(Double.parseDouble(res.getString(2)));
            }
        }
    }

    public void fillPie(){
        Pie pie = AnyChart.pie();
        List<DataEntry> dataEntries = new ArrayList<>();
        for (int i = 0; i <= category.size() - 1; i++) {
            dataEntries.add(new ValueDataEntry(category.get(i), price.get(i)));
        }
        pie.data(dataEntries);
        pie.title("Overview of Expenses");
        // pie.background(String.valueOf(Color.BLACK));

        chart.setChart(pie);
        // chart.animate().rotation(5000).setDuration(5000);
    }

    public void init() {
        db = new DBHelper(this);
        addExpensesButton = (Button) findViewById(R.id.addExpensesButton);
        viewAllExpensesButton = (Button) findViewById(R.id.viewAllExpensesButton);
        viewExpensesByDateButton = (Button) findViewById(R.id.viewExpensesByDateButton);
        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
        chart = findViewById(R.id.chart);
//        chart.setBackgroundColor("black");

    }

    public void onRestart() {
        super.onRestart();
        reloadPage();
    }

    public void reloadPage() {
        overridePendingTransition(0, 100);
        startActivity(new Intent(ExpenseManagementUserActivity.this, MainUserActivity.class));
        overridePendingTransition(0, 100);
        finish();
    }
}
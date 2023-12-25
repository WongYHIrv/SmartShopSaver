package com.example.smartshopsaver.activities;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ParseException;
import android.os.Bundle;
import android.renderscript.Sampler.Value;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.CategoryValueDataEntry;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.smartshopsaver.DBHelper;
import com.example.smartshopsaver.R;

public class ViewByDateExpense extends Activity {

    Button viewBetweenTwoDateButton,viewByOneDateButton,filterButton;
    TextView selectDateTextView,selectDate2TextView;
    long dateGlobal;
    String timeInMilisGlobal,timeInMilisGlobal2;
    Calendar calendar = Calendar.getInstance();

    // this 3 final int are date today
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);

    DBHelper DB;
    SimpleAdapter adapter;
    ArrayList<Long> expenses = new ArrayList<Long>();
    List<Map<String,String>> data2 = new ArrayList<Map<String,String>>();
    GridView gridView;
    String date1,date2;
    boolean oneDateMode = false,isGraphFirstDraw = true;
    LinearLayout gridviewlinearLayout,anyChart2LinearLayout;

    RelativeLayout view_by_dateLinearLayout;
    Button viewMoreInfo;
    ArrayList<Long> expensesGlobal = null;
    ArrayList<Double> price = new ArrayList<Double>();
    ArrayList<String> category = new ArrayList<String>();
    AnyChartView anyChartView;
    private Set set;
    List<DataEntry> data = new ArrayList<>();
    Cartesian cartesian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_by_date_expense);
        init();
        fillListData(null,null);
        columnGraph();

        viewByOneDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                selectDateTextView.setVisibility(View.VISIBLE);
                selectDate2TextView.setVisibility(View.GONE);
                selectDateTextView.setText("Select Date");
                oneDateMode = true;
            }
        });

        viewBetweenTwoDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                selectDateTextView.setVisibility(View.VISIBLE);
                selectDate2TextView.setVisibility(View.VISIBLE);
                selectDateTextView.setText("Select Date");
                selectDate2TextView.setText("Select Date");
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                date1 = selectDateTextView.getText().toString();
                date2 = selectDate2TextView.getText().toString();
                if(selectDateTextView.getVisibility() == View.VISIBLE && selectDate2TextView.getVisibility() == View.GONE) {
                    // View by one date
                    if(date1.equals("Select Date")) {
                        Toast.makeText(getApplicationContext(),"Please select date!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fillListData(timeInMilisGlobal,timeInMilisGlobal2);
                }
                else if(selectDateTextView.getVisibility() == View.VISIBLE && selectDate2TextView.getVisibility() == View.VISIBLE) {
                    // View between two date
                    if(date1.equals("Select Date") || date2.equals("Select Date") ) {
                        Toast.makeText(getApplicationContext(),"Please select date!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fillListData(timeInMilisGlobal,timeInMilisGlobal2);
                }
                else
                    Toast.makeText(getApplicationContext(),"Please select viewing mode",Toast.LENGTH_SHORT).show();
            }
        });

        selectDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ViewByDateExpense.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                        calendar.set(year, month, day);
                        minimumMili();
                        dateGlobal = calendar.getTimeInMillis();
                        timeInMilisGlobal = String.valueOf(dateGlobal);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String date2 = formatter.format(dateGlobal);
                        selectDateTextView.setText(date2);
                        if(selectDateTextView.getVisibility() == View.VISIBLE && selectDate2TextView.getVisibility() == View.GONE) {
                            maximumMili();
                            dateGlobal = calendar.getTimeInMillis();
                            timeInMilisGlobal2 = String.valueOf(dateGlobal);
                            oneDateMode = false;
                        }
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        selectDate2TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ViewByDateExpense.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                        calendar.set(year, month, day);
                        maximumMili();
                        dateGlobal = calendar.getTimeInMillis();
                        timeInMilisGlobal2 = String.valueOf(dateGlobal);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String date2 = formatter.format(dateGlobal);
                        selectDate2TextView.setText(date2);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final String id = data2.get(arg2).get("id");
                String category = data2.get(arg2).get("category");
                String price = data2.get(arg2).get("price");
                String date = data2.get(arg2).get("date");
                StringBuffer sb = new StringBuffer(price);
                sb.deleteCharAt(0); // deleting char at index 0
                CharSequence[] items = {"Delete","Delete All", "Update"};	// will put update feature if more time available
//				CharSequence[] items = {"Delete this","Delete All"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(ViewByDateExpense.this);
                dialog.setTitle("Choose an action");

                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            // Delete this block
                            if(DB.deleteById(id)) {
                                fillListData(timeInMilisGlobal, timeInMilisGlobal2);
                                columnGraphUpdate(timeInMilisGlobal,timeInMilisGlobal2);
                                Toast.makeText(getApplicationContext(), "deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(getApplicationContext(), "deleted unsuccessfully", Toast.LENGTH_SHORT).show();

                        } else if (item == 1){
                            // delete all block
                            if(DB.deleteAll()) {
                                fillListData(timeInMilisGlobal, timeInMilisGlobal2);
                                columnGraphUpdate(timeInMilisGlobal,timeInMilisGlobal2);
                                Toast.makeText(getApplicationContext(), "deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(getApplicationContext(), "deleted unsuccessfully", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if(DB.updateById(ViewByDateExpense.this, id)) {
                                Intent intent = getIntent();
                                String category = intent.getStringExtra("category");
                                String price = intent.getStringExtra("price");
                                Long date = intent.getLongExtra("date", 0);

                                AlertDialog.Builder updateDialog = new AlertDialog.Builder(ViewByDateExpense.this);
                                updateDialog.setTitle("Update Price");

                                final EditText input = new EditText(ViewByDateExpense.this);
                                input.setText(price);
                                updateDialog.setView(input);

                                updateDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String newPrice = input.getText().toString();

                                        boolean success = DB.changeDataById(category,newPrice, date);
                                        if (success) {
                                            fillListData(timeInMilisGlobal, timeInMilisGlobal2);
                                            columnGraphUpdate(timeInMilisGlobal, timeInMilisGlobal2);
                                            Toast.makeText(getApplicationContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Update unsuccessful", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                updateDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                                AlertDialog updateAlertDialog = updateDialog.create();
                                updateAlertDialog.show();
                            }
                        }
                    }
                });
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
                return false;
            }
        });

        viewMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(expensesGlobal  == null) {
                    Toast.makeText(getApplicationContext(), "No Entry Exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                double firstIndex = expensesGlobal.get(0);
                double sum = 0,average=0,higest=firstIndex,lowest=firstIndex,median,range;
                for(double l:expensesGlobal) {
                    sum+=l;
                    if(l>higest)
                        higest = l;
                    if(l<lowest)
                        lowest = l;
                }
                double values[] = new double[expensesGlobal.size()];
                for(int i=0;i<=expensesGlobal.size()-1;i++){
                    values[i] = expensesGlobal.get(i);
                }
                Arrays.sort(values);
                int totalElements = values.length;
                if (totalElements % 2 == 0) {
                    double sumOfMiddleElements = values[totalElements / 2] + values[totalElements / 2 - 1];
                    // calculate average of middle elements
                    median = ((double) sumOfMiddleElements) / 2;
                } else {
                    // get the middle element
                    median = (double) values[values.length / 2];
                }
                range = higest - lowest;
                average = sum/expensesGlobal.size();
                StringBuffer buffer = new StringBuffer();
                buffer.append("Highest Expenses: RM"+higest+"\n");
                buffer.append("Lowest Expenses : RM"+lowest+"\n");
                buffer.append("Total Expenses  : RM"+sum+"\n");
                buffer.append("=========================");
                buffer.append("Average Expenses: RM"+average+"\n");
                buffer.append("Mean            : RM"+average+"\n");
                buffer.append("Median          : RM"+median+"\n");
                buffer.append("Range           : RM"+range+"\n");
                buffer.append("Your Array      : "+Arrays.toString(values)+"\n");
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(new ContextThemeWrapper(ViewByDateExpense.this, R.style.AlertDialogCustom));
                builder.setCancelable(true);
                builder.setTitle("Detailed Info");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
    }

    public void minimumMili() {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public void maximumMili() {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);
    }

    public void fillListData(String date1Local,String date2Local) {
        Cursor res = null;
        if(date1Local==null && date2Local==null)
            res = DB.getdata();
        else
            res = DB.getdata2(date1Local,date2Local);
        if(res == null)
            return;

        data2.clear();
        expenses.clear();
        if(res.getCount()==0) {
            Toast.makeText(getApplicationContext(), "No Entry Exists", Toast.LENGTH_SHORT).show();
            data2.clear();
            expenses.clear();
            gridviewlinearLayout.removeAllViews();
            gridviewlinearLayout.addView(gridView);
            expensesGlobal = null;
            anyChartView.setVisibility(View.GONE);
            return;
        }
        else {
            while(res.moveToNext()) {
                Map<String, String> tab = new HashMap<String, String>();
                tab.put("id", String.valueOf(res.getInt(0)));
                tab.put("category", res.getString(1));
                tab.put("price", "₱"+res.getString(2));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateQuery = formatter.format(new Date(res.getLong(3)));
                tab.put("date", dateQuery);
                data2.add(tab);
                expenses.add(res.getLong(2));
            }
        }
        if(!expenses.isEmpty()) {
            expensesGlobal = expenses;
            if(isGraphFirstDraw == false) {
                columnGraphUpdate(date1Local, date2Local);
                anyChartView.setVisibility(View.VISIBLE);
            }
        }
        else {
            expensesGlobal = null;
            set.data(data);
            anyChartView.setVisibility(View.GONE);
        }
        String[] from = {"category","price","date"};
        int[] to = {R.id.categoryTextView,R.id.priceTextView,R.id.dateTextView};
        adapter = new SimpleAdapter(this,data2,R.layout.grid_view_layout, from, to);
        gridView.setAdapter(adapter);
        gridviewlinearLayout.removeAllViews();
        gridviewlinearLayout.addView(gridView);
    }

    public  void columnGraph() {
        cartesian = AnyChart.column();
        anyChartView.setBackgroundColor("black");
        //cartesian.background(String.valueOf(Color.BLACK));
        Cursor res = DB.getdata();
        if (res.getCount() == 0) {
            anyChartView.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "No Entry Exists", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            String s = res.getString(1);
            int index = 0;
            if (category.contains(s)) {
                index = category.indexOf(s);
                double priceLocal = price.get(index);
                priceLocal += Double.parseDouble(res.getString(2));
                price.set(index,priceLocal);
            } else {
                category.add(s);
                price.add(Double.parseDouble(res.getString(2)));
            }
        }
        for (int i=0; i<=price.size()-1;i++){
            for (int j=i+1; j<=price.size()-1; j++){
                if (price.get(i)>price.get(j)){
                    double k = price.get(i);
                    price.set(i,price.get(j));
                    price.set(j,k);
                    String k2 = category.get(i);
                    category.set(i,category.get(j));
                    category.set(j,k2);
                }
            }
        }
        for (int i=0; i<=category.size()-1;i++){
            data.add(new ValueDataEntry(category.get(i), price.get(i)));
        }
        set = Set.instantiate();
        set.data(data);
        Mapping series1Data = set.mapAs("{ x: 'x', value: 'value' }");
        Column column = cartesian.column(series1Data);
        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("RM{%Value}{groupsSeparator: }");
        cartesian.animation(true);
        cartesian.title("Expenses");
        cartesian.yScale().minimum(0d);
        cartesian.yAxis(0).labels().format("RM{%Value}{groupsSeparator: }");
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        anyChartView.setChart(cartesian);
        isGraphFirstDraw = false;
        category.clear();
        price.clear();
    }

    public void columnGraphUpdate(String date1Local,String date2Local){
        Cursor res = null;
        if(date1Local==null && date2Local==null)
            res = DB.getdata();
        else
            res = DB.getdata2(date1Local,date2Local);
        if(res == null)
            return;
        data.clear();
        while (res.moveToNext()) {
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
        for (int i=0; i<=price.size()-1;i++){
            for (int j=i+1; j<=price.size()-1; j++){
                if (price.get(i)>price.get(j)){
                    double k = price.get(i);
                    price.set(i,price.get(j));
                    price.set(j,k);
                    String k2 = category.get(i);
                    category.set(i,category.get(j));
                    category.set(j,k2);
                }
            }
        }
        for (int i=0; i<=category.size()-1;i++){
            data.add(new ValueDataEntry(category.get(i), price.get(i)));
        }

        set.data(data);
        data.clear();
        category.clear();
        price.clear();
    }

    public void reloadPage() {
        Intent callThis= new Intent(".ViewByDate");
        overridePendingTransition(0, 0);
        startActivity(callThis);
        overridePendingTransition(0, 0);
        finish();
    }

    public void init() {
        viewByOneDateButton = (Button) findViewById(R.id.viewByOneDateButton);
        viewBetweenTwoDateButton = (Button) findViewById(R.id.viewBetweenTwoDateButton);
        selectDateTextView = (TextView) findViewById(R.id.selectDateTextView);
        selectDate2TextView = (TextView) findViewById(R.id.selectDate2TextView);
        filterButton = (Button) findViewById(R.id.filterButton);
        gridView = (GridView) findViewById(R.id.gridView);
        gridviewlinearLayout = (LinearLayout) findViewById(R.id.gridviewlinearLayout);
        DB = new DBHelper(this);
        viewMoreInfo = (Button) findViewById(R.id.viewMoreInfo);
        anyChart2LinearLayout = (LinearLayout) findViewById(R.id.anyChart2LinearLayout);
        anyChartView = findViewById(R.id.chart2);
        view_by_dateLinearLayout = (RelativeLayout) findViewById(R.id.view_by_dateLinearLayout);
    }
}
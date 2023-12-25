package com.example.smartshopsaver.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.smartshopsaver.DBHelper;
import com.example.smartshopsaver.R;

public class AddExpenses extends Activity{

    TextView selectDateTextView;
    Calendar calendar = Calendar.getInstance();
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);
    DBHelper db;
    Long dateGlobal;
    EditText categoryEditText,expensesEditText;
    Button addCategoryButton,deleteCategoryButton,addToDbButton,updateCategoryButton;

    ImageButton backBtn;
    Spinner spinner1;
    ArrayAdapter<String> dataAdapter;
    List<String> list = new ArrayList<String>();
    Intent callThis = new Intent(".AddExpenses");
    LinearLayout spinnerLinearLayout;

    RelativeLayout add_expensesLinearLayout;
    String categoryGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expenses);
        ImageButton backBtn = findViewById(R.id.backBtn);
        init();
        db.addIfNoCategory();
        spinner();
        selectDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AddExpenses.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                        calendar.set(year, month, day);
                        dateGlobal = calendar.getTimeInMillis();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        String date2 = formatter.format(dateGlobal);
                        selectDateTextView.setText(date2);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String categoryName = categoryEditText.getText().toString();
                if(categoryName.equals("")) {
                    Toast.makeText(getApplicationContext(),"Please enter category!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(db.checkIfCategoryExist(categoryName)) {
                    Toast.makeText(getApplicationContext(),"Category Exist!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(db.insertCategory(categoryName)) {
                    Toast.makeText(getApplicationContext(),"add success!",Toast.LENGTH_SHORT).show();
                    reloadPage();
                }
                else {
                    Toast.makeText(getApplicationContext(),"add unsuccessful !",Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String categoryName = categoryEditText.getText().toString();
                if(categoryName.equals("--select categories--"))
                    return;
                if(db.deleteCategory(categoryName)) {
                    categoryEditText.setText("");
                    Toast.makeText(getApplicationContext(),"delete success !",Toast.LENGTH_SHORT).show();
                    reloadPage();
                }
                else
                    Toast.makeText(getApplicationContext(),"delete unsuccessful no item like that exist yet!",Toast.LENGTH_SHORT).show();
            }
        });

        updateCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String category = categoryEditText.getText().toString();
                if(categoryGlobal == null || categoryGlobal.equals("--select categories--")) {
                    Toast.makeText(getApplicationContext(),"Please input select category you want to update!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(category.equals("")) {
                    Toast.makeText(getApplicationContext(),"Please input new category!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(db.updateuserdata(categoryGlobal,category)) {
                    reloadPage();
                    Toast.makeText(getApplicationContext(),"Update Success!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"Update Unsuccessful!",Toast.LENGTH_SHORT).show();
                }

            }
        });

        addToDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                boolean isNeedToAddToCategory = false;
                String category = categoryEditText.getText().toString();
                String price = expensesEditText.getText().toString();
                String date = String.valueOf(dateGlobal);
                if(category.equals("") || price.equals("") || dateGlobal ==null) {
                    Toast.makeText(getApplicationContext(),"Please complete details!",Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check if category input need to add in categoryTB
                for(int i=0; i<=list.size()-1; i++) {
                    if(category.equals(list.get(i)))
                        break;
                    if(i==list.size()-1)
                        isNeedToAddToCategory = true;
                }
                if(isNeedToAddToCategory) {
                    db.insertCategory(category);
                    reloadPage();
                }
                Double l = Double.parseDouble(price);
                price = String.valueOf(l);
                if(db.insertExpenses(category, price, dateGlobal)) {
                    dateGlobal = null;
                    selectDateTextView.setText("Select Date");
                    categoryEditText.setText("");
                    expensesEditText.setText("");
                    // MainActivity.chartStart();
                    Toast.makeText(getApplicationContext(),"add success!",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(),"add unsuccessful!",Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void spinner() {
        spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                // This means that the method is called whenever the currently selected item is removed from the list of available items. As the doc describes, this can occur under
                // different circumstances, but generally if the adapter is modified such that the currently selected item is no longer available then the method will be called.

                // This method may be used so that you can set which item will be selected given that the previous item is no longer available. This is instead of letting the spinner
                // automatically select the next item in the list.
            }
            public void onItemSelected(AdapterView<?> parent, View arg1, int position, long arg3) {
                // TODO Auto-generated method stub
                if(!(parent.getItemAtPosition(position).equals("--select categories--"))) {
                    categoryGlobal = parent.getItemAtPosition(position).toString();
                    categoryEditText.setText(categoryGlobal);
                    spinner1.setSelection(0);
                    expensesEditText.requestFocus();
                }
                else
                    return;
            }
        });

        list = db.loadCategory(list);
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter);
    }

    public void reloadPage() {
        overridePendingTransition(0, 0);
        startActivity(new Intent(AddExpenses.this, AddExpenses.class));
        overridePendingTransition(0, 0);
        finish();
    }

    public void init() {
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        categoryEditText = (EditText) findViewById(R.id.categoryEditText);
        selectDateTextView = (TextView) findViewById(R.id.selectDateTextView);
        db = new DBHelper(this);
        addCategoryButton = (Button) findViewById(R.id.addCategoryButton);
        deleteCategoryButton = (Button) findViewById(R.id.deleteCategoryButton);
        addToDbButton = (Button) findViewById(R.id.addToDbButton);
        expensesEditText = (EditText) findViewById(R.id.expensesEditText);
        spinnerLinearLayout = (LinearLayout) findViewById(R.id.spinnerLinearLayout);
        updateCategoryButton = (Button) findViewById(R.id.updateCategoryButton);
        add_expensesLinearLayout = (RelativeLayout) findViewById(R.id.add_expensesLinearLayout);
    }
}
package com.example.smartshopsaver;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartshopsaver.activities.ViewByDateExpense;

//DB Helper class for Expense Management SQL Module
public class DBHelper extends SQLiteOpenHelper{

    Cursor cursor;
    public DBHelper(Context context) {
        super(context, "expenses.db", null, 1);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("create Table categoriesTb(id INTEGER PRIMARY KEY AUTOINCREMENT,category TEXT)");
        db.execSQL("create Table expensesTb(id INTEGER PRIMARY KEY AUTOINCREMENT,category TEXT,price TEXT, date INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        // TODO Auto-generated method stub
        db.execSQL("drop Table if exists categoriesTb");
        db.execSQL("drop Table if exists expensesTb");
        onCreate(db);
    }

    public Boolean insertExpenses(String category, String price, Long date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", category);
        contentValues.put("price", price);
        contentValues.put("date", date);
        long result=db.insert("expensesTb", null, contentValues);
        db.close();
        if(result==-1)
            return false;
        else
            return true;
    }

    public Boolean insertCategory(String category)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", category);
        long result=db.insert("categoriesTb", null, contentValues);
        db.close();
        if(result==-1)
            return false;
        else
            return true;
    }

    public void addIfNoCategory() {
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("Select * from categoriesTb where category = '--select categories--'", null);
        if(cursor.getCount() > 0)
            return;
        else {
            ContentValues contentValues = new ContentValues();
            contentValues.put("category", "--select categories--");
            db.insert("categoriesTb", null, contentValues);
            return;
        }
    }

    public boolean checkIfCategoryExist(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("Select * from categoriesTb where category = ?", new String[] {category});
        if(cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    public boolean deleteCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("Select * from categoriesTb where category = ?", new String[] {category});
        if (cursor.getCount() > 0) {
            long result = db.delete("categoriesTb", "category=?", new String[]{category});
            if (result == -1)
                return false;
            else
                return true;
        }else
            return false;
    }

    public boolean deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("Select * from expensesTb", null);
        if (cursor.getCount() > 0) {
            long result = db.delete("expensesTb","", null);
            if (result == -1)
                return false;
            else
                return true;
        }else
            return false;
    }

    public boolean deleteById(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("Select * from expensesTb where id = ?", new String[] {id});
        if (cursor.getCount() > 0) {
            long result = db.delete("expensesTb", "id=?", new String[]{id});
            if (result == -1)
                return false;
            else
                return true;
        }else
            return false;
    }

    public boolean updateById(Context context, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("Select * from expensesTb where id = ?", new String[] {id});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            int categoryIndex = cursor.getColumnIndexOrThrow("category");
            int priceIndex = cursor.getColumnIndexOrThrow("price");
            int dateIndex = cursor.getColumnIndexOrThrow("date");

            String existingCategory = cursor.getString(categoryIndex);
            String existingPrice = cursor.getString(priceIndex);
            Long existingDate = cursor.getLong(dateIndex);

            Intent intent = new Intent(context, ViewByDateExpense.class);

            // Pass the data as extras to the Intent
            intent.putExtra("category", existingCategory);
            intent.putExtra("price", existingPrice);
            intent.putExtra("date", existingDate);

            // Start the other activity
            context.startActivity(intent);



        } else
            return false;
        return false;
    }

    public boolean changeDataById(String category, String price, Long date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", category);
        contentValues.put("price", price);
        contentValues.put("date", date);
        long result=db.insert("expensesTb", null, contentValues);
        db.close();
        if(result==-1)
            return false;
        else
            return true;
    }

    public ArrayList<String> loadCategory(List<String> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        cursor = db.rawQuery("Select * from categoriesTb", null);
        String s = null;
        while(cursor.moveToNext()){
            s=cursor.getString(1);
            list.add(s);
        }
        cursor.close();
        return (ArrayList<String>) list;
    }

    public Boolean updateuserdata(String category,String categoryNew){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", categoryNew);
        Cursor cursor = DB.rawQuery("Select * from categoriesTb where category = ?", new String[]{category});
        if (cursor.getCount() > 0) {
            long result = DB.update("categoriesTb", contentValues, "category=?", new String[]{category});
            if (result == -1)
                return false;
            else
                return true;
        } else
            return false;
    }

    public Cursor getdata()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from expensesTb Order By date ASC", null);
        return cursor;
    }

    public Cursor getdata2(String date1, String date2)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from expensesTb where date between ? and ? Order By date ASC", new String[] {date1,date2});
        return cursor;
    }
}

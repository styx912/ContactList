package com.dazuoye.contactlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 2;

    // 表名和列名
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";

    // 创建表SQL
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_CONTACTS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_PHONE + " TEXT NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
            Log.d("DatabaseHelper", "Table created successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating table", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
            onCreate(db);
            Log.d("DatabaseHelper", "Database upgraded from " + oldVersion + " to " + newVersion);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error upgrading database", e);
        }
    }

    // 添加联系人
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, contact.getName());
            values.put(COLUMN_PHONE, contact.getPhone());
            db.insert(TABLE_CONTACTS, null, values);
            Log.d("DatabaseHelper", "Contact added: " + contact.getName());
        } finally {
            db.close();
        }
    }

    // 获取所有联系人（按姓名排序）
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] columns = {COLUMN_ID, COLUMN_NAME, COLUMN_PHONE};
            cursor = db.query(TABLE_CONTACTS, columns,
                    null, null, null, null, null); // 不排序

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));

                    contacts.add(new Contact(id, name, phone));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting contacts", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return contacts;
    }
}
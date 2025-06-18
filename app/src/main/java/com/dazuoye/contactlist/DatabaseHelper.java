package com.dazuoye.contactlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 4;

    // 表名和列名（统一管理）
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IS_PINNED = "is_pinned";

    // 预定义所有列（假设表结构已通过升级保证）
    private static final String[] ALL_COLUMNS = {
            COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_IS_PINNED
    };

    // 缓存列存在性状态（通过构造函数初始化）
    private boolean hasPinnedColumn = false;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // 初始化时检查列存在性（单次检查）
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_CONTACTS + ")", null)) {

            if (cursor != null) {
                // 获取 "name" 列的索引（PRAGMA 返回结果的第2列）
                int nameColumnIndex = cursor.getColumnIndex("name");
                if (nameColumnIndex != -1) { // 显式检查索引有效性
                    while (cursor.moveToNext()) {
                        String columnName = cursor.getString(nameColumnIndex);
                        if (COLUMN_IS_PINNED.equals(columnName)) {
                            hasPinnedColumn = true;
                            break;
                        }
                    }
                } else {
                    Log.w("DatabaseHelper", "Column 'name' not found in PRAGMA table_info");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error initializing column cache", e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d("DatabaseHelper", "Table created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            try {
                // 尝试直接添加列（若失败则重建表）
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_IS_PINNED + " INTEGER DEFAULT 0");
                Log.d("DatabaseHelper", "Added pinned column");
            } catch (SQLiteException e) {
                // 列已存在或发生其他错误，重建表
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
                onCreate(db);
                Log.d("DatabaseHelper", "Recreated table with pinned column");
            }
        }
    }

    // 添加联系人（简化 pinned 状态处理）
    public void addContact(Contact contact) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, contact.getName());
            values.put(COLUMN_PHONE, contact.getPhone());
            values.put(COLUMN_IS_PINNED, contact.isPinned() ? 1 : 0);
            db.insert(TABLE_CONTACTS, null, values);
            Log.d("DatabaseHelper", "Contact added: " + contact.getName());
        }
    }

    // 更新联系人
    public boolean updateContact(String oldName, String oldPhone, String newName, String newPhone) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, newName);
            values.put(COLUMN_PHONE, newPhone);

            return db.update(TABLE_CONTACTS, values,
                    COLUMN_NAME + " = ? AND " + COLUMN_PHONE + " = ?",
                    new String[]{oldName, oldPhone}) > 0;
        }
    }

    // 删除联系人
    public boolean deleteContact(String name, String phone) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            return db.delete(TABLE_CONTACTS,
                    COLUMN_NAME + " = ? AND " + COLUMN_PHONE + " = ?",
                    new String[]{name, phone}) > 0;
        }
    }

    // 获取所有联系人（优化列查询）
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String[] columns = hasPinnedColumn ? ALL_COLUMNS : new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE};

        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.query(TABLE_CONTACTS, columns, null, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
                    boolean isPinned = hasPinnedColumn && cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PINNED)) == 1;

                    contacts.add(new Contact(id, name, phone, isPinned));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting contacts", e);
        }
        return contacts;
    }

    // 切换置顶状态
    public boolean togglePinnedStatus(long contactId, boolean isPinned) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_PINNED, isPinned ? 1 : 0);

            return db.update(TABLE_CONTACTS, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(contactId)}) > 0;
        }
    }

    // 创建表语句（私有常量）
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_CONTACTS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_PHONE + " TEXT NOT NULL," +
                    COLUMN_IS_PINNED + " INTEGER DEFAULT 0)";
}
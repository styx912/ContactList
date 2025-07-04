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
    //数据库配置
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 5;

    // 表名和列名（统一管理）
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IS_PINNED = "is_pinned";
    public static final String COLUMN_AVATAR_URI = "avatar_uri";

    //默认不置顶
    private boolean hasPinnedColumn = false;

    // 构造函数中检查is_pinned列是否存在
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_CONTACTS + ")", null)) {
            if (cursor != null) {
                int nameColumnIndex = cursor.getColumnIndex("name");
                if (nameColumnIndex != -1) {
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
        db.execSQL(CREATE_TABLE); //创建新表
        Log.d("DatabaseHelper", "Table created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) { // 升级到版本5
            try {
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_AVATAR_URI + " TEXT"); // 版本5新增头像字段
            } catch (SQLiteException e) {
                // 处理异常
            }
        }
    }

    // 创建表语句（私有常量）
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_CONTACTS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_PHONE + " TEXT NOT NULL," +
                    COLUMN_IS_PINNED + " INTEGER DEFAULT 0," +
                    COLUMN_AVATAR_URI + " TEXT)";


//--------------------------------------------------------------------


    // 添加联系人
    public void addContact(Contact contact) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, contact.getName());
            values.put(COLUMN_PHONE, contact.getPhone());
            values.put(COLUMN_IS_PINNED, contact.isPinned() ? 1 : 0);
            values.put(COLUMN_AVATAR_URI, contact.getAvatarUri());
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
            return db.update(TABLE_CONTACTS, values, COLUMN_NAME + " = ? AND " + COLUMN_PHONE + " = ?",
                    new String[]{oldName, oldPhone}) > 0;  //返回值 > 0 表示至少更新了1条记录
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

    // 获取所有联系人
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String[] columns = hasPinnedColumn ?    //根据hasPinnedColumn标志决定查询哪些列
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_IS_PINNED, COLUMN_AVATAR_URI} :
                //当is_pinned列存在时：查询所有5个字段（ID、姓名、电话、置顶状态、头像URI）
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_AVATAR_URI};

        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.query(TABLE_CONTACTS, columns, null, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
                    boolean isPinned = hasPinnedColumn && cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PINNED)) == 1;
                    String avatarUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_URI));

                    contacts.add(new Contact(id, name, phone, isPinned,avatarUri));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting contacts", e);
        }
        return contacts;
    }

    public Contact getContactById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Contact contact = null;

        Cursor cursor = db.query(TABLE_CONTACTS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_IS_PINNED, COLUMN_AVATAR_URI},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            contact = new Contact(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PINNED)) == 1,
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_URI))
            );
            cursor.close();
        }
        return contact;
    }

    // 切换置顶状态
    public void togglePinnedStatus(long contactId, boolean isPinned) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_PINNED, isPinned ? 1 : 0);

            db.update(TABLE_CONTACTS, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(contactId)});
        }
    }

    // 更新头像
    public boolean updateAvatar(long contactId, String avatarUri) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_AVATAR_URI, avatarUri);
            return db.update(TABLE_CONTACTS, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(contactId)}) > 0;
        }
    }
}
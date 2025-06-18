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
    private static final int DATABASE_VERSION = 4;

    // 表名和列名
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_IS_PINNED = "is_pinned";

    // 创建表SQL
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_CONTACTS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_PHONE + " TEXT NOT NULL," +
                    COLUMN_IS_PINNED + " INTEGER DEFAULT 0)";

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
            if (oldVersion < 4) {
                // 检查表是否存在
                Cursor tableCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TABLE_CONTACTS});
                boolean tableExists = tableCursor != null && tableCursor.moveToFirst();
                if (tableCursor != null) tableCursor.close();

                if (tableExists) {
                    // 检查列是否存在
                    Cursor columnCursor = db.rawQuery("PRAGMA table_info(" + TABLE_CONTACTS + ")", null);
                    boolean columnExists = false;
                    if (columnCursor != null) {
                        // 查找列名在结果集中的索引
                        int nameIndex = columnCursor.getColumnIndex("name");
                        if (nameIndex >= 0) {
                            while (columnCursor.moveToNext()) {
                                String columnName = columnCursor.getString(nameIndex);
                                if (COLUMN_IS_PINNED.equals(columnName)) {
                                    columnExists = true;
                                    break;
                                }
                            }
                        }
                        columnCursor.close();
                    }

                    // 如果列不存在则添加
                    if (!columnExists) {
                        db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_IS_PINNED + " INTEGER DEFAULT 0");
                        Log.d("DatabaseHelper", "Added pinned column");
                    }
                } else {
                    // 如果表不存在，重新创建
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
                    onCreate(db);
                    Log.d("DatabaseHelper", "Recreated table with pinned column");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error upgrading database", e);
            // 如果升级失败，重建表
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
                onCreate(db);
                Log.d("DatabaseHelper", "Recreated table after upgrade failure");
            } catch (Exception ex) {
                Log.e("DatabaseHelper", "Error recreating table", ex);
            }
        }
    }

    // 添加联系人
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, contact.getName());
            values.put(COLUMN_PHONE, contact.getPhone());
            values.put(COLUMN_IS_PINNED, contact.isPinned() ? 1 : 0); // 添加置顶状态
            db.insert(TABLE_CONTACTS, null, values);
            Log.d("DatabaseHelper", "Contact added: " + contact.getName());
        } finally {
            db.close();
        }
    }

    // 更新联系人
    public boolean updateContact(String oldName, String oldPhone, String newName, String newPhone) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, newName);
            values.put(COLUMN_PHONE, newPhone);

            int rowsAffected = db.update(TABLE_CONTACTS, values,
                    COLUMN_NAME + " = ? AND " + COLUMN_PHONE + " = ?",
                    new String[]{oldName, oldPhone});

            return rowsAffected > 0;
        } finally {
            db.close();
        }
    }

    // 删除联系人
    public boolean deleteContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_CONTACTS,
                    COLUMN_NAME + " = ? AND " + COLUMN_PHONE + " = ?",
                    new String[]{name, phone});

            return rowsDeleted > 0;
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
            // 检查列是否存在
            boolean hasPinnedColumn = false;
            Cursor infoCursor = db.rawQuery("PRAGMA table_info(" + TABLE_CONTACTS + ")", null);
            if (infoCursor != null) {
                // 查找列名在结果集中的索引
                int nameIndex = infoCursor.getColumnIndex("name");
                if (nameIndex >= 0) {
                    while (infoCursor.moveToNext()) {
                        String columnName = infoCursor.getString(nameIndex);
                        if (COLUMN_IS_PINNED.equals(columnName)) {
                            hasPinnedColumn = true;
                            break;
                        }
                    }
                }
                infoCursor.close();
            }

            // 构建查询列
            String[] columns;
            if (hasPinnedColumn) {
                columns = new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_IS_PINNED};
            } else {
                columns = new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PHONE};
            }

            cursor = db.query(TABLE_CONTACTS, columns,
                    null, null, null, null, null); // 不排序

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));

                    boolean isPinned = false;
                    if (hasPinnedColumn) {
                        try {
                            int pinnedIndex = cursor.getColumnIndex(COLUMN_IS_PINNED);
                            if (pinnedIndex != -1) {
                                isPinned = cursor.getInt(pinnedIndex) == 1;
                            }
                        } catch (Exception e) {
                            Log.e("DatabaseHelper", "Error reading pinned status", e);
                        }
                    }

                    contacts.add(new Contact(id, name, phone, isPinned));
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

    // 添加置顶状态更新方法
    public boolean togglePinnedStatus(long contactId, boolean isPinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_PINNED, isPinned ? 1 : 0);

            int rowsAffected = db.update(TABLE_CONTACTS, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(contactId)});

            return rowsAffected > 0;
        } finally {
            db.close();
        }
    }
}
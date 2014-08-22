package com.maxedapps.shopper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Max on 22.07.2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "db_shoppinglist";
    private static final String TABLE_LISTS = "tablists";
    private static final String TABLE_ITEMS = "tabitems";
    private static final int DATABASE_VERSION = 2;
    private static final String KEY_ID = "id";
    private static final String KEY_WEB_ID = "web_id";
    private static final String KEY_CONNECTED_USERS = "connected_users";
    private static final String KEY_LIST_STATE = "list_state";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LIST_NAME = "list_name";
    private static final String CREATE_DATABASE_PART_LISTS = "CREATE TABLE " + TABLE_LISTS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_LIST_NAME + " TEXT NOT NULL, " + KEY_WEB_ID + " INTEGER, " + KEY_CONNECTED_USERS + " INTEGER, " + KEY_LIST_STATE + " INTEGER, " + KEY_PASSWORD + " TEXT);";
    private static final String KEY_ITEM_NAME = "item_name";
    private static final String KEY_ITEM_CHECKED = "item_status";
    private static final String KEY_LIST_ID = "list_id";
    private static final String CREATE_DATABASE_PART_ITEMS = "CREATE TABLE " + TABLE_ITEMS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_ITEM_NAME + " TEXT NOT NULL, " + KEY_ITEM_CHECKED + " INTEGER, " + KEY_LIST_ID + " INTEGER, FOREIGN KEY (" + KEY_LIST_ID + ") REFERENCES " + TABLE_LISTS + "(" + KEY_ID + ") ON DELETE CASCADE);";
    private static final int LIST_STATE_ADDED = 0;
    private static final int LIST_STATE_JOINED = 1;
    private static DBHelper INSTANCE;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DBHelper(context);
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DATABASE_PART_LISTS);
        sqLiteDatabase.execSQL(CREATE_DATABASE_PART_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onOpen(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON");
    }

    public void addList(ShoppingList newList) {
        SQLiteDatabase db = this.getWritableDatabase();
        long lastListId = 0;
        ContentValues cv = new ContentValues();
        cv.put(KEY_LIST_NAME, newList.getName());
        cv.put(KEY_WEB_ID, newList.getWebId());
        cv.put(KEY_CONNECTED_USERS, newList.getTotalUsers());
        cv.put(KEY_LIST_STATE, newList.getListState());
        cv.put(KEY_PASSWORD, newList.getPassword());
        long error = db.insert(TABLE_LISTS, null, cv);

        Cursor c = db.query(TABLE_LISTS, new String[]{KEY_ID}, null, null, null, null, KEY_ID + " DESC", "1");
        if (c != null && c.moveToFirst()) {
            lastListId = c.getLong(0); //The 0 is the column index, we only have 1 column, so the index is 0
        }
        newList.setLocalDBKey(lastListId);

        long lastItemId = 0;
        c = db.query(TABLE_ITEMS, new String[]{KEY_ID}, null, null, null, null, KEY_ID + " DESC", "1");
        if (c != null && c.moveToFirst()) {
            lastItemId = c.getLong(0) + 1; //The 0 is the column index, we only have 1 column, so the index is 0
        }

        // Item ID Zuweisung ist hier vermutlich fehlerhaft. Aber überhaupt nötig?

        for (ListItem item : newList.getItems()) {
            cv.clear();
            cv.put(KEY_ITEM_NAME, item.getName());
            cv.put(KEY_ITEM_CHECKED, item.isChecked() ? 1 : 0);
            cv.put(KEY_LIST_ID, lastListId);
            db.insert(TABLE_ITEMS, null, cv);
            item.setLocalDBKey(lastItemId);
            lastItemId++;
        }

    }

    public void updateList(ShoppingList updatedList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_LIST_NAME, updatedList.getName());
        cv.put(KEY_WEB_ID, updatedList.getWebId());
        cv.put(KEY_CONNECTED_USERS, updatedList.getTotalUsers());
        cv.put(KEY_LIST_STATE, updatedList.getListState());
        cv.put(KEY_PASSWORD, updatedList.getPassword());
        db.update(TABLE_LISTS, cv, KEY_ID + " = ?", new String[]{String.valueOf(updatedList.getLocalDBKey())});

        long listId = updatedList.getLocalDBKey();
        long lastItemId = 0;
        Cursor c = db.query(TABLE_ITEMS, new String[]{KEY_ID}, null, null, null, null, KEY_ID + " DESC", "1");
        if (c != null && c.moveToFirst()) {
            lastItemId = c.getLong(0) + 1; //The 0 is the column index, we only have 1 column, so the index is 0
        }

        for (ListItem item : updatedList.getItems()) {
            cv.clear();
            cv.put(KEY_ITEM_NAME, item.getName());
            cv.put(KEY_ITEM_CHECKED, item.isChecked() ? 1 : 0);
            cv.put(KEY_LIST_ID, listId);
            db.insert(TABLE_ITEMS, null, cv);
            item.setLocalDBKey(lastItemId);
            lastItemId++;
        }

    }

    public void updateOrAddList(ShoppingList list) {
        if (list.getLocalDBKey() != 0) {
            this.clearItems(list.getLocalDBKey());
            this.updateList(list);
        } else
            this.addList(list);
    }

    public void deleteList(long listId) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_LISTS, KEY_ID + " = ?", new String[]{String.valueOf(listId)});

    }

    public void deleteAllLists(long[] exceptionIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder sb = new StringBuilder();
        for (long exceptionId : exceptionIds) {
            sb.append("'" + exceptionId + "', ");
        }
        sb.delete(sb.length()-2, sb.length());
        db.delete(TABLE_LISTS, KEY_WEB_ID + " NOT IN (" + sb + ")", null);
    }

    public void connectUser(int listId) {

    }

    public void changeSyncState(int listId, int syncState) {

    }

    public void setPassword(int listId, String password) {

    }

    public void setWebId(long listId, long webId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_WEB_ID, webId);
        db.update(TABLE_LISTS, cv, KEY_ID + " = ?", new String[]{String.valueOf(listId)});

    }

    public ArrayList<ShoppingList> getLists() {
        ArrayList<ShoppingList> results = new ArrayList<ShoppingList>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_LISTS, new String[]{KEY_ID, KEY_LIST_NAME, KEY_CONNECTED_USERS, KEY_WEB_ID, KEY_LIST_STATE, KEY_PASSWORD}, null, null, null, null, null);
        while (c.moveToNext()) {
            ShoppingList list = new ShoppingList(c.getString(c.getColumnIndex(KEY_LIST_NAME)));
            list.setLocalDBKey(c.getInt(c.getColumnIndex(KEY_ID)));
            list.setWebId(c.getInt(c.getColumnIndex(KEY_WEB_ID)));
            list.setPassword(c.getString(c.getColumnIndex(KEY_PASSWORD)));
            list.setTotalUsers(c.getInt(c.getColumnIndex(KEY_CONNECTED_USERS)));
            list.setListState(c.getInt(c.getColumnIndex(KEY_LIST_STATE)));
            ArrayList<ListItem> items = this.getItems(c.getInt(c.getColumnIndex(KEY_ID)));
            for (ListItem item : items) {
                list.addItem(item);
            }
            results.add(list);
        }
        return results;
    }

    public ShoppingList getListByLocalId(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_LISTS, new String[]{KEY_ID, KEY_LIST_NAME, KEY_CONNECTED_USERS, KEY_WEB_ID, KEY_LIST_STATE, KEY_PASSWORD}, KEY_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToNext()) {
            ShoppingList list = new ShoppingList(c.getString(c.getColumnIndex(KEY_LIST_NAME)));
            list.setLocalDBKey(c.getInt(c.getColumnIndex(KEY_ID)));
            list.setWebId(c.getInt(c.getColumnIndex(KEY_WEB_ID)));
            list.setPassword(c.getString(c.getColumnIndex(KEY_PASSWORD)));
            list.setTotalUsers(c.getInt(c.getColumnIndex(KEY_CONNECTED_USERS)));
            list.setListState(c.getInt(c.getColumnIndex(KEY_LIST_STATE)));
            ArrayList<ListItem> items = this.getItems(c.getInt(c.getColumnIndex(KEY_ID)));
            for (ListItem item : items) {
                list.addItem(item);
            }
            return list;
        }
        return null;
    }

    public ShoppingList getListByWebId(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_LISTS, new String[]{KEY_ID, KEY_LIST_NAME, KEY_CONNECTED_USERS, KEY_WEB_ID, KEY_LIST_STATE, KEY_PASSWORD}, KEY_WEB_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToNext()) {
            ShoppingList list = new ShoppingList(c.getString(c.getColumnIndex(KEY_LIST_NAME)));
            list.setLocalDBKey(c.getInt(c.getColumnIndex(KEY_ID)));
            list.setWebId(c.getInt(c.getColumnIndex(KEY_WEB_ID)));
            list.setPassword(c.getString(c.getColumnIndex(KEY_PASSWORD)));
            list.setTotalUsers(c.getInt(c.getColumnIndex(KEY_CONNECTED_USERS)));
            list.setListState(c.getInt(c.getColumnIndex(KEY_LIST_STATE)));
            ArrayList<ListItem> items = this.getItems(c.getInt(c.getColumnIndex(KEY_ID)));
            for (ListItem item : items) {
                list.addItem(item);
            }
            return list;
        }
        return null;
    }

    public ArrayList<ListItem> getItems(long listId) {
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_ITEMS, new String[]{KEY_ID, KEY_ITEM_NAME, KEY_ITEM_CHECKED}, KEY_LIST_ID + " = ?", new String[]{String.valueOf(listId)}, null, null, null);
        while (c.moveToNext()) {
            ListItem item = new ListItem(c.getString(c.getColumnIndex(KEY_ITEM_NAME)), c.getInt(c.getColumnIndex(KEY_ID)));
            item.setChecked(c.getInt(c.getColumnIndex(KEY_ITEM_CHECKED)) == 1);
            results.add(item);
        }
        return results;
    }

    public void checkItems(ArrayList<ListItem> listItems) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (ListItem item : listItems) {
            cv.put(KEY_ITEM_CHECKED, item.isChecked() ? 1 : 0);
            db.update(TABLE_ITEMS, cv, KEY_ID + " = ?", new String[]{String.valueOf(item.getLocalDBKey())});
        }

    }

    public void clearItems(long listId) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_ITEMS, KEY_LIST_ID + " = ?", new String[]{String.valueOf(listId)});

    }

}

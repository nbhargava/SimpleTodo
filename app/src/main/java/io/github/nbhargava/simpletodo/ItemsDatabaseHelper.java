package io.github.nbhargava.simpletodo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikhil on 8/16/15.
 */
public class ItemsDatabaseHelper extends SQLiteOpenHelper {
    // Singleton
    private static ItemsDatabaseHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "itemsDatabase";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_ITEMS = "items";

    // Table Columns
    private static final String KEY_ITEM_ID = "id";
    private static final String KEY_ITEM = "item";

    private static final String TAG = "ItemsDatabaseHelper";

    public static synchronized ItemsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new ItemsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Constructor is private
    private ItemsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS +
                "(" +
                KEY_ITEM_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_ITEM + " TEXT " +
                ")";

        db.execSQL(CREATE_ITEMS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }
    }

    public List<TodoItem> getAllItems() {
        List<TodoItem> items = new ArrayList<TodoItem>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s", TABLE_ITEMS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(KEY_ITEM_ID));
                    String item = cursor.getString(cursor.getColumnIndex(KEY_ITEM));

                    TodoItem todoItem = new TodoItem(id, item);
                    items.add(todoItem);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get items from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return items;
    }

    // Returns the newly added TodoItem or null if the operation failed
    public TodoItem addTodoItem(String item) {
        SQLiteDatabase db = getWritableDatabase();
        TodoItem newItem = null;

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // We're okay with duplicate items in the list
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM, item);

            long rowId = db.insertOrThrow(TABLE_ITEMS, null, values);
            if (rowId != -1) {
                newItem = new TodoItem(rowId, item);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add item to database");
        } finally {
            db.endTransaction();
        }

        return newItem;
    }

    public TodoItem editTodoItem(TodoItem todoItem, String newItem) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM, newItem);

            int rows = db.update(TABLE_ITEMS, values, KEY_ITEM_ID + "= ?", new String[] {Long.toString(todoItem.getId())});
            if (rows == 0) {
                long rowId = db.insertOrThrow(TABLE_ITEMS, null, values);
                todoItem = new TodoItem(rowId, newItem);
            } else {
                todoItem.setItem(newItem);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to update item in database");
        } finally {
            db.endTransaction();
        }

        return todoItem;
    }

    public void deleteTodoItem(TodoItem todoItem) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            int rowsDeleted = db.delete(TABLE_ITEMS, KEY_ITEM_ID + "= ?", new String[] {Long.toString(todoItem.getId())});
            if (rowsDeleted == 1) {
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete item from database");
        } finally {
            db.endTransaction();
        }
    }


}

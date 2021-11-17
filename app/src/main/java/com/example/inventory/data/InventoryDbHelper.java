package com.example.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.inventory.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_SUPPLIER + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_IMAGE + " BLOB"
                + ");";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //_//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\_\\
        //_\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//_\\
        //_//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\_\\
        //_\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//_\\
        //_//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\_\\
        //_\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//_\\
        //_//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\//||\\_\\
        //_\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//\\||//_\\
    }
}
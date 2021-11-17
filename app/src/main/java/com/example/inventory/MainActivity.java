package com.example.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventory.data.InventoryContract.InventoryEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    InventoryCursorAdapter inventoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            startActivity(intent);
        });

        ListView listView = findViewById(R.id.list);
        inventoryAdapter = new InventoryCursorAdapter(this, null);

        listView.setEmptyView(findViewById(R.id.empty_view));
        listView.setAdapter(inventoryAdapter);
        listView.setOnItemClickListener((l1stView, itemView, position, id) -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.setData(Uri.withAppendedPath(InventoryEntry.CONTENT_URI, String.valueOf(id)));
            startActivity(intent);
        });

        getLoaderManager().initLoader(0, null, this);
    }

    private void insertItem() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.raider);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, "MSI GE75 10SGS-047 Raider");
        values.put(InventoryEntry.COLUMN_PRICE, 219999);
        values.put(InventoryEntry.COLUMN_QUANTITY, 99);
        values.put(InventoryEntry.COLUMN_SUPPLIER, "APV Technology Co., Ltd.");
        values.put(InventoryEntry.COLUMN_IMAGE, DbBitmapUtility.getBitmapAsArray(bitmap));

        Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (uri == null) Toast.makeText(this, R.string.item_save_fail, Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, R.string.item_save_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.empty_inventory_msg);

        builder.setPositiveButton(R.string.remove, (dialog, id) -> deleteAllItems());

        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllItems() {
        int rowsDel = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);

        if (rowsDel != 0) {
            Toast.makeText(this, R.string.items_delete_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.items_delete_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[] {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        inventoryAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryAdapter.swapCursor(null);
    }
}
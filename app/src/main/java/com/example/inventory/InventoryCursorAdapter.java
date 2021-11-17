package com.example.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventory.data.InventoryContract.InventoryEntry;

import java.util.concurrent.atomic.AtomicInteger;

public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        TextView priceTextView = view.findViewById(R.id.price);
        Button sellButton = view.findViewById(R.id.sell_button);

        long id = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));
        long priceInCents = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE));
        String price = String.format("$%.2f", priceInCents / 100.00);

        sellButton.setTag(id);

        nameTextView.setText(name);
        quantityTextView.setText(String.valueOf(quantity));
        priceTextView.setText(price);
        sellButton.setOnClickListener((v1ew) -> {
            Uri currentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, (long) v1ew.getTag());
            Cursor currentCursor = context.getContentResolver().query(currentUri, null,
                    null, null, null);
            if (currentCursor.moveToNext()) {
                ContentValues values = new ContentValues();
                long quant1ty = currentCursor.getLong(currentCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));
                if (quant1ty < 1) {
                    Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
                } else {
                    values.put(InventoryEntry.COLUMN_QUANTITY, quant1ty - 1);
                    context.getContentResolver().update(currentUri, values, null, null);
                }
            } currentCursor.close();
        });
    }
}
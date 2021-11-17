package com.example.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.inventory.data.InventoryContract.InventoryEntry;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText nameText;
    private EditText quantityText;
    private EditText supplierText;
    private EditText priceText;
    private ImageView imageView;
    private TextView imageHint;

    private ActivityResultLauncher<Intent> setImage;
    private Bitmap currentBitmap;
    private Uri currentUri = null;
    private boolean mPetHasChanged = false;
    private final View.OnTouchListener touchListener = (view, motionEvent) -> {
        mPetHasChanged = true;
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (result) -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        if (bundle == null) {
                            try {
                                currentBitmap = MediaStore.Images.Media.getBitmap(
                                        getContentResolver(),
                                        result.getData().getData());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            currentBitmap = (Bitmap) bundle.get("data");
                        }
                        imageView.setImageBitmap(currentBitmap);
                        imageHint.setVisibility(View.GONE);
                    }
                });

        nameText = findViewById(R.id.product_name);
        quantityText = findViewById(R.id.product_quantity);
        priceText = findViewById(R.id.product_price);
        supplierText = findViewById(R.id.product_supplier);
        imageView = findViewById(R.id.image_view);
        imageHint = findViewById(R.id.image_hint);
        TextView header = findViewById(R.id.header);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri == null) {
            setTitle(R.string.add_item);
            invalidateOptionsMenu();

            LinearLayout imageLayout = findViewById(R.id.image_layout);
            imageLayout.setOnClickListener((view) -> onImageChoose());
            LinearLayout orderLayout = findViewById(R.id.order_layout);
            orderLayout.setVisibility(View.GONE);
            header.setText(R.string.label_description);
        } else {
            setTitle(R.string.edit_item);
            currentUri = uri;

            Cursor cursor = getContentResolver().query(currentUri, null, null,
                    null, null);
            cursor.moveToNext();
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME));
            String supplier = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_SUPPLIER));
            cursor.close();

            header.setTypeface(header.getTypeface(), Typeface.BOLD);
            header.setText(itemName);
            imageHint.setText(R.string.click_to_change_photo);
            imageHint.setOnClickListener((view) -> onImageChoose());
            findViewById(R.id.name_layout).setVisibility(View.GONE);

            TextView amountOfGoods = findViewById(R.id.how_much_order);
            Button orderButton = findViewById(R.id.order_button);

            orderButton.setOnClickListener((view) -> {
                if (!TextUtils.isEmpty(supplier)) {
                    int quantity;
                    try {
                        quantity = Integer.parseInt(amountOfGoods.getText().toString());
                    } catch (NumberFormatException ne) {
                        Toast.makeText(this, R.string.quantity_prompt, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (quantity == 0) {
                        Toast.makeText(this, R.string.cant_order_zero, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent int3nt = new Intent(Intent.ACTION_SEND);
                    int3nt.setType("*/*");
                    int3nt.putExtra(Intent.EXTRA_SUBJECT, "Order for " + supplier);
                    int3nt.putExtra(Intent.EXTRA_TEXT, getString(R.string.product_name) + ": " +
                            itemName + "\n" +
                            getString(R.string.quantity) + ": " +
                            quantity +
                            "\n\nSleep well!");
                    if (int3nt.resolveActivity(getPackageManager()) != null) {
                        startActivity(int3nt);
                    }
                } else {
                    Toast.makeText(this, R.string.unknown_supplier, Toast.LENGTH_LONG).show();
                }
            });

            getLoaderManager().initLoader(0, null, this);
        }

        nameText.setOnTouchListener(touchListener);
        quantityText.setOnTouchListener(touchListener);
        supplierText.setOnTouchListener(touchListener);
        priceText.setOnTouchListener(touchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = (dialogInterface, i) -> {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void saveItem() {
        String name = nameText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.item_requires_name, Toast.LENGTH_SHORT).show();
            return;
        }
        String quantity = quantityText.getText().toString().trim();

        String supplier = supplierText.getText().toString().trim();

        String priceStr = priceText.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, R.string.price_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        int priceInCents = (int) Math.round(Double.parseDouble(priceStr) * 100);

        byte[] bitmapArray = null;
        if (currentBitmap != null) {
            bitmapArray = DbBitmapUtility.getBitmapAsArray(currentBitmap);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, name);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER, supplier);
        values.put(InventoryEntry.COLUMN_PRICE, priceInCents);
        values.put(InventoryEntry.COLUMN_IMAGE, bitmapArray);

        if (currentUri == null) {
            Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (uri == null) Toast.makeText(this, R.string.item_save_fail, Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, R.string.item_save_success, Toast.LENGTH_SHORT).show();
        } else {
            int rowsUpd = getContentResolver().update(currentUri, values, null, null);
            if (rowsUpd == 0) Toast.makeText(this, R.string.item_save_fail, Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, R.string.item_save_success, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void deleteItem() {
        if (currentUri != null) {
            int rowsDel = getContentResolver().delete(currentUri, null, null);
            if (rowsDel != 0) {
                Toast.makeText(this, R.string.items_delete_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.items_delete_fail, Toast.LENGTH_SHORT).show();
            }
        } finish();
    };

    private void onImageChoose() {
        final String[] options = { getString(R.string.option_take_photo),
                getString(R.string.option_choose_from_gallery),
                getString(R.string.cancel) };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_photo));
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals(getString(R.string.option_take_photo))) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                setImage.launch(intent);
            } else if (options[item].equals(getString(R.string.option_choose_from_gallery))) {
                Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                setImage.launch(intent);
            } else if (options[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener quitButtonClickListener = (dialogInterface, i) -> finish();
        showUnsavedChangesDialog(quitButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener quitButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.quit, quitButtonClickListener);
        builder.setNegativeButton(R.string.stay, (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.remove_item_msg);
        builder.setPositiveButton(R.string.remove, (dialog, id) -> {
            deleteItem();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                currentUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToNext()) {
            nameText.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME)));
            quantityText.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY)));

            long priceInCents = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE));
            String price = String.valueOf(priceInCents / 100.00);
            priceText.setText(price);

            supplierText.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_SUPPLIER)));

            byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_IMAGE));
            if (bitmapArray != null) {
                currentBitmap = DbBitmapUtility.getArrayAsBitmap(bitmapArray);
                imageView.setImageBitmap(currentBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameText.setText(null);
        quantityText.setText(null);
        priceText.setText(String.valueOf(0));
        supplierText.setText(null);
        imageView.setImageBitmap(null);
    }
}
package com.example.android.inventoryapp;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

/**
 * Allows user to create a new inventory or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    /**
     * EditText field to enter the product name
     */
    private EditText mEditName;

    /**
     * EditText field to enter the author name
     */
    private EditText mEditAuthor;

    /**
     * EditText field to enter the supplier
     */
    private EditText mEditSupplier;

    /**
     * EditText field to enter the phone
     */
    private EditText mEditPhone;

    /**
     * EditText field to enter the price
     */
    private EditText mEditPrice;

    /**
     * EditText field to enter the Quantity
     */
    private EditText mEditQuantity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mEditName = (EditText) findViewById(R.id.edit_product_name);
        mEditAuthor = (EditText) findViewById(R.id.edit_author);
        mEditSupplier = (EditText) findViewById(R.id.edit_supplier);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditPrice = (EditText) findViewById(R.id.edit_price);
        mEditQuantity = (EditText) findViewById(R.id.edit_quantity);

    }


    // Get user input from the editor and save new data into database

    private void insertInventory() {
        //Read from input fields
        //Use trim to eliminate leading or trailing white space
        String nameString = mEditName.getText().toString().trim();
        String authorString = mEditAuthor.getText().toString().trim();
        String supplierString = mEditSupplier.getText().toString().trim();
        String phoneString = mEditPhone.getText().toString().trim();
        int phone = Integer.parseInt(phoneString);
        String quantityString = mEditQuantity.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String priceString = mEditPrice.getText().toString().trim();
        int price = Integer.parseInt(priceString);


        // Create a ContentValues object where column names are the keys,
        // and inventory attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryEntry.COLUMN_AUTHOR, authorString);
        values.put(InventoryEntry.COLUMN_SUPPLIER, supplierString);
        values.put(InventoryEntry.COLUMN_PHONE, phone);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);

        // Insert a new pet into the provider, returning the content URI for the new pet.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                    Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save inventory to database
                insertInventory();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
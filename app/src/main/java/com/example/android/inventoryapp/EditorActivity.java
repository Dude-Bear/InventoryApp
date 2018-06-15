package com.example.android.inventoryapp;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
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

    private void insertPet() {
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


        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and inventory attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryEntry.COLUMN_AUTHOR, authorString);
        values.put(InventoryEntry.COLUMN_SUPPLIER, supplierString);
        values.put(InventoryEntry.COLUMN_PHONE, phone);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the inventory table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        if (newRowId == -1) {
            Toast.makeText(this, "Error with saving inventory", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Inventory saved with row ID; " + newRowId, Toast.LENGTH_SHORT).show();
        }

        Log.v("CatalogActivity", "New row ID: " + newRowId);


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
                insertPet();
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
package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Displays list of inventory that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

    }

    //Show CatalogActivity with solution code:
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the inventory database.
     */
    private void displayDatabaseInfo() {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_AUTHOR,
                InventoryEntry.COLUMN_SUPPLIER,
                InventoryEntry.COLUMN_PHONE,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY
        };

        //Perform a query on the provider using the ContentResolver.
        //Use the {@InventoryEntry.CONTENT_URI} to access the inventory data.
        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI ,projection,
                null, null, null );


        TextView displayView = (TextView) findViewById(R.id.text_view_inventory);

        try {
            // Create a header in the Text View that looks like this:
            //
            // The inventory table contains <number of rows in Cursor> inventory.
            // _id - name - author - supplier - phone - price - quantity
            //
            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText("The inventory table contains " + cursor.getCount() + " inventories.\n\n");
            displayView.append(InventoryEntry._ID + " - " +
                    InventoryEntry.COLUMN_NAME + " - " +
                    InventoryEntry.COLUMN_AUTHOR + " - " +
                    InventoryEntry.COLUMN_SUPPLIER + " - " +
                    InventoryEntry.COLUMN_PHONE + " - " +
                    InventoryEntry.COLUMN_PRICE + " - " +
                    InventoryEntry.COLUMN_QUANTITY + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int authorColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_AUTHOR);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentAuthor = cursor.getString(authorColumnIndex);
                String currentSupplier = cursor.getString(supplierColumnIndex);
                int currentPhone = cursor.getInt(phoneColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);

                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentAuthor + " - " +
                        currentSupplier + " - " +
                        currentPhone + " - " +
                        currentPrice + " - " +
                        currentQuantity));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    /**
     * Helper method to insert hardcoded inventory data into the database. For debugging purposes only.
     */
    private void insertInventory() {
        // Create a ContentValues object where column names are the keys,
        // and Baum`s attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, "Baum");
        values.put(InventoryEntry.COLUMN_AUTHOR, "Moser");
        values.put(InventoryEntry.COLUMN_SUPPLIER, "Amazon");
        values.put(InventoryEntry.COLUMN_PHONE, 121212);
        values.put(InventoryEntry.COLUMN_PRICE, 9);
        values.put(InventoryEntry.COLUMN_QUANTITY, 45);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertInventory();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
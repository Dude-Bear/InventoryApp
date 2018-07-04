package com.example.android.inventoryapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Allows user to create a new inventory or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks <Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    //Identifier for the inventory data loader
    private static final int EXISTING_INVENTORY_LOADER = 0;

    //Content URI for the existing inventory (null if it's a new inventory)
    private  Uri mCurrentInventoryUri;

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
     * Button to call the phone number of the supplier
     */
    private Button mPhoneCallButton;

    /**
     * TextView field to enter the Quantity
     */
    private TextView mEditQuantity;

    /**
     * Minus Button to reduce the quantity
     */
    private Button mMinusButton;

    /**
     * Plus button to increase the Quantity
     */
    private Button mPlusButton;

    /**
     * Start value for the quantity
     */
    private int quantity = 0;

    /**
     * Uri of the image
     */
    private Uri mInventoryImageUri;

    private Uri mUri = Uri.parse("");

    private static final int PICK_IMAGE_REQUEST = 0;

    /**
     * ImageView for the inventory object
     */
    private ImageView mImageView;

    /**
     * Button to add an Image
     */
    private Button mAddImageButton;

    // Boolean flag that keeps track of wether the inventory has been edited (true) or not (false)
    private boolean mInventoryHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying the viee,
    // and we change the mInventoryHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Use getIntent() and getData to get the associated URI
        // Examine the intent that was used to launch this activity
        // in order to figure out if we are creating a new inventory or editing an existing one.
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        // If the intent does not contain a inventory URI, then we know that we are
        // creating a new inventory
        if (mCurrentInventoryUri == null){
            //This is a new inventory, so change the app baar to say "Add inventory"
            setTitle(getString(R.string.add_inventory));

            //Invalidate the otions menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete inventory that hasn't been created yer.)
            invalidateOptionsMenu();

        } else {
            //Otherwise this is an existing inventory, so change app bat to say "Edit Pet"
            setTitle(getString(R.string.edit_inventory));

            //Initialize a loader to read the inventory data from the database
            //and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        // Find all relevant views
        mEditName = (EditText) findViewById(R.id.edit_product_name);
        mEditAuthor = (EditText) findViewById(R.id.edit_author);
        mEditSupplier = (EditText) findViewById(R.id.edit_supplier);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditPrice = (EditText) findViewById(R.id.edit_price);
        mEditQuantity = (TextView) findViewById(R.id.edit_quantity);
        mAddImageButton = (Button) findViewById(R.id.add_image_button);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mMinusButton = (Button) findViewById(R.id.minus_button);
        mPlusButton = (Button) findViewById(R.id.plus_button);
        mPhoneCallButton = (Button) findViewById(R.id.phone_call_button);


        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOneToQuantity(view);
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneOfQuantity(view);
            }
        });

        mPhoneCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhoneCallIntent();
            }
        });

        //Setup OnTouchListeners on all the input fields, so we can determine if the user has touched ord
        // modified them. This will let us know if there are unsaved changes
        // or nor, if the user tries to leave the editor without saving.
        mEditName.setOnTouchListener(mTouchListener);
        mEditAuthor.setOnTouchListener(mTouchListener);
        mEditSupplier.setOnTouchListener(mTouchListener);
        mEditPhone.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mEditPrice.setOnTouchListener(mTouchListener);
        mAddImageButton.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);

    }

    // Get user input from the editor and save  data into database

    private void saveInventory() {
        //Read from input fields
        //Use trim to eliminate leading or trailing white space
        String nameString = mEditName.getText().toString().trim();
        String authorString = mEditAuthor.getText().toString().trim();
        String supplierString = mEditSupplier.getText().toString().trim();
        String phoneString = mEditPhone.getText().toString().trim();
        String quantityString = mEditQuantity.getText().toString().trim();
        String priceString = mEditPrice.getText().toString().trim();
        String uriString;

        if (mUri.toString().isEmpty()) {
            uriString = "drawable://" + R.drawable.ic_empty_inventory;
        } else {
            uriString = mUri.toString();
        }

        //Check if this is supposed to be a new inventory
        // and check if all the fields in the editor are blank
        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(authorString) &&
                TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(phoneString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(uriString)) {return;}

        // Create a ContentValues object where column names are the keys,
        // and inventory attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryEntry.COLUMN_AUTHOR, authorString);
        values.put(InventoryEntry.COLUMN_SUPPLIER, supplierString);
        values.put(InventoryEntry.COLUMN_IMAGE, uriString);
        // If the phone number is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int phone = 0;
        if (!TextUtils.isEmpty(phoneString)) {
            phone = Integer.parseInt(phoneString);
        }
        values.put(InventoryEntry.COLUMN_PHONE, phone);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryEntry.COLUMN_PRICE, price);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);

        //Determine if this is a new or existing inventory by checking if mCurrentInventoryUri is null or not
        if (mCurrentInventoryUri == null){
            //This is a new inventory, so insert a new inventory in the provider
            // returning the content URI for the new inventory.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            //Show a toast message depending on whether or not the isertion was successful.
            if (newUri == null) {
                //If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, (R.string.editor_insert_inventory_failed), Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, (R.string.editor_insert_inventory_successful), Toast.LENGTH_SHORT).show();
            }

        } else {
            //Otherwise this is an existing inventory, so update the inventory with content URI: mCurrentInventoryUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentInventoryUri will already identify the correct row in the database that
            // we want to modify
            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            //Show a toast message dependeing on whether or not the update was successful.
            if (rowsAffected == 0) {
                //If there are no rows affected, then there was an error with the update.
                Toast.makeText(this, R.string.inventory_update_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.inventory_update_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    //This method is called after invalidateOptionsMenu(), so that the
    //menu can be updated (some menu items can be hidden or made visible).
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onCreateOptionsMenu(menu);
        // If this is a new inventory, hide the "Delete" menu item.
        if (mCurrentInventoryUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save inventory to database
                saveInventory();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                //Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //If the inventory hasn't changed, continue with novigating up to parent activity
                //wich is the {@link CatalogActivity}.
                if(!mInventoryHasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved chages, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that changes should ve discarded.
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //User clicked "Discard" button, nacigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                //Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public  void onBackPressed(){
        //If the inventory hasn't changed, continue with handling back button press
        if(!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        //Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //User clicked "Discard" button, close the current activity.
                finish();
            }
        };

        //Show dialog tht there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Since the editor shows all inventory attributes, define a projection that contains
        // all columns from the inventory table
        // (Android documentation says for projection: "//The array
        // of columns to return (pass null to get all)". Since we need all attributes we may omit this
        // snippet and pass null directly when we return a new CursorLoader.)
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_AUTHOR,
                InventoryEntry.COLUMN_SUPPLIER,
                InventoryEntry.COLUMN_PHONE,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_IMAGE};

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       //Parent activity context
                mCurrentInventoryUri,               //Query the content URI for the current inventory
                projection,                         //Columns to include the resulting Cursor
                null,                      //No selection clause
                null,                   //No selection arguments
                null);                     // No selection sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1){
            return;
        }

        //Proceed with moving to the first row of the cursor and reading data from it
        //(This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Find the columns of inventory attributes that we 're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int authorColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_AUTHOR);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            mInventoryImageUri = Uri.parse(cursor.getString(imageColumnIndex));

            //Update the views on the screen with the values from the database
            mEditName.setText(name);
            mEditAuthor.setText(author);
            mEditSupplier.setText(supplier);
            mEditPhone.setText(phone);
            mEditPrice.setText(price);
            mEditQuantity.setText(quantity);
            mImageView.setImageURI(mInventoryImageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields.
        mEditName.setText("");
        mEditAuthor.setText("");
        mEditSupplier.setText("");
        mEditPhone.setText("");
        mEditQuantity.setText("");
        mEditPrice.setText("");
            }

    /*
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue lwacing the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                     the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, an click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // User clicked the "Keep building" button, so dismiss the dialog
                // and continue editing the inventory
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        // Create an show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Prompt the user to confirm that they want to delete this inventory.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory.
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the inventory in the database.
     */
    private void deleteInventory() {
        // Only preform the delete if this is an existing inventory
        if(mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the inventory at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentInventoryUri
            // content URI already identifies the inventory that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was suxxessful.
            if (rowsDeleted == 0){
                //If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this,getString(R.string.editor_delete_inventory_failed), Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise, the delete was successful and we con display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful), Toast.LENGTH_SHORT).show();
            }
        }

        //Close the activity
        finish();
    }


    // Image Button logic
    // Credit goes to Carlos Jimenez who wrote an example app.

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    // Increase / decrease Button logic

    // set the quantity on the {@mEditQuantity}
    public void displayProductQuantity(int mTextQuantity) {
        mEditQuantity.setText(String.valueOf(mTextQuantity));
    }

    //increase the quantity by one
    public void addOneToQuantity(View v) {
        quantity = quantity + 1;
        displayProductQuantity(quantity);
    }

    //reduce the quantity by one
    private void subtractOneOfQuantity(View view) {
        if (quantity > 0) {
            quantity = quantity - 1;
            displayProductQuantity(quantity);
        }
    }


    //Logic to call the supplier phone number
    private void makePhoneCallIntent (){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + mEditPhone.getText().toString().trim()));
        startActivity(intent);

    }

}
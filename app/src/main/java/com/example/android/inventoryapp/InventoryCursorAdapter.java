package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of inventory data in the {@link Cursor}.
 */


public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent,false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current inventory can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        // (bzw. Find individual views that we want to modify in the list item layout)
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView authorTextView = (TextView) view.findViewById(R.id.author);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // Find the columns of inventory attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
        int authorColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_AUTHOR);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
        final int rowId = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));

        // Read the inventory attributes from the Cursor for the current inventory
        String inventoryName = cursor.getString(nameColumnIndex);
        String inventoryAuthor = cursor.getString(authorColumnIndex);
        String inventoryPrice = cursor.getString(priceColumnIndex);
        String inventoryQuantity = cursor.getString(quantityColumnIndex);

        // If the inventory author is an empty string or null, then use some default text
        // that says "Unknown author", so the TextView isn't blank.
        if (TextUtils.isEmpty(inventoryAuthor)) {
            inventoryAuthor = context.getString(R.string.unknown_author);
        }

        // Update the TextViews with the attributes for the current inventory
        nameTextView.setText(inventoryName);
        authorTextView.setText(inventoryAuthor);
        priceTextView.setText(inventoryPrice);
        quantityTextView.setText(inventoryQuantity);

        final int currentQuantity = Integer.parseInt(inventoryQuantity);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = currentQuantity;
                if (quantity > 0) {
                    quantity = quantity - 1;
                }

                ContentValues contentValues = new ContentValues();
                Uri newUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, rowId);
                contentValues.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
                context.getContentResolver().update(newUri, contentValues, null, null);
            }
        });
    }
}
package com.example.android.inventoryapp.data;


import android.provider.BaseColumns;

public final class InventoryContract {

    private InventoryContract() {}

    public static final class InventoryEntry implements BaseColumns {

        public final static String TABLE_NAME  = "inventory";

        public final static String  _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_AUTHOR = "author";
        public final static String COLUMN_SUPPLIER = "supplier";
        public final static String COLUMN_PHONE = "phone";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY = "quantity";

    }
}

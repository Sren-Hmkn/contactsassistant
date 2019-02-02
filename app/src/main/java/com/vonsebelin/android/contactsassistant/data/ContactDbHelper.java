package com.vonsebelin.android.contactsassistant.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vonsebelin.android.contactsassistant.data.ContactContract.ContactEntry;

public class ContactDbHelper extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "assistant_contacts_3.db";
    public final static int DATABASE_VERSION = 2;

    public ContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ContactEntry.TABLE_NAME + " ("
                + ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ContactEntry.COLUMN_CONTACT_NOTE + " TEXT ,"
                + ContactEntry.COLUMN_CONTACT_BCARD + " BLOB , "
                + ContactEntry.COLUMN_CONTACT_IMAGE + " BLOB , "
                + ContactEntry.COLUMN_CONTACT_NAME + " TEXT ,"
                + ContactEntry.COLUMN_CONTACT_EVENT + " TEXT , "
                + ContactEntry.COLUMN_CONTACT_DATE + " TEXT , "
                + ContactEntry.COLUMN_CONTACT_MAIL + " TEXT , "
                + ContactEntry.COLUMN_CONTACT_ADDRESS_STREET + " TEXT , "
                + ContactEntry.COLUMN_CONTACT_ADDRESS_CITY + " TEXT , "
                + ContactEntry.COLUMN_CONTACT_TAGS + " TEXT , "
                + ContactEntry.COLUMN_CONTACT_BIRTHDAY + " TEXT , "
                + ContactEntry.COLUMN_CONTACT_PHONE + " TEXT "
                + ");";
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

        Log.i("DB","successfully created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

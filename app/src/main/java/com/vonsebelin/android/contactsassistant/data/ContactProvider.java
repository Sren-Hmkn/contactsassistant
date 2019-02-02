package com.vonsebelin.android.contactsassistant.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.io.File;

// THIS CLASS HANDLES THE METHODS FOR THE DATABASE
// CREATE;READ;UPDATE;INSERT;DELETE;QUERY (and getType)
// CHANGE STUFF HERE https://stackoverflow.com/questions/6217008/stream-audio-from-sqlite-db/6231791#6231791


public final class ContactProvider extends ContentProvider {

    public static final String LOG_TAG = ContactProvider.class.getSimpleName();

    public ContactDbHelper mDbHelper;

    private static final int CONTACTS = 100;
    private static final int CONTACT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ContactContract.CONTENT_AUTHORITY,ContactContract.PATH_CONTACTS,CONTACTS);
        sUriMatcher.addURI(ContactContract.CONTENT_AUTHORITY,ContactContract.PATH_CONTACTS +"/#",CONTACT_ID);
    }

    @Override
    public boolean onCreate(){
        mDbHelper = new ContactDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder){
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case CONTACTS:
                cursor = database.query(
                        ContactContract.ContactEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,null,null);
                break;
            case CONTACT_ID:
                selection = ContactContract.ContactEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(
                        ContactContract.ContactEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,null,null);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case CONTACTS:
                return insertContact(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertContact(Uri uri, ContentValues values) {
        // Check that the stuff is not null

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new contact with the given values
        long id = database.insert(
                ContactContract.ContactEntry.TABLE_NAME,
                null,
                values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return updateContact(uri, contentValues, selection, selectionArgs);
            case CONTACT_ID:
                selection = ContactContract.ContactEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateContact(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateContact(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ContactContract.ContactEntry.TABLE_NAME,values,selection,selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case CONTACTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ContactContract.ContactEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case CONTACT_ID:
                // Delete a single row given by the ID in the URI
                selection = ContactContract.ContactEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ContactContract.ContactEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return ContactContract.ContactEntry.CONTENT_LIST_TYPE;
            case CONTACT_ID:
                return ContactContract.ContactEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

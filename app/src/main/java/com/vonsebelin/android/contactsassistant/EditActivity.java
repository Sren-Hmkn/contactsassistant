package com.vonsebelin.android.contactsassistant;

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
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vonsebelin.android.contactsassistant.data.ContactContract;

import java.io.ByteArrayOutputStream;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class EditActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri mCurrentContactUri;
    private EditText
            mNameEditText,
            mPhoneEditText,
            mMailEditText,
            mEventEditText,
            mDateEditText,
            mStreetEditText,
            mCityEditText,
            mBirthdayEditText,
            mTagEditText;
    private ImageView mBusinessCard;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };



    public EditActivity() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        mCurrentContactUri = intent.getData();

        setTitle("Contact Information");
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        mBusinessCard = (ImageView) findViewById(R.id.business_card);
        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mPhoneEditText = (EditText) findViewById(R.id.edit_phone);
        mMailEditText = (EditText) findViewById(R.id.edit_mail);
        mEventEditText = (EditText) findViewById(R.id.edit_event);
        mDateEditText = (EditText) findViewById(R.id.edit_date);
        mStreetEditText = (EditText) findViewById(R.id.edit_street);
        mCityEditText = (EditText) findViewById(R.id.edit_city);
        mBirthdayEditText = (EditText) findViewById(R.id.edit_birthday);
        mTagEditText = (EditText) findViewById(R.id.edit_tags);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mMailEditText.setOnTouchListener(mTouchListener);
        mEventEditText.setOnTouchListener(mTouchListener);
        mDateEditText.setOnTouchListener(mTouchListener);
        mStreetEditText.setOnTouchListener(mTouchListener);
        mCityEditText.setOnTouchListener(mTouchListener);
        mBirthdayEditText.setOnTouchListener(mTouchListener);
        mTagEditText.setOnTouchListener(mTouchListener);

    }

    private boolean saveProduct(){
        String nameString = mNameEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String mailString = mMailEditText.getText().toString().trim();
        String eventString = mEventEditText.getText().toString().trim();
        String dateString = mDateEditText.getText().toString().trim();
        String streetString = mStreetEditText.getText().toString().trim();
        String cityString = mCityEditText.getText().toString().trim();
        String birthdayString = mBirthdayEditText.getText().toString().trim();
        String tagsString = mTagEditText.getText().toString().trim();

        if(mCurrentContactUri == null){
            Toast.makeText(this, getString(R.string.editor_insert_contact_failed), Toast.LENGTH_LONG).show();
            return false;
        }

        ContentValues values = new ContentValues();

        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_NAME, nameString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_PHONE, phoneString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_MAIL, mailString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_EVENT, eventString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_DATE, dateString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_ADDRESS_STREET, streetString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_ADDRESS_CITY, cityString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_BIRTHDAY, birthdayString);
        values.put(ContactContract.ContactEntry.COLUMN_CONTACT_TAGS, tagsString);

        int rowsAffected = 0;
        try {
            rowsAffected = getContentResolver().update(mCurrentContactUri, values, null, null);
        } catch(IllegalArgumentException e){
            Log.e("Error", e.getMessage());
        }

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, getString(R.string.editor_update_contact_failed),
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_update_contact_successful),
                    Toast.LENGTH_SHORT).show();
            return true;
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ContactContract.ContactEntry._ID,
                ContactContract.ContactEntry.COLUMN_CONTACT_NAME,
                ContactContract.ContactEntry.COLUMN_CONTACT_NOTE,
                ContactContract.ContactEntry.COLUMN_CONTACT_BCARD,
                ContactContract.ContactEntry.COLUMN_CONTACT_IMAGE,
                ContactContract.ContactEntry.COLUMN_CONTACT_PHONE,
                ContactContract.ContactEntry.COLUMN_CONTACT_MAIL,
                ContactContract.ContactEntry.COLUMN_CONTACT_EVENT,
                ContactContract.ContactEntry.COLUMN_CONTACT_DATE,
                ContactContract.ContactEntry.COLUMN_CONTACT_ADDRESS_STREET,
                ContactContract.ContactEntry.COLUMN_CONTACT_ADDRESS_CITY,
                ContactContract.ContactEntry.COLUMN_CONTACT_BIRTHDAY,
                ContactContract.ContactEntry.COLUMN_CONTACT_TAGS
        };
        return new CursorLoader(this, mCurrentContactUri, projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int noteColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_NOTE);
            int bcardColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_BCARD);
            int imageColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_IMAGE);

            int nameColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_PHONE);
            int mailColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_MAIL);
            int eventColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_EVENT);
            int dateColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_DATE);
            int addressStreetColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_ADDRESS_STREET);
            int addressCityColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_ADDRESS_CITY);
            int birthdayColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_BIRTHDAY);
            int tagsColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_TAGS);

            String note = cursor.getString(noteColumnIndex);
            byte[] bcard = cursor.getBlob(bcardColumnIndex);
            if( bcard != null) {
                mBusinessCard.setImageBitmap(BitmapFactory.decodeByteArray(bcard, 0, bcard.length));
            }


            byte[] image = cursor.getBlob(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String mail = cursor.getString(mailColumnIndex);
            String event = cursor.getString(eventColumnIndex);
            String date = cursor.getString(dateColumnIndex);
            String addressStreet = cursor.getString(addressStreetColumnIndex);
            String addressCity = cursor.getString(addressCityColumnIndex);
            String birthday = cursor.getString(birthdayColumnIndex);
            String tags = cursor.getString(tagsColumnIndex);

            mNameEditText.setText(name);
            mPhoneEditText.setText(phone);
            mMailEditText.setText(mail);
            mEventEditText.setText(event);
            mDateEditText.setText(date);
            mStreetEditText.setText(addressStreet);
            mCityEditText.setText(addressCity);
            mBirthdayEditText.setText(birthday);
            mTagEditText.setText(tags);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPhoneEditText.setText("");
        mMailEditText.setText("");
        mEventEditText.setText("");
        mDateEditText.setText("");
        mStreetEditText.setText("");
        mCityEditText.setText("");
        mBirthdayEditText.setText("");
        mTagEditText.setText("");
    }


// Menu on top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if(saveProduct()) finish();
                return true;
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentContactUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentContactUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_contact_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_contact_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

}

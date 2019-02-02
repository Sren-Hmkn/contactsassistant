package com.vonsebelin.android.contactsassistant;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vonsebelin.android.contactsassistant.data.ContactContract;

public class ContactCursorAdapter extends CursorAdapter {

    public ContactCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contact_list_item,parent,false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        ImageView businessCardView = (ImageView) view.findViewById(R.id.photo_button);

        int nameColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_NAME);
        int bcardColumnIndex = cursor.getColumnIndex(ContactContract.ContactEntry.COLUMN_CONTACT_BCARD);

        String contactName = cursor.getString(nameColumnIndex);
        byte[] currentBcard = cursor.getBlob(bcardColumnIndex);

        nameTextView.setText(contactName);
        if(currentBcard != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(currentBcard, 0, currentBcard.length);
            businessCardView.setImageBitmap(bitmap);
        } else {
            businessCardView.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
        }
    }

}

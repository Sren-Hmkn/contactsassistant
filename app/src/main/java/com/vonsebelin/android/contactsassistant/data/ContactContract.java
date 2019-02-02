package com.vonsebelin.android.contactsassistant.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ContactContract {

    private ContactContract(){}

    public static final String CONTENT_AUTHORITY = "com.vonsebelin.android.contactsassistant";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CONTACTS = "contacts";

    public static abstract class ContactEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);
        public static final String TABLE_NAME="contacts";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CONTACT_NOTE = "note";
        public static final String COLUMN_CONTACT_BCARD = "businesscard";
        public static final String COLUMN_CONTACT_TAGS = "tags";
        public static final String COLUMN_CONTACT_NAME = "name";
        public static final String COLUMN_CONTACT_IMAGE = "image";
        public static final String COLUMN_CONTACT_EVENT = "event";
        public static final String COLUMN_CONTACT_DATE = "date";
        public static final String COLUMN_CONTACT_MAIL = "mail";
        public static final String COLUMN_CONTACT_PHONE = "phone";
        public static final String COLUMN_CONTACT_BIRTHDAY = "birthday";
        public static final String COLUMN_CONTACT_ADDRESS_STREET = "street";
        public static final String COLUMN_CONTACT_ADDRESS_CITY = "city";



        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

    }

}

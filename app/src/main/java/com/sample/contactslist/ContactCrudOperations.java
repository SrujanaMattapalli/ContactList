package com.sample.contactslist;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import com.sample.contactslist.Pojo.ContactModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactCrudOperations {

    public static boolean addcontacts(Context context, String Name, String Number) {
        try {
            //Uri addContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            // Below uri can avoid java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/phones error.
            Uri addContactsUri = ContactsContract.Data.CONTENT_URI;
            // Add an empty contact and get the generated id.
            long rowContactId = ContactCrudOperations.getRawContactId(context);
            // Add contact name data.
            ContactCrudOperations.insertContactDisplayName(context, addContactsUri, rowContactId, Name);
            // Add contact phone data.
            ContactCrudOperations.insertContactPhoneNumber(context, addContactsUri, rowContactId, Number);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean updateContactList(Context context, String name, String newPhoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? AND " + String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";
        String[] params = new String[]{name, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, params)
                .withValue(ContactsContract.CommonDataKinds.Phone.DATA, newPhoneNumber)
                .build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteContactList(Context context, String name) {
        try {
            ContentResolver cr = context.getContentResolver();
            String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
            String[] params = new String[]{name};
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection(where, params)
                    .build());
            try {
                cr.applyBatch(ContactsContract.AUTHORITY, ops);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /*Get all Contacts List and send to Main Activity*/
    public static List<ContactModel> getAllContacts(Context context) {
        try {
            List<ContactModel> getallcontactslist = new ArrayList<>();
            ContentResolver contentResolver = context.getContentResolver();
            /*Fetching Top 60 Contacts*/
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC limit 60");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));
                        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, Long.parseLong(id));
                        Uri pURI = Uri.withAppendedPath(photoUri, ContactsContract.Contacts.Photo.PHOTO);
                        Bitmap photo = null;
                        if (inputStream != null) {
                            photo = BitmapFactory.decodeStream(inputStream);
                        }
                        while (cursorInfo.moveToNext()) {
                            ContactModel info = new ContactModel();
                            info.id = id;
                            info.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            info.mobileNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            info.photo = photo;
                            info.photoURI = pURI;
                            getallcontactslist.add(info);
                        }
                        cursorInfo.close();
                    }
                }
                cursor.close();
            }
            return getallcontactslist;
        } catch (Exception ex) {
            ex.printStackTrace();
            List<ContactModel> contactModels = new ArrayList<>();
            return contactModels;
        }
    }

    /*Insert On DisplayName*/
    public static void insertContactDisplayName(Context context, Uri addContactsUri, long rawContactId, String displayName) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            // Put contact display name value.
            contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, displayName);
            context.getContentResolver().insert(addContactsUri, contentValues);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*INsert only COntactNumber*/
    private static void insertContactPhoneNumber(Context context, Uri addContactsUri, long rawContactId, String phoneNumber) {
        try {
            // Create a ContentValues object.
            ContentValues contentValues = new ContentValues();
            // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            // Put phone number value.
            contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
            // Calculate phone type by user selection.
            int phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
            // Put phone type value.
            contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneContactType);
            // Insert new contact data into phone contact list.
            context.getContentResolver().insert(addContactsUri, contentValues);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // This method will only insert an empty data to RawContacts.CONTENT_URI
    // The purpose is to get a system generated raw contact id.
    public static long getRawContactId(Context context) {
        // Inser an empty contact.
        ContentValues contentValues = new ContentValues();
        Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created contact raw id.
        long ret = ContentUris.parseId(rawContactUri);
        return ret;
    }
}

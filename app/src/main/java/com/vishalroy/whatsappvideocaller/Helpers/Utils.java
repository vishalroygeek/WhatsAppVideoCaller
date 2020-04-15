package com.vishalroy.whatsappvideocaller.Helpers;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.widget.Toast;
import java.util.ArrayList;

public class Utils {

    private Context context;

    public Utils(Context context) {
        this.context = context;
    }

    //Method to open app settings
    public void openAppSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    //Method to check if an app is installed or not
    public static boolean isAppInstalled(Context context, String packageName) {
        if (context!=null){
            try {
                context.getPackageManager().getApplicationInfo(packageName, 0);
                return true;
            }catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }else {
            return false;
        }
    }

    //Method to get app's icon from package name
    public static Drawable getAppIconFromPackage(Context context, String packageName){
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return icon;
    }

    //Method to get app's name from package name
    public static String getAppNameFromPackage(Context context, String packageName){
        PackageManager pm = context.getPackageManager();
        String appName = "";

        try {
            appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appName;
    }

    //Method to launch WhatsApp voip or video call
    public void launchWhatsAppCall(String contactID, String packageName, String mime, Context context){
        String data = "content://com.android.contacts/data/" + contactID;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(data), mime);
        intent.setPackage(packageName);
        context.startActivity(intent);
    }

    //Method to check if string is neither null nor empty
    public static boolean isStringValid(String text){
        return text!=null && !text.isEmpty();
    }

    //Method to get contact ID by phone number
    public static String contactIdByPhoneNumber(Context context, String phoneNumber, String mime) {
        String contactId = null;
        if (isStringValid(phoneNumber)) {

            //Initializing contact resolver
            ContentResolver contentResolver = context.getContentResolver();

            //Making required cursor
            Cursor cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null, null, null,
                    null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //Fetching all the required contact details
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                    String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA3));


                    if (isStringValid(number)){
                        //Removing empty spaces from number
                        number = number.replaceAll("\\s+", "");

                        //Removing alphabets from number. Yes this is required :)
                        number = number.replaceAll("([a-z, A-Z])", "");

                        //Assigning contact ID if phone number and mime are satisfied
                        if (number.equals(phoneNumber) && mimeType.equals(mime)){
                            contactId = id;
                        }
                    }
                }
                cursor.close();
            }
        }
        return contactId;
    }

    //Method to save a new contact
    public static String saveNewContact(String name, String number, ContentResolver contentResolver){

        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<>();
        int contactIndex = cntProOper.size();

        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, cntProOper);
        } catch (RemoteException | OperationApplicationException exp) {}

        return String.valueOf(contactIndex);
    }

    //Method to show toast
    public void toast(String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}

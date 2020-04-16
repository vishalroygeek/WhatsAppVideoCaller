package com.vishalroy.whatsappvideocaller;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.hbb20.CountryCodePicker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.vishalroy.whatsappvideocaller.Dialogs.LoaderDialog;
import com.vishalroy.whatsappvideocaller.Helpers.Typefaces;
import com.vishalroy.whatsappvideocaller.Helpers.Utils;

import java.util.List;

import static com.vishalroy.whatsappvideocaller.Helpers.Constants.MIME_WHATSAPP_BUSINESS_VIDEO_CALL;
import static com.vishalroy.whatsappvideocaller.Helpers.Constants.MIME_WHATSAPP_VIDEO_CALL;
import static com.vishalroy.whatsappvideocaller.Helpers.Constants.PACKAGE_WHATSAPP;
import static com.vishalroy.whatsappvideocaller.Helpers.Constants.PACKAGE_WHATSAPP_BUSINESS;
import static com.vishalroy.whatsappvideocaller.Helpers.Constants.URL_WHATSAPP;
import static com.vishalroy.whatsappvideocaller.Helpers.Utils.contactIdByPhoneNumber;
import static com.vishalroy.whatsappvideocaller.Helpers.Utils.getAppIconFromPackage;
import static com.vishalroy.whatsappvideocaller.Helpers.Utils.getAppNameFromPackage;
import static com.vishalroy.whatsappvideocaller.Helpers.Utils.hasActiveInternetConnection;
import static com.vishalroy.whatsappvideocaller.Helpers.Utils.isAppInstalled;
import static com.vishalroy.whatsappvideocaller.Helpers.Utils.isStringValid;
import static com.vishalroy.whatsappvideocaller.Helpers.Utils.saveNewContact;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button video_call_button;
    private TextView message;
    private EditText number;
    private CountryCodePicker ccp;
    private Typefaces typefaces;
    private Utils utils;
    private LoaderDialog loaderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing some objects
        typefaces = new Typefaces(this);
        utils = new Utils(this);
        loaderDialog = new LoaderDialog(this);

        //Finding view by ID
        video_call_button = findViewById(R.id.video_call_button);
        message = findViewById(R.id.message);
        number = findViewById(R.id.number);
        ccp = findViewById(R.id.ccp);

        //Changing typefaces
        video_call_button.setTypeface(typefaces.ralewayMedium());
        message.setTypeface(typefaces.ralewayMedium());
        number.setTypeface(typefaces.latoBold());
        ccp.setTypeFace(typefaces.ralewaySemiBold());

        //Binding number editText to ccp
        ccp.registerCarrierNumberEditText(number);

        //Assigning click listeners
        video_call_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.video_call_button:
                //Checking if the phone number is formatted correctly
                if (ccp.isValidFullNumber()){
                    //Lets check if the necessary permission are granted
                    checkPermissionsWithDexter();
                    break;
                }else {
                    utils.toast(getString(R.string.invalid_number));
                }
        }
    }

    private class SaveContactAndCall extends AsyncTask<Void, Void, Void>{
        String contactID, phoneNumber, packageName, whatsAppMime;
        boolean noInternet = false;

        public SaveContactAndCall(String packageName, String whatsAppMime) {
            this.packageName = packageName;
            this.whatsAppMime = whatsAppMime;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Showing loader dialog
            loaderDialog.show();

            //Getting the typed phone number
            phoneNumber = ccp.getFullNumberWithPlus();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (hasActiveInternetConnection(MainActivity.this, URL_WHATSAPP)){
                //Getting contact ID from phone number
                contactID = contactIdByPhoneNumber(MainActivity.this, phoneNumber, whatsAppMime);

                //Lets proceed if the phone number is not saved
                if (!isStringValid(contactID)){
                    //Saving the number as contact & getting the ID
                    saveNewContact(phoneNumber, phoneNumber, getContentResolver());

                    /*Adding a delay to let WhatsApp update MIME details
                     * of the contact*/
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            contactID = contactIdByPhoneNumber(MainActivity.this, phoneNumber, whatsAppMime);
                        }
                    }, 5000);
                }
            }else {
                noInternet = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Dismissing the progress bar
            loaderDialog.dismiss();


            /*Finally making the video call after checking if the device has active internet
            * connection or not*/
            if (noInternet){
                utils.toast(getString(R.string.no_internet));
            }else if (isStringValid(contactID)){
                utils.launchWhatsAppCall(contactID, packageName, whatsAppMime, MainActivity.this);
            }else {
                utils.toast(getString(R.string.not_whatsapp_number));
            }
        }
    }

    private void checkPermissionsWithDexter(){
        //Initializing Dexter
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.CALL_PHONE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()){
                            //Redirecting user to settings if permission is permanently denied
                            utils.toast(getString(R.string.permissions_grant));
                            utils.openAppSettings();
                        }else if (multiplePermissionsReport.areAllPermissionsGranted()){
                            //Letting user choose if multiple WhatsApps are installed
                            makeWhatsAppVideoCall(MainActivity.this);
                        }else {
                            //Notifying user that permission has been denied
                            utils.toast(getString(R.string.permissions_denied));
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void makeWhatsAppVideoCall(Context context){
        if (isAppInstalled(context, PACKAGE_WHATSAPP) && isAppInstalled(context, PACKAGE_WHATSAPP_BUSINESS)){
            showAppOptionsToCall(context);
        }else if (isAppInstalled(context, PACKAGE_WHATSAPP)){
            new SaveContactAndCall(PACKAGE_WHATSAPP, MIME_WHATSAPP_VIDEO_CALL).execute();
        }else if (isAppInstalled(context, PACKAGE_WHATSAPP_BUSINESS)){
            new SaveContactAndCall(PACKAGE_WHATSAPP_BUSINESS, MIME_WHATSAPP_BUSINESS_VIDEO_CALL).execute();
        }else {
            utils.toast(getString(R.string.whatsapp_not_found));
        }
    }

    private void showAppOptionsToCall(final Context context){
        BottomSheetBuilder builder = new BottomSheetBuilder(context);
        builder.setMode(BottomSheetBuilder.MODE_LIST);
        builder.expandOnStart(true);
        builder.addTitleItem(getString(R.string.call_via));

        //Adding WhatsApps to the list
        builder.addItem(0, getAppNameFromPackage(context, PACKAGE_WHATSAPP), getAppIconFromPackage(context, PACKAGE_WHATSAPP));
        builder.addItem(1, getAppNameFromPackage(context, PACKAGE_WHATSAPP_BUSINESS), getAppIconFromPackage(context, PACKAGE_WHATSAPP_BUSINESS));


        builder.setItemClickListener(new BottomSheetItemClickListener() {
            @Override
            public void onBottomSheetItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case 0:
                        new SaveContactAndCall(PACKAGE_WHATSAPP, MIME_WHATSAPP_VIDEO_CALL).execute();
                        break;
                    case 1:
                        new SaveContactAndCall(PACKAGE_WHATSAPP_BUSINESS, MIME_WHATSAPP_BUSINESS_VIDEO_CALL).execute();
                        break;
                }
            }
        });

        builder.createDialog().show();
    }


}

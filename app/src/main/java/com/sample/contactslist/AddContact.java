package com.sample.contactslist;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddContact extends AppCompatActivity {
    @BindView(R.id.name_edt)
    TextInputEditText name_edt;

    @BindView(R.id.number_edt)
    TextInputEditText number_edt;

    @BindView(R.id.save_btn)
    Button save_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ButterKnife.bind(this);
        askForContactPermission();
        if (getIntent().getExtras() != null) {
            if (!getIntent().getStringExtra("UPDATE").isEmpty()) {
                name_edt.setText(getIntent().getStringExtra("NAME"));
                number_edt.setText(getIntent().getStringExtra("NUMBER"));
                save_btn.setText(getIntent().getStringExtra("UPDATE"));
            }
        }
    }

    @OnClick(R.id.save_btn)
    public void AddContact() {
        if (!name_edt.getText().toString().isEmpty() && name_edt.getText().toString() != null && name_edt.getText().toString().length() > 2) {
            if (!number_edt.getText().toString().isEmpty() && number_edt.getText().toString() != null && number_edt.getText().toString().length() > 8) {
                askForContactPermission();
            } else {
                number_edt.setFocusable(true);
                number_edt.setError("Valid mobile Number is mandatory to " + save_btn.getText().toString() + " contact");
            }
        } else {
            name_edt.setFocusable(true);
            name_edt.setError("Valid Name is mandatory");
        }
    }



    private void saveContact() {
        try {
            boolean iscontactadded = ContactCrudOperations.addcontacts(AddContact.this,name_edt.getText().toString(),number_edt.getText().toString());
            if (iscontactadded) {
                Toast.makeText(getApplicationContext(), "Contact Saved Successfully", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
            }else {
                Toast.makeText(getApplicationContext(), "Can't   save contact", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Can't   save contact", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    public void askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, 1);
                        }
                    });
                    builder.show();
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, 1);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                if (save_btn.getText().toString().equalsIgnoreCase("Add")) {
                    if (!name_edt.getText().toString().isEmpty())
                        saveContact();
                } else if (save_btn.getText().toString().equalsIgnoreCase("UPDATE")) {
                   updateContact();
                }
            }
        } else {
            if (save_btn.getText().toString().equalsIgnoreCase("Add")) {
                if (!name_edt.getText().toString().isEmpty())
                    saveContact();
            } else if (save_btn.getText().toString().equalsIgnoreCase("UPDATE")) {
                updateContact();
            }
        }
    }

    private void updateContact()
    {
        try{
            boolean isupdated = ContactCrudOperations.updateContactList(AddContact.this, name_edt.getText().toString(), number_edt.getText().toString());
            if (isupdated) {
                name_edt.setText("");
                number_edt.setText("");
                Toast.makeText(AddContact.this, "Contact Updated Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Toast.makeText(AddContact.this, "Can't Updated contact details", Toast.LENGTH_SHORT).show();

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!name_edt.getText().toString().isEmpty())
                        saveContact();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "No Permissions ", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }




}

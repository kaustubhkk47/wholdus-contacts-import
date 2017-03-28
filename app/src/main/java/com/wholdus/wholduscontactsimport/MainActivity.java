package com.wholdus.wholduscontactsimport;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int CONTACTS_PERMISSION = 0;
    private static final int CONTACTS_REMOVE_PERMISSION = 1;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button importContactsButton = (Button) findViewById(R.id.import_contacts_button);
        importContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchContacts();
            }
        });

        Button clearContactsButton = (Button) findViewById(R.id.clear_contacts_button);
        clearContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeContacts();
            }
        });

        Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LogoutTask().execute();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void fetchContacts() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            mProgressBar.setVisibility(View.VISIBLE);
            new FetchAndSaveContactsTask().execute();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, CONTACTS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CONTACTS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchContacts();
                } else {
                    Toast.makeText(this, "Permission needed", Toast.LENGTH_SHORT).show();
                }
                break;
            case CONTACTS_REMOVE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    removeContacts();
                } else {
                    Toast.makeText(this, "Permission needed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private class LogoutTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            LoginHelper loginHelper = new LoginHelper(getApplicationContext());
            return loginHelper.logout();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.general_error_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchAndSaveContactsTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("new_contacts", "1");
                String url = GlobalAccessHelper.generateUrl(APIConstants.MARKETING_CONTACTS_URL, params);
                Response response = OkHttpHelper.makeGetRequest(getApplicationContext(), url);
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    response.body().close();
                    JSONObject data = new JSONObject(responseBody);
                    JSONArray contacts = data.getJSONArray("marketing_contacts");
                    if (contacts.length() == 0) {
                        return 2;
                    }
                    ArrayList<Integer> contactIDs = saveContacts(contacts);

                    HashMap<String, String> updateParams = new HashMap<>();
                    updateParams.put("marketingcontactID", TextUtils.join(",", contactIDs));
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("assign_user", 1);
                    String updateUrl = GlobalAccessHelper.generateUrl(APIConstants.MARKETING_CONTACTS_URL, updateParams);
                    Response updateResponse = OkHttpHelper.makePutRequest(getApplicationContext(), updateUrl, requestBody.toString());
                    if (updateResponse.isSuccessful()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer code) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (code==0) {
                Toast.makeText(getApplicationContext(), R.string.general_error_message, Toast.LENGTH_SHORT).show();
            } else if (code==1) {
                Toast.makeText(getApplicationContext(), R.string.general_success_message, Toast.LENGTH_SHORT).show();
            }else if (code==2) {
                Toast.makeText(getApplicationContext(), "No contacts left to import", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ArrayList<Integer> saveContacts(JSONArray contacts) throws Exception {

        ArrayList<Integer> contactIDs = new ArrayList<>();

        for (int i = 0; i < contacts.length(); i++) {

            JSONObject contact = contacts.getJSONObject(i);
            String nameToWrite = contact.getString("contact_name");
            String mobileNumber = contact.getString("mobile_number");
            contactIDs.add(contact.getInt("marketingcontactID"));

            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            nameToWrite).build());

            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());

            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }



        return contactIDs;

    }

    private class RemoveContactsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                removeContactsFromPhone();
                String url = GlobalAccessHelper.generateUrl(APIConstants.MARKETING_CONTACTS_URL, null);
                Response response = OkHttpHelper.makeDeleteRequest(getApplicationContext(), url, new JSONObject().toString());
                return response.isSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (!success) {
                Toast.makeText(getApplicationContext(), R.string.general_error_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.general_success_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeContacts() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            mProgressBar.setVisibility(View.VISIBLE);
            new RemoveContactsTask().execute();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, CONTACTS_PERMISSION);
        }
    }

    private void removeContactsFromPhone() throws Exception {

        ContentResolver contentResolver = getContentResolver();
        String selection = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE '%Script%'";
        String[] projection = {ContactsContract.Contacts._ID};
        Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                projection, selection, null, null);
        if (cur.getCount() == 0) {
            return;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation contentProviderOperation;
        while (cur.moveToNext()) {
            String contactID = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            contentProviderOperation = ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI).withSelection(ContactsContract.Data._ID + " = " + contactID, null).build();
            operationList.add(contentProviderOperation);
        }
        getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
    }

}

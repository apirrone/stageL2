package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class BrowseContacts extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;
    String itemToEdit;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_contacts);


        lv = (ListView)findViewById(R.id.ListContacts);
        sqLiteHelper = new SQLiteHelper(this);

        updateView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ;
                String itemValue = (String) lv.getItemAtPosition(position);
                goToSendActivity(itemValue);
            }

        });

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return longClickAlert(position);
            }
        });

       // Useful to send our public Key to another user without launching again "addcontactactivity"
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override public NdefMessage createNdefMessage(NfcEvent event) {

                String stringOut = KeysHelper.getMyPublicKey();

                byte[] bytesOut = stringOut.getBytes();

                NdefRecord ndefRecordOut = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[] {},
                        bytesOut);

                NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);

                return ndefMessageout;
            }
        }, this);

    }

    @Override
    public void onResume(){
        super.onResume();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    public boolean longClickAlert(int position){
        itemToEdit = (String) lv.getItemAtPosition(position);


        new AlertDialog.Builder(BrowseContacts.this)
                .setTitle("Edit Contact")
                .setMessage("What do yo want to do with this contact?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    User u = sqLiteHelper.getUserByName(itemToEdit);

                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteHelper.deleteUser(u);

                        updateView();
                    }
                })
                .setNegativeButton("Rename", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        renameAlert();
                    }

                })
                .setNeutralButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return true;
    }

    public boolean renameAlert(){

        final EditText input = new EditText(this);
        new AlertDialog.Builder(BrowseContacts.this)
                .setTitle("Rename contact")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    User u = sqLiteHelper.getUserByName(itemToEdit);

                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteHelper.updateUserName(u, input.getText().toString());

                        updateView();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setView(input)
                .setIcon(android.R.drawable.ic_dialog_alert)

                .show();
        return true;
    }

    public void updateView(){
        List<User> users = sqLiteHelper.getAllUsers();

        String[] lv_arr = new String[users.size()];

        for(int i = 0 ; i < users.size() ; i++)
            lv_arr[i] = users.get(i).getName();

        lv.setAdapter(new ArrayAdapter<String>(BrowseContacts.this, android.R.layout.simple_list_item_1, lv_arr));
    }


    public void goToSendActivity(String name){
        Intent intent = new Intent(this, SendMessage.class);
        intent.putExtra("name", name);
        intent.putExtra("pk", sqLiteHelper.getUserByName(name).getPublicKey());
        intent.setAction("NewActivity");
        startActivity(intent);
        finish();
    }

    public void goToAddContact(View view) {
        Intent intent = new Intent(this, AddContactActivity.class);
        intent.setAction("NewActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }




}

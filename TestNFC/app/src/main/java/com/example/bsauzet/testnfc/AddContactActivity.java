package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.Toast;


public class AddContactActivity extends Activity{

    NfcAdapter nfcAdapter;
    EditText editText;
    SQLiteHelper sqLiteHelper;


    /**
     *  Creation of the Activity
     *
     *  Here we initialize and
     *  set up the NFC callback to allow public key exchange
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        editText = (EditText)findViewById(R.id.editText);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        Intent intent = getIntent();

        sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());

        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            /**
             *
             * ndefMessage is being prepared to send public key
             *
             * @param event
             * @return
             */
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

        checkAndProcessBeamIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

    }

    /**
     * Processing to the add query to database.
     * This checks whether the user already exists or not thanks to the publicKey
     *
     * @param publicKey
     * @param name
     */
    public void addContactDataBase(String publicKey, String name){
        if(!sqLiteHelper.userExists(publicKey, name))
            sqLiteHelper.addUser(new User(name, publicKey));
        else {
            User u = sqLiteHelper.getUserByPublicKey(publicKey);
            if(u.getName().toLowerCase().contains("Unknown".toLowerCase())){
                sqLiteHelper.updateUserName(u, name);
            }
            else
                Toast.makeText(AddContactActivity.this,
                        "User already exists",
                        Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkAndProcessBeamIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * Definitions of processes done on reception of ndefMessages.
     *
     * @param intent
     */
    private void checkAndProcessBeamIntent(Intent intent) {
        String action = intent.getAction();

        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];

            String inMsg = new String(NdefRecord_0.getPayload());

            addContactDataBase(inMsg, editText.getText().toString());
            Toast.makeText(AddContactActivity.this, "User successfully added", Toast.LENGTH_SHORT).show();
            finish();

        }
    }

}

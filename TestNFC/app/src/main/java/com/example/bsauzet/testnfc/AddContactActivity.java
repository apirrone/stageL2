package com.example.bsauzet.testnfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;


public class AddContactActivity extends Activity{

    NfcAdapter nfcAdapter;
    EditText editText;
    SQLiteHelper sqLiteHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        editText = (EditText)findViewById(R.id.editText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        Intent intent = getIntent();

        sqLiteHelper = new SQLiteHelper(this);
        //sqLiteHelper.deleteAllUsers(sqLiteHelper.getWritableDatabase());

        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override public NdefMessage createNdefMessage(NfcEvent event) {

                String stringOut = getMyPublicKey();

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
        addContactDataBase("publickitamere", "testPasMoi");
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();

        Toast.makeText(AddContactActivity.this,
                "onResume : "+intent.getAction().toString(),
                Toast.LENGTH_LONG).show();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        IntentFilter[] intentFiltersArray = new IntentFilter[] {ndef, };
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    public void addContactDataBase(String publicKey, String name){
        if(!sqLiteHelper.userExists(publicKey, name))
            sqLiteHelper.addUser(new User(name, publicKey));
        else
            Toast.makeText(AddContactActivity.this,
                    "User already exists",
                    Toast.LENGTH_LONG).show();
    }

    public void checkUserInDataBase(String publicKey){

        List<User> users = sqLiteHelper.getAllUsers();

        for(int i = users.size()-1 ; i >=0 ; i--){
            if(users.get(i).getPublicKey().equals(publicKey)) {
                Toast.makeText(AddContactActivity.this,
                        "check : " + users.get(i).getName(),
                        Toast.LENGTH_LONG).show();
                break;
            }
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        //setIntent(intent);
        Toast.makeText(AddContactActivity.this,
                "OnNewIntent : "+intent.getAction().toString(),
                Toast.LENGTH_LONG).show();
        checkAndProcessBeamIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

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
            //checkUserInDataBase(inMsg);

        }
    }

    public String getMyPublicKey(){
        KeyStore ks = null;
        RSAPublicKey publicKey = null;
        String output = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);


            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry("Keys", null);
            if(keyEntry != null)
                publicKey = (RSAPublicKey) keyEntry.getCertificate().getPublicKey();

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        if(publicKey != null) {
            return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
        }
        else return null;
    }
}

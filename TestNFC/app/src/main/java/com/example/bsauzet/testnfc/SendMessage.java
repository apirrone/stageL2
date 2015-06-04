package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;


public class SendMessage extends Activity {

    private EditText mEdit;
    String destName;
    String destPk;
    String myPk;
    NfcAdapter nfcAdapter;

    SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        Intent intent = getIntent();

        sqLiteHelper = new SQLiteHelper(this);
        destName = intent.getStringExtra("name");
        destPk = intent.getStringExtra("pk");
        myPk = getMyPublicKey();

        Toast.makeText(SendMessage.this,
                destName,
                Toast.LENGTH_LONG).show();
        mEdit = (EditText)findViewById(R.id.editText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {

                String stringOut = mEdit.getText().toString();
                byte[] bytesOutMessage = stringOut.getBytes();
                byte[] bytesOutSender = myPk.getBytes();
                byte[] bytesOutDest = sqLiteHelper.getUserByName(destName).getName().getBytes();

                NdefRecord ndefRecordOut = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        bytesOutMessage);


                NdefRecord[] ndefRecords = new NdefRecord[3];
                ndefRecords[0] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        bytesOutMessage);
                ndefRecords[1] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        bytesOutSender);
                ndefRecords[2] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        bytesOutDest);

                NdefMessage ndefMessageout = new NdefMessage(ndefRecords);

                return ndefMessageout;
            }
        }, this);


    }

    public String getMyPublicKey(){
        KeyStore ks = null;
        RSAPublicKey publicKey = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);


            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry("Keys", null);
            publicKey = (RSAPublicKey) keyEntry.getCertificate().getPublicKey();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return publicKey.toString();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

}

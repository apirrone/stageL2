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
import android.view.View;
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
        mEdit = (EditText)findViewById(R.id.editText);
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void sendButton(View view) {
        Message message = new Message(mEdit.getText().toString(), myPk, destPk);
        Toast.makeText(SendMessage.this,
                message.getPublicKeyDest(),
                Toast.LENGTH_LONG).show();
        sqLiteHelper.addMessage(message);
    }

}

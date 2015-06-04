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
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;


public class MainActivity extends Activity{

    private TextView mTextView;
    private EditText mEdit;

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(getMyPublicKey() == null)
            generateKeys();
        Log.i("myApp", getMyPublicKey());


        mTextView = (TextView)findViewById(R.id.retour);
        mEdit = (EditText)findViewById(R.id.editText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override public NdefMessage createNdefMessage(NfcEvent event) {

                String stringOut = mEdit.getText().toString();

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

        Intent intent = getIntent();

        Toast.makeText(MainActivity.this,
                intent.getAction().toString(),
                Toast.LENGTH_LONG).show();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        IntentFilter[] intentFiltersArray = new IntentFilter[] {ndef, };
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
    }


    private void checkAndProcessBeamIntent(Intent intent) {
        String action = intent.getAction();

        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord message = inNdefRecords[0];
            NdefRecord sender = inNdefRecords[1];
            NdefRecord dest = inNdefRecords[2];

            String inMsg = new String(message.getPayload());
            String inSender = new String(sender.getPayload());
            String inDest = new String(dest.getPayload());

            if(inDest.equals(getMyPublicKey()))
                mTextView.setText(inMsg);//IF MESSAGE FOR ME DISPLAY, ELSE PUT IN DATABASE
            else
                mTextView.setText("MESSAGE NOT FOR YOU");

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(MainActivity.this,
                intent.getAction().toString(),
                Toast.LENGTH_LONG).show();
        checkAndProcessBeamIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void generateKeys(){
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();

        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            kpg.initialize(new KeyPairGeneratorSpec.Builder(getApplicationContext())
                    .setAlias("Keys")
                    .setStartDate(now)
                    .setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal("CN=test1"))
                    .build());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        kpg.generateKeyPair();
    }

    public void goToAddContact(View view) {
        Intent intent = new Intent(this, AddContactActivity.class);
        intent.setAction("NewActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }

    public void browseContactsButton(View view) {
        Intent intent = new Intent(this, BrowseContacts.class);
        intent.setAction("NewActivity");
        startActivity(intent);
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
        if(publicKey != null)
            output = publicKey.toString();
        return output;
    }
}
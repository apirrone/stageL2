package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MainActivity extends Activity{

    private TextView mTextView;
    private EditText mEdit;

    NfcAdapter nfcAdapter;

    SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        if(KeysHelper.getMyPublicKey() == null)
            KeysHelper.generateKeys(getApplicationContext());
        Log.i("myApp", KeysHelper.getMyPublicKey());

        sqLiteHelper = new SQLiteHelper(this);

        mTextView = (TextView)findViewById(R.id.retour);
        mEdit = (EditText)findViewById(R.id.editText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override public NdefMessage createNdefMessage(NfcEvent event) {
                NdefMessage mess = createNdefMessageAllMessages();

                return mess;
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
                Toast.LENGTH_SHORT).show();

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

            ArrayList<Message> incMessages= new ArrayList<Message>();

            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();

            String temp = new String(inNdefRecords[0].getPayload());
            int nbMessages = Integer.valueOf(temp);

            int j = 1;
            for(int i = 0 ; i < nbMessages ; i++){
                String uuid = new String(inNdefRecords[j].getPayload());
                j++;
                byte[] message =  inNdefRecords[j].getPayload();
                j++;
                String source = new String(inNdefRecords[j].getPayload());
                j++;
                String dest = new String(inNdefRecords[j].getPayload());
                j++;
                incMessages.add(new Message(uuid, message, source, dest));
            }
            Log.i("MyApp", "size incMessages : "+incMessages.size());

            addNotKnownMessages(incMessages);

        }
    }

    public void addNotKnownMessages(ArrayList<Message> mess){
        List<Message> myMessages = sqLiteHelper.getAllMessages();
        for(int i = 0 ; i < mess.size() ; i++) {
            boolean newMess = true;
            for (int j = 0; j < myMessages.size(); j++) {
                if (mess.get(i).getUuid().equals(myMessages.get(j).getUuid())) {
                    newMess = false;
                    break;
                }
            }
            if(newMess) {
                if (mess.get(i).getPublicKeyDest().equals(KeysHelper.getMyPublicKey())) { //MESSAGE FOR ME
                    String decryptedMessage = "null";
                    try {
                        decryptedMessage = CryptoHelper.RSADecrypt(mess.get(i).getContent(), KeysHelper.getMyPrivateKey());
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
                        e.printStackTrace();
                    }
                    mTextView.setText(decryptedMessage);
                }
                sqLiteHelper.addMessage(mess.get(i));

            }
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

    public NdefMessage createNdefMessageAllMessages(){

        List<Message> messages = sqLiteHelper.getAllMessages();
        if(!messages.isEmpty()) {
            String nbMessages = String.valueOf(messages.size());

            NdefRecord[] ndefRecords = new NdefRecord[(messages.size() * 4) + 1];

            ndefRecords[0] = new NdefRecord(
                    NdefRecord.TNF_MIME_MEDIA,
                    "text/plain".getBytes(),
                    new byte[]{},
                    nbMessages.getBytes());

            int j = 1;
            for (int i = 0; i < messages.size(); i++) {
                ndefRecords[j] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        messages.get(i).getUuid().getBytes());
                j++;
                ndefRecords[j] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        messages.get(i).getContent());
                j++;
                ndefRecords[j] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        messages.get(i).getPublicKeySource().getBytes());
                j++;
                ndefRecords[j] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        messages.get(i).getPublicKeyDest().getBytes());
                j++;
            }

            NdefMessage ndefMessageout = new NdefMessage(ndefRecords);

            return ndefMessageout;
        }
        else
            return null;
    }

    public void viewMessagesButton(View view) {
        startActivity(new Intent(this, BrowseConversations.class));
    }
}

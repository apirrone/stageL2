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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MainActivity extends Activity{

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

        sqLiteHelper = new SQLiteHelper(this);

        mEdit = (EditText)findViewById(R.id.editText);

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        double now = (double)System.currentTimeMillis()/1000.0;

        List<Message> messages = sqLiteHelper.getAllMessages();
        for (int i = 0 ; i < messages.size(); i++)
            if(messages.get(i).getSent()) {//S'il a déjà été envoyé au moins une fois
                Log.i("Tamere", "timeout : "+(now - messages.get(i).getTimeout()));
                if (now - messages.get(i).getTimeout() > Global.INITIAL_TIMEOUT)//Si son timeout est écoulé
                    sqLiteHelper.deleteMessage(messages.get(i));//Suppression du message
            }



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


    private void checkAndProcessBeamIntent(Intent intent){
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

            try {
                addNotKnownMessages(incMessages);
            } catch (InvalidKeyException | NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }

        }
    }

    public void addNotKnownMessages(ArrayList<Message> mess) throws IllegalBlockSizeException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException {
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
                if(mess.get(i).getPublicKeyDest().equals(KeysHelper.getMyPublicKey())) {//MESSAGE FOR ME

                    Message m = new Message(mess.get(i).getUuid(), CryptoHelper.RSADecryptByte(mess.get(i).getContent(), KeysHelper.getMyPrivateKey()), mess.get(i).getPublicKeySource(), mess.get(i).getPublicKeyDest());
                    sqLiteHelper.addMessageToChat(m);
                }
                else
                    sqLiteHelper.addMessage(mess.get(i));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
                if(messages.get(i).getSent() == false)
                    sqLiteHelper.updateSent(messages.get(i));

                Log.i("Tamere" , "sent : "+messages.get(i).getSent());

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

    public void debuglog(View view) {
        List<Message> convMessages = sqLiteHelper.getAllMessagesChat();
        List<Message> transitMessages = sqLiteHelper.getAllMessages();
        Log.i("DATABASE", "id --- content --- sent --- timeout ");
        for(int i = 0 ; i < convMessages.size() ; i++){
            double now = (double)System.currentTimeMillis()/1000.0;
            Message m = convMessages.get(i);
            Log.i("DATABASE conv", ""+m.getUuid()+" --- "+m.getContent()+" --- "+m.getSent()+" --- "+(now - m.getTimeout()));
        }
        for(int i = 0 ; i < transitMessages.size() ; i++){
            double now = (double)System.currentTimeMillis()/1000.0;
            Message m = transitMessages.get(i);
            Log.i("DATABASE trans", ""+m.getUuid()+" --- "+m.getContent()+" --- "+m.getSent()+" --- "+(now - m.getTimeout()));
        }

    }
}

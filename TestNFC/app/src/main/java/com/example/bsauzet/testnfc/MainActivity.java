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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * The main activity
 */
public class MainActivity extends Activity{

    ListView lv;
    NfcAdapter nfcAdapter;

    SQLiteHelper sqLiteHelper;

    ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        users = new ArrayList<User>();
        lv = (ListView)findViewById(R.id.listViewConversation);

        //Generate private and public keys if they do not exist (first launch)
        if(KeysHelper.getMyPublicKey() == null)
            KeysHelper.generateKeys(getApplicationContext());

        sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        double now = (double)System.currentTimeMillis()/1000.0;

        List<Message> messages = sqLiteHelper.getAllMessages();
        for (int i = 0 ; i < messages.size(); i++)
            if(messages.get(i).getSent())//if the message has been sent once (at least)
                if (now - messages.get(i).getTimeout() > Global.INITIAL_TIMEOUT)//If the timeout of the message is over
                    sqLiteHelper.deleteMessage(messages.get(i));//Delete the message

        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override public NdefMessage createNdefMessage(NfcEvent event) {
                return createNdefMessageAllMessages();
            }
        }, this);

        updateView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,  View view, int position, long id) {;
                String itemValue = (String) lv.getItemAtPosition(position);
                goToBrowseMessagesActivity(itemValue);
            }

        });

        checkAndProcessBeamIntent(intent);

    }
    public void updateView(){
        //fetch all messages concerning me
        List<Message> messages = sqLiteHelper.getMessagesChatFromPublicKeyDestAndSource(KeysHelper.getMyPublicKey());
        QuickSortHelper.sortMessagesByDate(messages);
        users = new ArrayList<User>();
        //Updating the conversations view
        for(int i = 0 ; i < messages.size() ; i++)
            if(!localUserExists(messages.get(i).getPublicKeySource())){
                User temp = sqLiteHelper.getUserByPublicKey(messages.get(i).getPublicKeySource());

                if(messages.get(i).getPublicKeySource().equals(KeysHelper.getMyPublicKey())){
                    if(!localUserExists(messages.get(i).getPublicKeyDest())){
                        User u = sqLiteHelper.getUserByPublicKey(messages.get(i).getPublicKeyDest());
                        users.add(u);
                    }
                }
                else if(temp != null)
                    users.add(temp);
                else {
                    User u = new User("Unknown(" + nextUnknownId() + ")", messages.get(i).getPublicKeySource());
                    users.add(u);
                    sqLiteHelper.addUser(u);
                }
            }


        String[] lv_arr = new String[users.size()];

        for(int i = 0 ; i < users.size() ; i++)
            lv_arr[i] = users.get(i).getName();

        lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, lv_arr));
    }

    /**
     * Manage the Unregistered users by umerating them
     * @return
     */
    public int nextUnknownId(){
        int cmp = 0;

        for(int i = 0 ; i < users.size() ; i++)
            if(users.get(i).getName().toLowerCase().contains("Unknown".toLowerCase()))
                cmp++;

        return cmp+1;
    }

    /**
     *
     * @param name
     */
    public void goToBrowseMessagesActivity(String name){
        Intent intent = new Intent(this, BrowseMessages.class);
        User u = getUserByName(name);

        if(u != null) {
            intent.putExtra("name", u.getName());
            intent.putExtra("publicKey", u.getPublicKey());
            intent.setAction("NewActivity");
            startActivity(intent);
        }
    }

    /**
     * get Users to render the list of conversations
     * @param name
     * @return
     */
    public User getUserByName(String name){
        User u = null;
        for(int i = 0 ; i < users.size() ; i++)
            if(users.get(i).getName().equals(name)){
                u = users.get(i);
                break;
            }
        return u;
    }

    /**
     * Used to avoid duplications of conversations while fetching messages
     * @param publicKey
     * @return
     */
    public boolean localUserExists(String publicKey){
        boolean exist = false;
        for(int i = 0 ; i < users.size() ; i++)
            if(users.get(i).getPublicKey().equals(publicKey)) {
                exist = true;
                break;
            }
        return exist;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();

        debuglog();

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

    /**
     * While receiving messages, we check what are the new messages and those we already got in base.
     * If the user receives a delete signal for a message he sent, it will show him that the message has arrived to destination
     * @param intent
     */
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
                double date = Double.parseDouble(new String(inNdefRecords[j].getPayload()));
                j++;
                incMessages.add(new Message(uuid, message, source, dest, date));
            }
            temp = new String(inNdefRecords[j].getPayload());
            int nbSignals = Integer.valueOf(temp);
            j++;

            for(int i = 0 ; i < nbSignals ; i++) {
                String uui = new String (inNdefRecords[j].getPayload());
                sqLiteHelper.addSignal(new Signal(uui));
                j++;

                Message m = sqLiteHelper.getMessage(uui);
                if(m != null)
                    sqLiteHelper.deleteMessage(m);

                m = sqLiteHelper.getMessageChat(uui);
                if(m!=null) {
                    if (m.getPublicKeySource().equals(KeysHelper.getMyPublicKey())) {
                        Toast.makeText(MainActivity.this, "Accusé reception", Toast.LENGTH_SHORT).show();
                    }
                }


            }

            try {
                addNotKnownMessages(incMessages);
            } catch (InvalidKeyException | NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Used to fetch messages : messages for the fetching user will go to the 'Chat' database to be rendered on the main activity,
     * others will remain hidden in the 'Messages' database
     * @param mess
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public void addNotKnownMessages(ArrayList<Message> mess) throws IllegalBlockSizeException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException {
        List<Message> myMessages = sqLiteHelper.getAllMessages();
        myMessages.addAll(sqLiteHelper.getAllMessagesChat());
        for(int i = 0 ; i < mess.size() ; i++) {
            boolean newMess = true;

            for (int j = 0; j < myMessages.size(); j++)
                if (mess.get(i).getUuid().equals(myMessages.get(j).getUuid())) {
                    newMess = false;
                    break;
                }


            if(newMess) {
                //If the message is for me
                if(mess.get(i).getPublicKeyDest().equals(KeysHelper.getMyPublicKey())) {
                    //Add the message to the chat database table
                    Message m = new Message(mess.get(i).getUuid(), CryptoHelper.RSADecryptByte(mess.get(i).getContent(), KeysHelper.getMyPrivateKey()), mess.get(i).getPublicKeySource(), mess.get(i).getPublicKeyDest(), mess.get(i).getDate());
                    sqLiteHelper.addMessageToChat(m);

                    sqLiteHelper.addSignal(new Signal(m.getUuid()));
                    Toast.makeText(MainActivity.this, "New message !", Toast.LENGTH_SHORT).show();
                }
                else {
                    sqLiteHelper.addMessage(mess.get(i));//Add the message to the messages database table (we are an intermediate)
                }
            }
            else{

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

    public void onDestroy(){
        super.onDestroy();
    }

    /**
     *
     * @param view
     */
    public void goToAddContact(View view) {
        Intent intent = new Intent(this, AddContactActivity.class);
        intent.setAction("NewActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }

    /**
     *
     * @param view
     */
    public void browseContactsButton(View view) {
        Intent intent = new Intent(this, BrowseContacts.class);
        intent.setAction("NewActivity");
        startActivity(intent);
    }

    /**
     * Prepares messages and signals to be sent
     * @return
     */
    public NdefMessage createNdefMessageAllMessages(){

        List<Message> messages = sqLiteHelper.getAllMessages();
        List<Signal> signals = sqLiteHelper.getAllSignals();

        String nbMessages = String.valueOf(messages.size());
        String nbSignals = String.valueOf(signals.size());

        NdefRecord[] ndefRecords = new NdefRecord[(messages.size() * 5) + 1 + (signals.size() +1)];//(5 champs * nb messages) + champ "nbMessages" + (1 champ * nb signals) + "nbsignals"

        //add nb of messages
        ndefRecords[0] = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[]{},
                nbMessages.getBytes());

        int j = 1;
        if(!messages.isEmpty()) {
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getSent() == false)
                    sqLiteHelper.updateSent(messages.get(i));


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
                ndefRecords[j] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        String.valueOf(messages.get(i).getDate()).getBytes());
                j++;
            }
        }
        //add nb of signals
        ndefRecords[j] = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[]{},
                nbSignals.getBytes());
        j++;

        if(!signals.isEmpty()){
            for(int i = 0 ; i < signals.size() ; i++) {
                ndefRecords[j] = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        "text/plain".getBytes(),
                        new byte[]{},
                        signals.get(i).getUuid().getBytes());
                j++;
            }
        }

        NdefMessage ndefMessageout = new NdefMessage(ndefRecords);

        return ndefMessageout;

    }

    /**
     * displays the database content on the Log
     */
    public void debuglog() {
        List<Message> convMessages = sqLiteHelper.getAllMessagesChat();
        List<Message> transitMessages = sqLiteHelper.getAllMessages();
        List<Signal> signals = sqLiteHelper.getAllSignals();

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
        for(int i = 0 ; i < signals.size() ; i++){
            Signal s = signals.get(i);
            Log.i("DATABASE signal", "uuid :"+s.getUuid());
        }

    }
}

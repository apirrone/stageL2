package com.example.bsauzet.testnfc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class BrowseMessages extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;

    String userName;
    String userPk;
    String itemToDelete;

    String myPublicKey;


    EditText mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_messages);

        Intent intent = getIntent();

        userName = intent.getStringExtra("name");
        userPk = intent.getStringExtra("publicKey");

        TextView tv = (TextView)findViewById(R.id.contactName);
        tv.setText(userName);

        lv = (ListView)findViewById(R.id.listView);
        sqLiteHelper = new SQLiteHelper(this);
        mEdit = (EditText)findViewById(R.id.editText);


        myPublicKey = KeysHelper.getMyPublicKey();

        updateView();

        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               itemToDelete = (String) lv.getItemAtPosition(position);

                new AlertDialog.Builder(BrowseMessages.this)
                        .setTitle("Delete message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            List<Message> messagesToDelete = sqLiteHelper.getMessagesChatFromContentAndSender(itemToDelete, userName );
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i = 0 ; i < messagesToDelete.size() ; i++)
                                    sqLiteHelper.deleteMessageChat(messagesToDelete.get(i));

                                updateView();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return false;
            }
        });


    }

    public void updateView(){
        final List<Message> messages = sqLiteHelper.getMessagesChatConcerningUser(userPk);

        if(messages != null) {
            ArrayList<Message> temp = new ArrayList<Message>();

            for (int i = 0; i < messages.size(); i++) {
                //MESSAGE FOR ME
                if (messages.get(i).getPublicKeyDest().equals(myPublicKey)){
                    temp.add(messages.get(i));
                }
                //MESSAGE BY ME
                if (messages.get(i).getPublicKeySource().equals(myPublicKey)) {
                    Message m = messages.get(i);
                        temp.add(m);
                }
            }

            if(temp.size()>0){
                String[] lv_arr = new String[temp.size()];
                for(int i = 0 ; i < temp.size() ; i++)
                    lv_arr[i] = new String(temp.get(i).getContent());
                lv.setAdapter(new ArrayAdapter<String>(BrowseMessages.this, android.R.layout.simple_list_item_1, lv_arr));
            }
//            Log.i("TAMERE", "temp.size() : " + temp.size() + " --- lv.size : " + (lv.getFirstVisiblePosition()));
//            for(int i = 0 ; i < lv.getLastVisiblePosition() - lv.getFirstVisiblePosition() ; i++){
//                if(temp.get(i).getPublicKeySource().equals(myPublicKey)) {
//                    lv.getChildAt(i).setBackgroundColor(0xAA5F82A6);
//                }
//            }

        }
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View rowView;
//
//        if(convertView == null){
//            LayoutInflater inflator = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            rowView = inflator.inflate(R.layout.mylistview, null);
//
//        } else {
//            rowView = (View) convertView;
//        }
//
//        TextView myTv = (TextView) rowView.findViewById(R.id.tvCode);
//
//        LinearLayout ActiveItem = (LinearLayout) rowView;
//        if(position == selectedItem)
//            ActiveItem.setBackgroundColor(Color.red(255));
//
//
//    }
//
//    public void setSelectedItem(int position) {
//        selectedItem = position;
//    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public byte[] getEncryptedMessage(String message){
        try {
            return CryptoHelper.RSAEncrypt(message, userPk);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendButton(View view) {
        byte[] text = getEncryptedMessage(mEdit.getText().toString());
        Message messageEncr = new Message(text, myPublicKey, userPk);
        Message messageCl = new Message(mEdit.getText().toString().getBytes(), myPublicKey, userPk);


        sqLiteHelper.addMessage(messageEncr);
        sqLiteHelper.addMessageToChat(messageCl);
        Toast.makeText(BrowseMessages.this, "Message sent", Toast.LENGTH_SHORT).show();
        updateView();
    }
}

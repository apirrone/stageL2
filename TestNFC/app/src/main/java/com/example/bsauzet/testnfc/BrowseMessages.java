package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class BrowseMessages extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;

    String userName;
    String userPk;
    String itemToDelete;

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

        final List<Message> messages = sqLiteHelper.getMessagesFromPublicKeySource(userPk);

        if(messages != null) {
            ArrayList<String> temp = new ArrayList<String>();

            for (int i = 0; i < messages.size(); i++)
                if(messages.get(i).getPublicKeyDest().equals(KeysHelper.getMyPublicKey())) {
                    try {
                        temp.add(CryptoHelper.RSADecrypt(messages.get(i).getContent(), KeysHelper.getMyPrivateKey()));
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchProviderException e) {
                        e.printStackTrace();
                    }
                }
            if(temp.size()>0){
                String[] lv_arr = new String[temp.size()];
                for(int i = 0 ; i < temp.size() ; i++)
                    lv_arr[i] = temp.get(i);
                lv.setAdapter(new ArrayAdapter<String>(BrowseMessages.this, android.R.layout.simple_list_item_1, lv_arr));
            }

        }
        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               itemToDelete = (String) lv.getItemAtPosition(position);

                new AlertDialog.Builder(BrowseMessages.this)
                        .setTitle("Delete message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            List<Message> messagesToDelete = sqLiteHelper.getMessagesFromContentAndSender(itemToDelete, userName);
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i = 0 ; i < messagesToDelete.size() ; i++)
                                    sqLiteHelper.deleteMessage(messages.get(i));
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

}

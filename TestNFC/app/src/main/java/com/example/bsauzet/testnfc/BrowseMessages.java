package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class BrowseMessages extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;

    String userName;
    String userPk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_messages);

        Intent intent = getIntent();
        userName = intent.getStringExtra("name");
        userPk = intent.getStringExtra("publicKey");

        lv = (ListView)findViewById(R.id.listView);
        sqLiteHelper = new SQLiteHelper(this);
        List<Message> messages = sqLiteHelper.getMessagesFromPublicKeySource(userPk);
        String[] lv_arr = new String[messages.size()];
        for(int i = 0 ; i < messages.size() ; i++){
            try {
                lv_arr[i] = CryptoHelper.RSADecrypt(messages.get(i).getContent(), KeysHelper.getMyPrivateKey());
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
        lv.setAdapter(new ArrayAdapter<String>(BrowseMessages.this,
                android.R.layout.simple_list_item_1, lv_arr));
    }

}

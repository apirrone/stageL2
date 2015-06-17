package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


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
        myPk = KeysHelper.getMyPublicKey();
        mEdit = (EditText)findViewById(R.id.editText);
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
        byte[] text = getEncryptedMessage(mEdit.getText().toString());
        Message messageEncr = new Message(text, myPk, destPk);
        Message messageCl = new Message(mEdit.getText().toString().getBytes(), myPk, destPk);


        sqLiteHelper.addMessage(messageEncr);
        sqLiteHelper.addMessageToChat(messageCl);
        Toast.makeText(SendMessage.this, "Message sent", Toast.LENGTH_SHORT).show();
        finish();

    }

    public byte[] getEncryptedMessage(String message){
        try {
            return CryptoHelper.RSAEncrypt(message, destPk);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

}

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
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class SendMessage extends Activity {

    private EditText mEdit;
    String destName;
    String destPk;
    String myPk;

    SQLiteHelper sqLiteHelper;

    /**
     * Fetches information concerning the person the user wants to chat with
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        Intent intent = getIntent();

        sqLiteHelper = SQLiteHelper.getInstance(getApplicationContext());
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

    /**
     * Triggers the sending of a message
     * @param view
     */
    public void sendButton(View view) {
        byte[] text = getEncryptedMessage(mEdit.getText().toString());
        Message message = new Message(text, myPk, destPk);
        sqLiteHelper.addMessage(message);

        message.setContent(mEdit.getText().toString().getBytes());
        sqLiteHelper.addMessageToChat(message);
        Toast.makeText(SendMessage.this, "Message sent", Toast.LENGTH_SHORT).show();
        finish();

    }

    /**
     *
     * @param message
     * @return the message encrypted in a byte array
     */
    public byte[] getEncryptedMessage(String message){
        try {
            return CryptoHelper.RSAEncrypt(message, destPk);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

}

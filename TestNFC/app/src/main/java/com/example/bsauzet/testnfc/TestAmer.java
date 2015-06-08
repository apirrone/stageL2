package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;


public class TestAmer extends Activity {
    EditText mEdit;
    TextView tv;
    CryptoHelper cryptoHelper;
    byte[] bitz;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_amer);

        mEdit = (EditText)findViewById(R.id.editText2);
        tv = (TextView)findViewById(R.id.textView2);
        cryptoHelper = new CryptoHelper();
        generateKeys();

    }



    public void generateKeys() {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();

        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            assert kpg != null;
            kpg.initialize(new KeyPairGeneratorSpec.Builder(getApplicationContext())
                    .setAlias("Keys")
                    .setStartDate(now)
                    .setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal("CN=test1"))
                    .setKeySize(4096)
                    .build());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        kpg.generateKeyPair();

//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(4096);
//        KeyPair keyPair = keyPairGenerator.generateKeyPair();
//        PublicKey publicKey = keyPair.getPublic();
//        PrivateKey privateKey = keyPair.getPrivate();
//
//        KeyStore keyStore = KeyStore.getInstance("RSA");
//        keyStore.load(null, null);
//        keyStore.setKeyEntry("privateKey", (Key)privateKey, "caca".toCharArray(), null);



    }

//    public X509Certificate generateCertificate(KeyPair keyPair){
//        X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
//        cert.setSerialNumber(BigInteger.valueOf(1));   //or generate a random number
//        cert.setSubjectDN(new X509Principal("CN=localhost"));  //see examples to add O,OU etc
//        cert.setIssuerDN(new X509Principal("CN=localhost")); //same since it is self-signed
//        cert.setPublicKey(keyPair.getPublic());
//        cert.setNotBefore(<date>);
//        cert.setNotAfter(<date>);
//        cert.setSignatureAlgorithm("SHA1WithRSAEncryption");
//        PrivateKey signingKey = keyPair.getPrivate();
//        return cert.generate(signingKey, "BC");
//    }


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

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        if(publicKey != null) {
            return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
        }
        else return null;
    }

    public PrivateKey getMyPrivateKey(){
        KeyStore ks = null;
        PrivateKey privateKey = null;
        String output = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);


            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry("Keys", null);
            if(keyEntry != null)
                privateKey = keyEntry.getPrivateKey();

        } catch (KeyStoreException | NoSuchAlgorithmException | IOException | CertificateException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        if(privateKey != null)
            return privateKey;
        else
            return null;
    }



    public void EncryptButton(View view) {
        try {
            bitz = CryptoHelper.RSAEncrypt(mEdit.getText().toString(), getMyPublicKey());
            String decoded = new String(bitz, "Cp1252");
            tv.setText(decoded);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | InvalidKeySpecException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void DecryptButton(View view) {
        try {
            tv.setText(CryptoHelper.RSADecrypt(bitz, getMyPrivateKey()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

}

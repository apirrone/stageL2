package com.example.bsauzet.testnfc;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

/**
 * Created by bsauzet on 08/06/15.
 */
public class KeysHelper {

    public static String getMyPublicKey(){
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

    public static PrivateKey getMyPrivateKey(){
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



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void generateKeys(Context context){
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
            kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
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
    }
}

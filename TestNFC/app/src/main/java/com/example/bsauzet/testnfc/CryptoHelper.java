package com.example.bsauzet.testnfc;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by bsauzet on 05/06/15.
 * Here are defined all the methods concerning encryption/decryption
 *
 */
public class CryptoHelper {

    /**
     * Encrypts the message the user wants to send thanks to the RSA algorithm with the recipients' 4096bits public key
     * @see java.security.interfaces.RSAPublicKey
     * @param plain
     * @param publicKeyString
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public static byte[] RSAEncrypt(final String plain, String publicKeyString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeySpecException {

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.decode(publicKeyString, Base64.DEFAULT));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(plain.getBytes());
    }


    /**
     *
     * Decrypts a message encrypted with the user's public key by using their private key
     * @see java.security.interfaces.RSAPrivateKey
     *
     * @param encBarr
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws NoSuchProviderException
     */
    public static String RSADecrypt(final byte[] encBarr, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchProviderException {
        String res = null;

        Cipher cipher1 = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decBarr = new byte[0];
        try {
            decBarr = cipher1.doFinal(encBarr);
        } catch (BadPaddingException e) {
            Log.i("myApp", "BadPadding");
            e.printStackTrace();
        }

        if(decBarr!=null)
             res = new String(decBarr);

        return res;
    }

    /**
     *  Decrypts a byte array encrypted with the user's public key by using their private key (RSA)
     *
     * @see java.security.PrivateKey
     * @param encBarr
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws NoSuchProviderException
     */
    public static byte[] RSADecryptByte(final byte[] encBarr, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchProviderException {

        Cipher cipher1 = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decBarr = new byte[0];
        try {
            decBarr = cipher1.doFinal(encBarr);
        } catch (BadPaddingException e) {
            Log.i("myApp", "BadPadding");
            e.printStackTrace();
        }
        return decBarr;
    }


}


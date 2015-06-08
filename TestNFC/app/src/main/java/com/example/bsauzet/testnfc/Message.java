package com.example.bsauzet.testnfc;

import java.security.PublicKey;
import java.util.UUID;

/**
 * Created by Antoine on 29/05/2015.
 */
public class Message {


    private String uuid;
    private String publicKeySource;
    private String publicKeyDest;
    private byte[] content;

    public Message(byte[] content, String publicKeySource, String publicKeyDest) {
        this.uuid = UUID.randomUUID().toString();
        this.publicKeySource = publicKeySource;
        this.publicKeyDest = publicKeyDest;
        this.content = content;
    }
    public Message(String uuid, byte[] content, String publicKeySource, String publicKeyDest) {
        this.uuid = uuid;
        this.publicKeySource = publicKeySource;
        this.publicKeyDest = publicKeyDest;
        this.content = content;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPublicKeySource() {
        return publicKeySource;
    }

    public void setPublicKeySource(String publicKeySource) {
        this.publicKeySource = publicKeySource;
    }

    public String getPublicKeyDest() {
        return publicKeyDest;
    }

    public void setPublicKeyDest(String publicKeyDest) {
        this.publicKeyDest = publicKeyDest;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }





}

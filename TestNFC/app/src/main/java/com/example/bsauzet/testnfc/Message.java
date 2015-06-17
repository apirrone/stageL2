package com.example.bsauzet.testnfc;

import java.util.UUID;

/**
 * Created by Antoine on 29/05/2015.
 */
public class Message {

    private String uuid;
    private String publicKeySource;
    private String publicKeyDest;
    private byte[] content;
    private double timeout;
    private boolean sent;

    //Constructor that generates UUID, sets Timeout and set sent to false
    public Message(byte[] content, String publicKeySource, String publicKeyDest) {
        this.uuid = UUID.randomUUID().toString();
        this.publicKeySource = publicKeySource;
        this.publicKeyDest = publicKeyDest;
        this.content = content;
        this.timeout = (double)System.currentTimeMillis()/1000.0;
        this.sent = false;

    }

    //Construtor that copies all parameters
    public Message(String uuid, byte[] content, String publicKeySource, String publicKeyDest, double timeout, boolean sent) {
        this.uuid = uuid;
        this.publicKeySource = publicKeySource;
        this.publicKeyDest = publicKeyDest;
        this.content = content;
        this.timeout = timeout;
        this.sent = sent;
    }

    //Constructor that copies UUID but sets timeout and sent
    public Message(String uuid, byte[] content, String publicKeySource, String publicKeyDest) {
        this.uuid = uuid;
        this.publicKeySource = publicKeySource;
        this.publicKeyDest = publicKeyDest;
        this.content = content;
        this.timeout = (double)System.currentTimeMillis()/1000.0;
        this.sent = false;
    }


    //GETTERS AND SETTERS

    public String getUuid() {
        return uuid;
    }

    public String getPublicKeySource() {
        return publicKeySource;
    }

    public String getPublicKeyDest() {
        return publicKeyDest;
    }
    public byte[] getContent() {
        return content;
    }

    public double getTimeout() {
        return timeout;
    }

    public boolean getSent() {
        return sent;
    }
}

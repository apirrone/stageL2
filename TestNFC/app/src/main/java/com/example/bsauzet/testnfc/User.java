package com.example.bsauzet.testnfc;

/**
 * Created by Antoine on 28/05/2015.
 */
public class User {

    private String name;
    private String publicKey;

    public User(String name, String publicKey) {
        this.name = name;
        this.publicKey = publicKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey;
    }

}



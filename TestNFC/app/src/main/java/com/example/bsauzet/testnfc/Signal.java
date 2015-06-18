package com.example.bsauzet.testnfc;

/**
 * Created by Antoine on 18/06/2015.
 */
public class Signal {

    private String uuid;
    private double date;

    public Signal(String uuid){
        this.uuid = uuid;
        this.date = System.currentTimeMillis()/1000.0;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getDate() {
        return date;
    }

    public void setDate(double date) {
        this.date = date;
    }
}

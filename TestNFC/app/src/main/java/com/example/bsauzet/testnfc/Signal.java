package com.example.bsauzet.testnfc;

/**
 * Created by Antoine on 18/06/2015.
 */
public class Signal {

    private String uuid;
    private double date;

    /**
     * Creating signal from the uuid of the message the signal will delete, and the timeout before expiration of the signal
     * @param uuid
     */
    public Signal(String uuid){
        this.uuid = uuid;
        this.date = System.currentTimeMillis()/1000.0;
    }

    public String getUuid() {
        return uuid;
    }

    public double getDate() {
        return date;
    }

    public void setDate(double date) {
        this.date = date;
    }
}

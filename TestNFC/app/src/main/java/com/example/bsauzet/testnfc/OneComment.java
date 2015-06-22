package com.example.bsauzet.testnfc;

public class OneComment {
    public boolean left;
    public String comment;

    /**
     *
     * @param left if the message shall be displayed on the left or on the right side of the screen
     * @param comment content of the message
     */
    public OneComment(boolean left, String comment) {
        super();
        this.left = left;
        this.comment = comment;
    }

    public String getComment(){
        return comment;
    }

}
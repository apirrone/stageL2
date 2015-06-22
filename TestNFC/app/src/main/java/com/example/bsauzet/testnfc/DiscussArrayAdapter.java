
package com.example.bsauzet.testnfc;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DiscussArrayAdapter extends ArrayAdapter<OneComment> {

    private TextView countryName;
    private List<OneComment> countries = new ArrayList<OneComment>();
    private LinearLayout wrapper;

    /**
     * adds the conversation bubble
     * @param object
     */
    @Override
    public void add(OneComment object) {
        countries.add(object);
        super.add(object);
    }

    public DiscussArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    /**
     *
     * @return the number of bubbles in conversation (number of messages exchanged between 2 persons)
     */
    public int getCount() {
        return this.countries.size();
    }

    /**
     * Returns the comment located at the 'index' location
     * @param index
     * @return
     */
    public OneComment getItem(int index) {
        return this.countries.get(index);
    }

    /**
     * Manages the display
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listitem_discuss, parent, false);
        }

        wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

        OneComment coment = getItem(position);

        countryName = (TextView) row.findViewById(R.id.comment);

        countryName.setText(coment.comment);

        countryName.setBackgroundResource(coment.left ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        countryName.setTextColor(coment.left ? Color.BLACK : Color.WHITE);
        wrapper.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);

        return row;
    }


}
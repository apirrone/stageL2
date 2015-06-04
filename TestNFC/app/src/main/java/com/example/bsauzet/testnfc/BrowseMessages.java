package com.example.bsauzet.testnfc;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class BrowseMessages extends Activity {

    ListView lv;
    SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_messages);

        lv = (ListView)findViewById(R.id.listView);
        sqLiteHelper = new SQLiteHelper(this);
        List<Message> messages = sqLiteHelper.getAllMessages();
        String[] lv_arr = new String[messages.size()];
        for(int i = 0 ; i < messages.size() ; i++){
            lv_arr[i] = messages.get(i).getContent();
        }
        lv.setAdapter(new ArrayAdapter<String>(BrowseMessages.this,
                android.R.layout.simple_list_item_1, lv_arr));
    }

}

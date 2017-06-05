package com.example.son.timetable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Jeon on 2017-06-04.
 */

public class Setting_list extends AppCompatActivity{
    static final String[] List_Menu = {"서비스"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        getSupportActionBar().setTitle("Setting"); //title 이름

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, List_Menu);
        ListView listview = (ListView)findViewById(R.id.setting);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strText = (String)parent.getItemAtPosition(position);

                if(strText.equals("서비스")){
                    Log.i("aaa", "Service");
                    //Intent Service_intent = new Intent(this, Setting_service.class);
                    //startActivity(Service_intent);
                }
            }
        });
    }
}

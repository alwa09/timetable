package com.example.son.timetable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Jeon on 2017-06-04.
 */

public class Setting_list extends AppCompatActivity{
    static final String[] menu_list = {"서비스", "장소 등록", "장소 삭제"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        getSupportActionBar().setTitle("Setting"); //title 이름

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, menu_list);
        ListView listview = (ListView) findViewById(R.id.setting);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strText = (String)parent.getItemAtPosition(position);
                if(strText.equals("서비스")){
                    Intent Service_intent = new Intent(getApplicationContext(), Switch_check.class);
                    startActivity(Service_intent);
                }
                else if(strText.equals("장소 등록"))
                {
                    Intent placeRegister = new Intent(getApplicationContext(), RegisterPlace.class);
                    startActivity(placeRegister);
                }else if(strText.equals("장소 삭제"))
                {
                    Intent placeDelete = new Intent(getApplicationContext(), DeletePlace.class);
                    startActivity(placeDelete);
                }
            }
        });
    }
}

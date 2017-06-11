package com.example.son.timetable;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Jeon on 2017-06-04.
 */

public class Setting_list extends AppCompatActivity{
    static final String[] List_Menu = {"서비스 시작", "서비스 종료", "장소", "장소 등록", "장소 삭제"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        Toolbar toolbar= (Toolbar)findViewById(R.id.action_bar);
        if (toolbar!= null){
            toolbar.setTitleTextColor(Color.BLACK);
        }
        getSupportActionBar().setTitle("Setting"); //title 이름

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, List_Menu);
        final ListView listview = (ListView)findViewById(R.id.setting);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strText = (String)parent.getItemAtPosition(position);
                if(strText.equals("서비스 시작")){
                    if(!isServiceRunningCheck())
                    {
                        Intent Service_intent = new Intent(getApplicationContext(), MyService.class);
                        startService(Service_intent);
                    }
                    else
                        Toast.makeText(getApplicationContext(),"이미 서비스가 실행중입니다", Toast.LENGTH_SHORT).show();
                }
                else if(strText.equals("서비스 종료"))
                {
                    Intent Service_intent = new Intent(getApplicationContext(), MyService.class);
                    stopService(Service_intent);
                }
                else if(strText.equals("장소"))
                {
                    Intent placeListActivity = new Intent(getApplicationContext(), PlaceListActivity.class);
                    startActivity(placeListActivity);
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

    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.son.timetable.MyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

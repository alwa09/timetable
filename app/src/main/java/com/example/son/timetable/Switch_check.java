package com.example.son.timetable;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Jeon on 2017-06-06.
 */

public class Switch_check extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_list);

        Switch service_switch = (Switch)findViewById(R.id.switch_check);
        TextView textView = (TextView)findViewById(R.id.text);
        textView.setText("위치 서비스");

        service_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Log.i("aaa", "on");
                    Intent Service_intent = new Intent(getApplicationContext(), MyService.class);
                    startService(Service_intent);
                } else {
                    Log.i("aaa", "off");
                    Intent Service_intent = new Intent(getApplicationContext(), MyService.class);
                    stopService(Service_intent);
                }
            }
        });
    }
}

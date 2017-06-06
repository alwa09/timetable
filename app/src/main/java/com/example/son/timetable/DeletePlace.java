package com.example.son.timetable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Jeong on 2017-06-06.
 */

public class DeletePlace extends AppCompatActivity {
    ArrayList<String>placeName = new ArrayList<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deletelist);
        placeName = getStringArrayPref(getApplicationContext(),"place");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, placeName);
        ListView listview = (ListView)findViewById(R.id.setting);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String strText = (String)parent.getItemAtPosition(position);
                placeName.remove(strText);
                setStringArrayPref(getApplicationContext(), "place", placeName);
                finish();
            }
        });
    }

    private void setStringArrayPref(Context context, String key, ArrayList<String> values)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++)
        {
            a.put(values.get(i));
        }
        if (!values.isEmpty())
        {
            editor.putString(key, a.toString());
        } else
        {
            editor.putString(key, null);
        }
        editor.apply();
    }

    private ArrayList<String> getStringArrayPref(Context context, String key)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null)
        {
            try
            {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++)
                {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        } return urls;
    }


}

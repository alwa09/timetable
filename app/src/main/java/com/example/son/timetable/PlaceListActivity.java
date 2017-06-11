package com.example.son.timetable;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PlaceListActivity extends AppCompatActivity {
    ArrayList<String> placeName = new ArrayList<String>();
    final int REQUEST_CODE_REGISTED = 100;
    final int REQUEST_CODE_CANCELED = 101;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);
        Toolbar toolbar= (Toolbar)findViewById(R.id.action_bar);
        if (toolbar!= null){
            toolbar.setTitleTextColor(Color.BLACK);
        }
        getSupportActionBar().setTitle("장소");
        placeName = getStringArrayPref(getApplicationContext(),"place");
        /*
        if(placeName.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"등록한 장소가 없습니다.",Toast.LENGTH_SHORT).show();
            finish();
        }*/
        updateList();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_placelist, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_placeList_add:
                Intent placeRegister = new Intent(getApplicationContext(), RegisterPlace.class);
                startActivityForResult(placeRegister, REQUEST_CODE_REGISTED);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
        case REQUEST_CODE_REGISTED:
                updateList();
                break;
        }
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

    private void updateList()
    {
        placeName = getStringArrayPref(getApplicationContext(),"place");
        /*
        if(placeName.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"등록한 장소가 없습니다.",Toast.LENGTH_SHORT).show();
            finish();
        }*/
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, placeName);
        ListView listview = (ListView)findViewById(R.id.setting);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String strText = (String)parent.getItemAtPosition(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(PlaceListActivity.this);
                dialog.setTitle("장소 삭제")
                        .setMessage("이 장소를 삭제할까요?")
                        .setPositiveButton("네",
                                new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        placeName.remove(strText);
                                        setStringArrayPref(getApplicationContext(), "place", placeName);
                                        updateList();
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton("아니요",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                dialog.show();
            }
        });
    }
}

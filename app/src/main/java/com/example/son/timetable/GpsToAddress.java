package com.example.son.timetable;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GpsToAddress extends AsyncTask<Double, Void, String>{
    double latitude;
    double longitude;
    String adderess;

    private String getApiAddress()
    {
        String apiAddress = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                latitude +
                "," +
                longitude +
                "&language=ko&; // + API 키를 덧붙여서 사용합니다.
        return apiAddress;
    }

    private String getJSONData() throws Exception
    {
        String buf;
        String jsonString = new String();
        URL url = new URL(getApiAddress());
        URLConnection conn = url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        while((buf = br.readLine()) != null)
        {
            jsonString += buf;
        }
        return jsonString;
    }

    public String request() throws Exception
    {
        Log.d("JSON", getJSONData());
        JSONObject jobj = new JSONObject(getJSONData());
        JSONArray jarray = (JSONArray)jobj.get("results");
        jobj = (JSONObject)jarray.get(1);
        Log.d("JSON", jobj.getString("formatted_address"));
        return jobj.getString("formatted_address");
    }

    @Override
    protected String doInBackground(Double... params) {
        latitude = params[0];
        longitude = params[1];
        String result = null;
        try{
            result = request();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}

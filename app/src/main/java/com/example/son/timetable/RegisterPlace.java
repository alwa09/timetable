package com.example.son.timetable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Jeong on 2017-06-06.
 */

public class RegisterPlace extends AppCompatActivity {
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    final int REQUEST_CODE_REGISTED = 100;
    final int REQUEST_CODE_CANCELED = 101;
    public ArrayList<String>placeName = new ArrayList<String>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placeName = getStringArrayPref(getApplicationContext(),"place");
        openAutocompleteActivity();
        // Retrieve the TextViews that will display details about the selected place.
    }
    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e("TAG", message);
            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("TAG", "Place Selected: " + place.getName());
                placeName.add(place.getName().toString());
                setStringArrayPref(getApplicationContext(),"place",placeName);
                setResult(REQUEST_CODE_REGISTED);
                finish();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("TAG", "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
                setResult(REQUEST_CODE_CANCELED);
                finish();
            }
        }
    }

    /**
     * Helper method to format information about a place nicely.
     */
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e("TAG", res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

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

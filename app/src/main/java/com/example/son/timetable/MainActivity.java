package com.example.son.timetable;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SQLiteHelper dbHelper;
    String dbName = "timetable_db";
    int dbVersion = 1;
    private SQLiteDatabase db;
    String tag = "SQLite";
    LocationListener locationListener;
    LocationManager locationManager;
    double longitude;
    double latitude;
    Button.OnClickListener tableClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            String button_id = getResources().getResourceName(v.getId());
            String[] values = button_id.split("_");
            String day = values[1]; // 요일
            values = values[0].split("id/");
            String _class = values[1]; // 교시
            Log.d(tag, "입력될 값: " + _class + " " + day);
            showTimeTableDialog(_class, day);
            //insert(values[0], values[1], "테스트");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        GpsPermissionCheckForMashMallo();

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                1000*60*5, // 통지사이의 최소 시간간격 (miliSecond) , 이거 수정하면 몇초에 한번씩 땡겨올수 있음
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                1000*60*5, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);


        dbHelper = new SQLiteHelper(this, dbName, null , dbVersion);
        try {
            db = dbHelper.getWritableDatabase();
        }
        catch(SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, "데이터베이스를 얻어올 수 없음");
            finish();
        }

        select();
        updateTimeTable();
        registButtonFunc();


    }
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            SharedPreferences shared = getSharedPreferences("location",0);
            SharedPreferences.Editor editor = shared.edit();

            Log.d("test", "onLocationChanged, location:" + location);
            longitude = location.getLongitude(); //경도
            latitude = location.getLatitude();   //위도
            editor.putFloat("lat",(float)latitude);
            editor.putFloat("lon",(float) longitude);
            editor.commit();
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

            final Geocoder geocoder = new Geocoder(MainActivity.this);          // 현재는 주소를 받아오는 형식
            List<Address> list = null;
            try {
                list = geocoder.getFromLocation(
                        latitude, // 위도
                        longitude, // 경도
                        10); // 얻어올 값의 개수
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
            }
            if (list != null) {
                if (list.size()==0) {
                    //tv.setText("해당되는 주소 정보는 없습니다");
                } else {
                    //tv.setText(list.get(0).toString());
                }
            }




        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };
    void showTimeTableDialog(final String _class, final String day) // 입력창 띄우기
    {
        final EditText edittext = new EditText(this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("시간표 입력");
        alertDialog.setMessage("시간표를 입력하세요.");
        alertDialog.setView(edittext);
        alertDialog.setPositiveButton("입력", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                insert(_class, day, edittext.getText().toString());                     // 두번 넣을경우 다 들어간다 중복처리 해줘야함.
                updateTimeTable();
            }
        });
        alertDialog.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.execSQL("delete from timetable where class='"+_class+"' and day='"+day+"'");
                String button_id = _class +"_"+day;
                //Log.d(tag, button_id);
                Button btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
                btn.setText("");
            }
        });
        alertDialog.show();
    }

    void insert(String _class, String day, String lecture) // DB에 삽입
    {
        if(checkDup(_class, day)==0)
            db.execSQL("insert into timetable VALUES(null, '" + _class + "','" + day + "','" + lecture + "');");
        else
        {
            db.execSQL("update timetable set lecture='"+lecture+"' where class='"+_class+"' and day='"+day+"'");
            Log.d(tag, "update");
        }
    }

    int checkDup(String _class, String day)
    {
        String query = "select * from timetable where class='" + _class + "' and day='"+day+"'";
        Cursor c = db.rawQuery(query, null);
        Log.d(tag, "dup: " + query);
        Log.d(tag, "dup: "+c.getCount());
        return c.getCount();
    }
    void select() // 임시 기능임
    {
        Cursor c = db.rawQuery("select * from timetable;", null);
        while(c.moveToNext())
        {
            int id = c.getInt(0);
            String _class = c.getString(1);
            String day = c.getString(2);
            String lecture = c.getString(3);
            //Log.d(tag, id + " " + _class + " " + day + " " + lecture);
        }
    }

    void updateTimeTable()
    {
        Cursor c = db.rawQuery("select * from timetable;", null);
        while(c.moveToNext())
        {
            int id = c.getInt(0);
            String _class = c.getString(1);
            String day = c.getString(2);
            String lecture = c.getString(3);
            Log.d(tag, id + " " + _class + " " + day + " " + lecture);

            String button_id = _class +"_"+day;
            Log.d(tag, button_id);
            Button btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setText(lecture);
        }
    }

    void registButtonFunc()
    {
        String button_id;
        Button btn;
        for(int i=1; i<10; i++)
        {
            button_id = "class" + i +"_"+ "monday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setOnClickListener(tableClickListener);
            button_id = "class" + i +"_"+ "tuesday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setOnClickListener(tableClickListener);
            button_id = "class" + i +"_"+ "wednesday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setOnClickListener(tableClickListener);
            button_id = "class" + i +"_"+ "thursday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setOnClickListener(tableClickListener);
            button_id = "class" + i +"_"+ "friday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setOnClickListener(tableClickListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_setting:
                Intent setting_intent = new Intent(this, Setting_list.class);
                startActivity(setting_intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void GpsPermissionCheckForMashMallo() {

        //마시멜로우 버전 이하면 if문에 걸리지 않습니다.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
            alertDialog.setTitle("GPS 사용 허가 요청");
            alertDialog.setMessage("앰버요청 발견을 알리기위해서는 사용자의 GPS 허가가 필요합니다.\n('허가'를 누르면 GPS 허가 요청창이 뜹니다.)");
            // OK 를 누르게 되면 설정창으로 이동합니다.
            alertDialog.setPositiveButton("허가",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        }
                    });
            // Cancle 하면 종료 합니다.
            alertDialog.setNegativeButton("거절",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();
        }
    }
}

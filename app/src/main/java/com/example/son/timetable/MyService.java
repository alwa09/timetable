package com.example.son.timetable;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MyService extends Service {
    NotificationManager Notifi_M;
    ServiceThread thread;
    rightPlace right;
    Notification Notifi;
    SQLiteHelper dbHelper;
    SQLiteDatabase db;
    AudioManager mAudioManager;
    LocationManager mLocationManager;
    myServiceHandler mHandler;
    LocationListener mLocationListener;
    double longitude;
    double latitude;

    boolean mode_change_flag = false;
    int prev_mode = 0;
    String dbName = "timetable_db";
    int dbVersion = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        dbHelper = new SQLiteHelper(this, dbName, null, dbVersion);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        prev_mode = mAudioManager.getMode();
        mHandler = new myServiceHandler();
        try {
            db = dbHelper.getReadableDatabase();
        } catch (SQLiteException e) {
            Log.d("service", "fail read database");
        }
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //여기서 위치값이 갱신되면 이벤트가 발생한다.
                //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

                Log.d("test", "onLocationChanged, location:" + location);
                longitude = location.getLongitude(); //경도
                latitude = location.getLatitude();   //위도
                double altitude = location.getAltitude();   //고도
                float accuracy = location.getAccuracy();    //정확도
                String provider = location.getProvider();   //위치제공자
                //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                //Network 위치제공자에 의한 위치변화
                //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                100, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                100, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        thread = new ServiceThread(mHandler);
        thread.start();

        return START_STICKY;

    }

    //서비스가 종료될 때 할 작업

    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            //Intent intent1 = new Intent(MyService.this, rightPlace.class);
            //rightPlace r = new rightPlace();
            Intent intent = new Intent(MyService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notifi = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Content Title")
                    .setContentText("Content Text")
                    .setSmallIcon(R.drawable.logo)
                    .setTicker("알림!!!")
                    .setContentIntent(pendingIntent)
                    .build();

            //소리추가

            Notifi.defaults = Notification.DEFAULT_SOUND;
            //알림 소리를 한번만 내도록
            Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;
            //확인하면 자동으로 알림이 제거 되도록
            Notifi.flags = Notification.FLAG_AUTO_CANCEL;
            Notifi_M.notify(777, Notifi);
            //토스트 띄우기
            try {
                //String s = new GpsToAddress().execute(36.145639, 128.392385).get(); // 주소를 가져옴
                Log.d("JSON", "위도: " + latitude + " 경도: " + longitude);
                String s = new GpsToAddress().execute(latitude, longitude).get();
                Toast.makeText(MyService.this, s, Toast.LENGTH_SHORT).show();
                Log.d("JSONYO", s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            isShutTime(); // 시간표대로 모드 전환하는 함수
        }


        public String getDayOfWeek() // 요일 구하는 함수
        {
            Calendar cal = Calendar.getInstance();
            String day = null;
            int nWeek = cal.get(Calendar.DAY_OF_WEEK);
            switch (nWeek) {
                case 1:
                    day = "sunday";
                    break;
                case 2:
                    day = "monday";
                    break;
                case 3:
                    day = "tuesday";
                    break;
                case 4:
                    day = "wednesday";
                    break;
                case 5:
                    day = "thursday";
                    break;
                case 6:
                    day = "friday";
                    break;
                case 7:
                    day = "saturday";
                    break;
            }
            day = "monday"; // 실험용
            return day;
        }

        public String getTimeClass() // 몇 교시인지 구함
        {
            String _class = null;
            long nowTime = System.currentTimeMillis();
            Date date = new Date(nowTime);
            SimpleDateFormat sdfNow = new SimpleDateFormat("HH");
            String formatDate = sdfNow.format(date);

            int hours = Integer.parseInt(formatDate);
            hours -= 8;
            if (hours > 0 && hours < 10) {
                _class = "class" + hours;
            }
            return _class;
        }

        public boolean isShutTime() {
            String _class = getTimeClass();
            String _day = getDayOfWeek();
            if(_day != null) // null이면 토요일이나 일요일이라는 거임
            {
                String query = "select * from timetable where class='" + _class + "' and day='"+_day+"'";
                Cursor c = db.rawQuery(query, null);
                if(c.getCount() > 0) // 만약 수업이 있으면
                {
                    int current_mode = mAudioManager.getRingerMode();
                    if(current_mode == AudioManager.RINGER_MODE_VIBRATE || current_mode == AudioManager.RINGER_MODE_NORMAL)
                    {
                        prev_mode = current_mode;
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT); // 사일런스 모드로 전환
                        mode_change_flag = true;
                        Toast.makeText(MyService.this, "수업중이네요. 사일런스 모드로 전환합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                else // 수업이 없으면
                {
                    if(mode_change_flag) // 최초 모드 전환 시만 작동
                    {
                        mAudioManager.setRingerMode(prev_mode);
                        mode_change_flag = false;
                        Toast.makeText(MyService.this, "수업이 끝났네요. 이전 모드로 전환합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                //Toast.makeText(MyService.this, _class + " " + _day + " " + c.getCount(), Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(MyService.this, _class + " " + _day, Toast.LENGTH_SHORT).show();
            return true;
        }
    };
}




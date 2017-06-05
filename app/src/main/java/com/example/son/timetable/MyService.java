package com.example.son.timetable;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class MyService extends Service {
    NotificationManager Notifi_M;
    ServiceThread thread;
    rightPlace right;
    Notification Notifi;
    SQLiteHelper dbHelper;
    SQLiteDatabase db;
    AudioManager mAudioManager;
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
        try{
            db = dbHelper.getReadableDatabase();
        }
        catch(SQLiteException e)
        {
            Log.d("service", "fail read database");
        }

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        prev_mode = mAudioManager.getMode();
        myServiceHandler handler = new myServiceHandler();

        thread = new ServiceThread(handler);
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

            Intent intent1 = new Intent(MyService.this, rightPlace.class);
            startActivity(intent1);

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
            //day = "monday"; // 실험용
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




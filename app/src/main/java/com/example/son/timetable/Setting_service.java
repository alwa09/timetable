package com.example.son.timetable;


import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Setting_service extends AppCompatActivity {
    LocationListener locationListener;
    LocationManager locationManager;
    TextView logView;
    TextView tv;
    ToggleButton tb;
    Button btnStart, btnEnd;
    private AudioManager mAudioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GpsPermissionCheckForMashMallo();
        // Location 제공자에서 정보를 얻어오기(GPS)
        // 1. Location을 사용하기 위한 권한을 얻어와야한다 AndroidManifest.xml
        //     ACCESS_FINE_LOCATION : NETWORK_PROVIDER, GPS_PROVIDER
        //     ACCESS_COARSE_LOCATION : NETWORK_PROVIDER
        // 2. LocationManager 를 통해서 원하는 제공자의 리스너 등록
        // 3. GPS 는 에뮬레이터에서는 기본적으로 동작하지 않는다
        // 4. 실내에서는 GPS_PROVIDER 를 요청해도 응답이 없다.  특별한 처리를 안하면 아무리 시간이 지나도
        //    응답이 없다.
        //    해결방법은
        //     ① 타이머를 설정하여 GPS_PROVIDER 에서 일정시간 응답이 없는 경우 NETWORK_PROVIDER로 전환
        //     ② 혹은, 둘다 한꺼번헤 호출하여 들어오는 값을 사용하는 방식.


        tv = (TextView) findViewById(R.id.textView2);
        tv.setText("위치정보 미수신중");
        tb = (ToggleButton)findViewById(R.id.toggle1);
        btnStart = (Button)findViewById(R.id.btn_start);
        btnEnd = (Button)findViewById(R.id.btn_end);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Service 시작", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Setting_service.this, MyService.class);
                startService(intent);
                btnStart.setEnabled(false);
            }
        });
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "service 끝", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Setting_service.this, MyService.class);
                stopService(intent);
                btnStart.setEnabled(true);
            }
        });

        // LocationManager 객체를 얻어온다
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(tb.isChecked()){
                        tv.setText("수신중..");
                        // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                mLocationListener);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                mLocationListener);
                    }else{
                        tv.setText("위치정보 미수신중");
                        lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                    }
                }catch(SecurityException ex){
                }
            }
        });
    } // end of onCreate

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

            final Geocoder geocoder = new Geocoder(Setting_service.this);          // 현재는 주소를 받아오는 형식
                                                                                // google place api를 활용하면 장소를 가져올수 있음
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
                    tv.setText("해당되는 주소 정보는 없습니다");
                } else {
                    tv.setText(list.get(0).toString());
                }
            }

            TextView addressView = (TextView)findViewById(R.id.address);
            addressView.setText(list.get(0).toString());

            tv.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n고도 : " + altitude + "\n정확도 : "  + accuracy);

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






    public void GpsPermissionCheckForMashMallo() {

        //마시멜로우 버전 이하면 if문에 걸리지 않습니다.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("GPS 사용 허가 요청");
            alertDialog.setMessage("앰버요청 발견을 알리기위해서는 사용자의 GPS 허가가 필요합니다.\n('허가'를 누르면 GPS 허가 요청창이 뜹니다.)");
            // OK 를 누르게 되면 설정창으로 이동합니다.
            alertDialog.setPositiveButton("허가",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Setting_service.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
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

    public void onSilent(View v){
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted())
        {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        if(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
        {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }

        Intent intent = new Intent(Setting_service.this, rightPlace.class);
        startActivity(intent);

    }


    public void onVib(View v){
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
        Toast.makeText(this, "Vib", Toast.LENGTH_SHORT).show();
    }



}

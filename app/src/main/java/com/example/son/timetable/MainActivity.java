package com.example.son.timetable;


import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private SQLiteHelper dbHelper;
    String dbName = "timetable_db";
    int dbVersion = 1;
    private SQLiteDatabase db;
    String tag = "SQLite";

    View.OnClickListener tableClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            String button_id = getResources().getResourceName(v.getId());
            String[] values = button_id.split("_");
            String day = values[1]; // 요일
            values = values[0].split("id/");
            String _class = values[1]; // 교시
            Log.d(tag, "입력될 값: " + _class + " " + day);
            showTimeTableDialog(_class, day);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        Toolbar toolbar= (Toolbar)findViewById(R.id.action_bar);
        if (toolbar!= null){
            toolbar.setTitleTextColor(Color.BLACK);
        }
        getSupportActionBar().setTitle("시간표");

        startActivity(new Intent(this, SplashActivity.class));
        GpsPermissionCheckForMashMallo();

        dbHelper = new SQLiteHelper(this, dbName, null , dbVersion);
        try {
            db = dbHelper.getWritableDatabase();
        }
        catch(SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, "데이터베이스를 얻어올 수 없음");
            finish();
        }
        updateTimeTable();
        registButtonFunc();
    }

    void showTimeTableDialog(final String _class, final String day) // 입력창 띄우기
    {
        final TimeInputDialog td = new TimeInputDialog(this);

        td.setPositiveButton(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                insert(_class, day, td.getLecture(), td.getTableColor());
                updateTimeTable();
                td.dismiss();
                showDatabase();
            }
        });
        td.setNegativeButton(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                td.dismiss();
            }
        });
        td.setDeleteButton(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                delete(_class, day);
                td.dismiss();
                showDatabase();
            }
        });
        td.show();
    }

    void delete(String _class, String day)
    {
        String query = "select id from timetable where class='" + _class + "' and day='" + day + "';";
        Cursor c = db.rawQuery(query, null);
        int id = -99;
        while(c.moveToNext())
        {
            id = c.getInt(0);
        }
        db.execSQL("delete from timetable where refer="+id+";");
        updateTimeTable();
    }

    void insert(String _class, String day, String lecture, int color) // DB에 삽입
    {
        if(checkDup(_class, day)==0)
        {
            db.execSQL("insert into timetable VALUES(null, '" + _class + "','" + day + "','" + lecture + "'," + color + ", null);");
            String query = "select id from timetable where class='" + _class + "' and day='" + day + "';";
            Cursor c = db.rawQuery(query, null);
            while(c.moveToNext())
            {
                int id = c.getInt(0);
                query = "update timetable set refer=" + id + " where class='" + _class + "' and day='" + day + "';";
                db.execSQL(query);
            }
        }
        else
        {
            db.execSQL("update timetable set lecture='"+lecture+"', color=" + color + " where class='"+_class+"' and day='"+day+"'");
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

    void showDatabase()
    {
        Cursor c = db.rawQuery("select * from timetable;", null);
        while(c.moveToNext()) {
            int id = c.getInt(0);
            String _class = c.getString(1);
            String day = c.getString(2);
            String lecture = c.getString(3);
            int color = c.getInt(4);
            int refer = c.getInt(5);
            Log.d("YOYOSQL", id + " " + _class + " " + day + " " + lecture + " " + color + " " + refer);
        }
    }
    void updateTimeTable()
    {
        String button_id;
        Button btn;
        for(int i=1; i<10; i++)
        {
            button_id = "class" + i +"_"+ "monday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setText("");
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.empty_button);
            button_id = "class" + i +"_"+ "tuesday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setText("");
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.empty_button);
            button_id = "class" + i +"_"+ "wednesday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setText("");
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.empty_button);
            button_id = "class" + i +"_"+ "thursday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setText("");
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.empty_button);
            button_id = "class" + i +"_"+ "friday";
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setText("");
            btn.setEnabled(true);
            btn.setBackgroundResource(R.drawable.empty_button);
        }

        Cursor c = db.rawQuery("select * from timetable;", null);
        while(c.moveToNext())
        {
            int id = c.getInt(0);
            String _class = c.getString(1);
            String day = c.getString(2);
            String lecture = c.getString(3);
            int color = c.getInt(4);
            int refer = c.getInt(5);
            button_id = _class +"_"+day;
            btn = (Button)findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
            btn.setText(lecture);
            btn.setBackgroundColor(color);
            btn.setEnabled(true);
        }
        mergeTable("monday");
        mergeTable("tuesday");
        mergeTable("wednesday");
        mergeTable("thursday");
        mergeTable("friday");
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

    private void mergeTable(String day)
    {
        String query = "select * from timetable where day='" + day + "' order by class ASC;";
        Cursor c = db.rawQuery(query, null);
        int prevRefer = -99;
        int prevColor = 0;
        int prevNumber = -99;
        String prevLecture = null;
        while(c.moveToNext())
        {
            int id = c.getInt(0);
            String _class = c.getString(1);
            String _day = c.getString(2);
            String lecture = c.getString(3);
            int color = c.getInt(4);
            int refer = c.getInt(5);

            String classNumbers[] = _class.split("class");
            int classNumber = new Integer(classNumbers[1]);

            if((prevColor == color && prevNumber == (classNumber - 1)) || (prevLecture != null && lecture.equals(prevLecture) && prevNumber == (classNumber - 1)))
            {
                //Log.d("YOYOSQL", "MERGE");
                String button_id = _class + "_" + _day;
                Button btn = (Button) findViewById(getResources().getIdentifier(button_id, "id", getPackageName()));
                btn.setText("");
                btn.setEnabled(false);
                btn.setBackgroundColor(prevColor);
                query = "update timetable set lecture='" + prevLecture + "', refer=" + prevRefer + ", color=" + prevColor + " where class='" + _class + "' and day='" + _day + "'";
                lecture = prevLecture;
                refer = prevRefer;
                color = prevColor;
                //Log.d("YOYOSQL", query);
                db.execSQL(query);
            }
            else
            {
                query = "update timetable set refer=" + id + " where class='" + _class + "' and day='" + _day + "'";
                refer = id;
                db.execSQL(query);
            }
            prevRefer = refer;
            prevNumber = classNumber;
            prevColor = color;
            prevLecture = lecture;
            //Log.d("YOYOSQL", _class + " " + _day + " " + lecture);
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
            alertDialog.setMessage("현재 위치를 알기 위해서는 사용자의 GPS 허가가 필요합니다.\n('허가'를 누르면 GPS 허가 요청창이 뜹니다.)");
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
        requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp();
    }

    private void requestDoNotDisturbPermissionOrSetDoNotDisturbApi23AndUp() {
        //TO SUPPRESS API ERROR MESSAGES IN THIS FUNCTION, since Ive no time to figrure our Android SDK suppress stuff
        if( Build.VERSION.SDK_INT < 23 ) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if ( !notificationManager.isNotificationPolicyAccessGranted()) {
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
            alertDialog.setTitle("알림 일시중지 서비스 허가 요청");
            alertDialog.setMessage("음소거 모드를 사용하려면 사용자의 알림 일시정지 서비스 허가 요청이 필요합니다..\n('허가'를 누르면 요청창이 뜹니다.)");
            // OK 를 누르게 되면 설정창으로 이동합니다.
            alertDialog.setPositiveButton("허가",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
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
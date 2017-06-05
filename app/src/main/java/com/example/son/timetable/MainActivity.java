package com.example.son.timetable;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private SQLiteHelper dbHelper;
    String dbName = "timetable_db";
    int dbVersion = 1;
    private SQLiteDatabase db;
    String tag = "SQLite";

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
}

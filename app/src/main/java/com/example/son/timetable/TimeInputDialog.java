package com.example.son.timetable;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TimeInputDialog extends Dialog {
    private EditText mEdit;
    private int mColor;
    private Button mPositiveBtn;
    private Button mNegativeBtn;
    private Button mDeleteBtn;
    private Button[] mColorBtn;
    private TextView mSelectedColor;
    private int[] mColorTable;
    private View.OnClickListener mPositiveListener;
    private View.OnClickListener mNegativeListener;
    private View.OnClickListener mDeleteListener;
    private View.OnClickListener mColorListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Button btn = (Button)findViewById(v.getId());
            ColorDrawable colorDrawable = (ColorDrawable)btn.getBackground();
            mColor = colorDrawable.getColor();
            mSelectedColor.setBackgroundColor(mColor);
        }
    };

    public TimeInputDialog(Context context)
    {
        super(context);
    }
    public TimeInputDialog(@NonNull Context context, View.OnClickListener positive, View.OnClickListener negative) {
        super(context);
        mPositiveListener = positive;
        mNegativeListener = negative;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_timeinput);
        initColorBtn();

        mEdit = (EditText)findViewById(R.id.timeinput_edit);
        mPositiveBtn = (Button)findViewById(R.id.timeinput_positive);
        mNegativeBtn = (Button)findViewById(R.id.timeinput_negative);
        mDeleteBtn = (Button)findViewById(R.id.timeinput_delete);
        mSelectedColor = (TextView)findViewById(R.id.timeinput_selected_color);
        mPositiveBtn.setOnClickListener(mPositiveListener);
        mNegativeBtn.setOnClickListener(mNegativeListener);
        mDeleteBtn.setOnClickListener(mDeleteListener);

        mColor = mColorTable[0];
        mSelectedColor.setBackgroundColor(mColor);
    }

    private void initColorBtn()
    {
        mColorBtn = new Button[10];
        mColorBtn[0] = (Button)findViewById(R.id.timeinput_color0);
        mColorBtn[1] = (Button)findViewById(R.id.timeinput_color1);
        mColorBtn[2] = (Button)findViewById(R.id.timeinput_color2);
        mColorBtn[3] = (Button)findViewById(R.id.timeinput_color3);
        mColorBtn[4] = (Button)findViewById(R.id.timeinput_color4);
        mColorBtn[5] = (Button)findViewById(R.id.timeinput_color5);
        mColorBtn[6] = (Button)findViewById(R.id.timeinput_color6);
        mColorBtn[7] = (Button)findViewById(R.id.timeinput_color7);
        mColorBtn[8] = (Button)findViewById(R.id.timeinput_color8);
        mColorBtn[9] = (Button)findViewById(R.id.timeinput_color9);

        initColorTable();

        for(int i=0; i<10; i++)
        {
            mColorBtn[i].setBackgroundColor(mColorTable[i]);
            mColorBtn[i].setOnClickListener(mColorListener);
        }
    }

    private void initColorTable()
    {
        mColorTable = new int[10];
        mColorTable[0] = Color.rgb(220,20,60); // crimson
        mColorTable[1] = Color.rgb(255,165,0); // orange
        mColorTable[2] = Color.rgb(255,215,0); // gold
        mColorTable[3] = Color.rgb(124,252,0); // lawn green
        mColorTable[4] = Color.rgb(50,205,50); // lime green
        mColorTable[5] = Color.rgb(175,238,238); // pale turquoise
        mColorTable[6] = Color.rgb(30,144,255); // dodger blue
        mColorTable[7] = Color.rgb(123,104,238); // medium slate blue
        mColorTable[8] = Color.rgb(221,160,221); // plum
        mColorTable[9] = Color.rgb(255,192,203); // pink
    }

    public void setPositiveButton(View.OnClickListener listener)
    {
        mPositiveListener = listener;
        //mPositiveBtn.setOnClickListener(mPositiveListener);

    }

    public void setNegativeButton(View.OnClickListener listener)
    {
        mNegativeListener = listener;
        //mNegativeBtn.setOnClickListener(mNegativeListener);
    }

    public void setDeleteButton(View.OnClickListener listener)
    {
        mDeleteListener = listener;
        //mDeleteBtn.setOnClickListener(mDeleteListener);
    }

    public int getTableColor()
    {
        return mColor;
    }

    public String getLecture()
    {
        return mEdit.getText().toString();
    }
}

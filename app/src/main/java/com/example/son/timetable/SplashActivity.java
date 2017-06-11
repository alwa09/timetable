package com.example.son.timetable;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ImageView;

/**
 * Created by Jeong on 2017-06-04.
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        ImageView imageView = (ImageView)findViewById(R.id.splash_image);
        imageView.setBackgroundResource(R.drawable.splash_animation);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        AnimationDrawable animationDrawable = (AnimationDrawable)imageView.getBackground();
        animationDrawable.start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2500);
    }
}

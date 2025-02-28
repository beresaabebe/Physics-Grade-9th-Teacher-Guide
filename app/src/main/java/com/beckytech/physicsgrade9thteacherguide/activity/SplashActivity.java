package com.beckytech.physicsgrade9thteacherguide.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.beckytech.physicsgrade9thteacherguide.MainActivity;
import com.beckytech.physicsgrade9thteacherguide.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}
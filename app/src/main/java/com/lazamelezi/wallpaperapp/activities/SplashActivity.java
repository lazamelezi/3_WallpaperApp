package com.lazamelezi.wallpaperapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.lazamelezi.wallpaperapp.MainApplication;
import com.lazamelezi.wallpaperapp.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        MainApplication.getDataService(getApplication()).setupCategories(this::startMainActivity);

    }



    public void startMainActivity() {

            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            finish();
    }


}
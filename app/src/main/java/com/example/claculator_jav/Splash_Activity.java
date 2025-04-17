package com.example.claculator_jav;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper; // Import Looper
import android.os.Build;  // Import Build

import androidx.appcompat.app.AppCompatActivity;

public class Splash_Activity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //  No need to set windowSplashScreenBackground in Java
        //  It's handled by the theme in styles.xml

        new Handler(Looper.getMainLooper()).postDelayed(() -> {  // Use Looper explicitly
            Intent intent = new Intent(Splash_Activity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the splash activity
        }, SPLASH_DURATION);
    }
}

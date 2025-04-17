package com.example.claculator_jav;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash_Activity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash_Activity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the splash activity so the user can't go back to it
        }, SPLASH_DURATION);
    }
}
package com.example.canpay_operator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "CanPayPrefs";
    private static final String KEY_SESSION_TIMESTAMP = "session_timestamp";
    private static final long SESSION_DURATION = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isSessionExpired()) {
                    // Session expired, go to LoginActivity
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Session valid, go to PinLoginActivity
                    Intent intent = new Intent(SplashActivity.this, PinLoginActivity.class);
                    startActivity(intent);
                }
                finish(); // Close splash activity
            }
        }, 2000); // 2 seconds delay
    }

    private boolean isSessionExpired() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastLogin = prefs.getLong(KEY_SESSION_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastLogin) > SESSION_DURATION;
    }
}

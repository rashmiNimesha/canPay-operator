package com.example.canpay_operator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "CanPayPrefs";
    private static final String KEY_SESSION_TIMESTAMP = "session_timestamp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button continueButton = findViewById(R.id.btn_continue_login);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save current time as session timestamp
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis()).apply();

                // Navigate to PhoneNoActivity
                Intent intent = new Intent(LoginActivity.this, PhoneNoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

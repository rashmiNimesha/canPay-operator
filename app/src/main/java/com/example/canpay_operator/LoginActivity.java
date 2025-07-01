package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button continueButton = findViewById(R.id.btn_continue_login);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Replace MainActivity.class with your actual next screen
                Intent intent = new Intent(LoginActivity.this, PhoneNoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AccountSuccessActivity extends AppCompatActivity {

    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_success); // Use your actual XML layout filename here

        btnContinue = findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(v -> {
            // Navigate to HomeActivity
            Intent intent = new Intent(AccountSuccessActivity.this, HomeActivity.class);
            // Optional: clear back stack if you want to prevent going back to success screen
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}

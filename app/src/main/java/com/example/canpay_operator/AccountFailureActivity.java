package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AccountFailureActivity extends AppCompatActivity {

    private Button btnGoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_failure); // Use your actual XML layout filename here

        btnGoBack = findViewById(R.id.btn_continue);

        btnGoBack.setOnClickListener(v -> {
            // Navigate to NameActivity
            Intent intent = new Intent(AccountFailureActivity.this, NameActivity.class);
            // Optional: clear back stack if you want to prevent going back to failure screen
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}

package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class NameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        EditText etName = findViewById(R.id.et_name);
        Button btnNext = findViewById(R.id.btn_next);
        ImageButton btnBack = findViewById(R.id.btn_back);
        String email = getIntent().getStringExtra("email");

        // Back button: navigate to OtpActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(NameActivity.this, OtpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // NEXT button: validate and go to nic
        btnNext.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Please enter your name");
                return;
            }

            // Pass name and email to NIC entry screen
            Intent intent = new Intent(NameActivity.this, NICEntryActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        });

    }
}

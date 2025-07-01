package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class NICEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nicentry);

        EditText etNic = findViewById(R.id.et_nic);
        Button btnNext = findViewById(R.id.btn_next);
        ImageButton btnBack = findViewById(R.id.btn_back);

        // Retrieve name and email passed from previous activity
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");

        // Back button: navigate explicitly to NameActivity with name and email
        btnBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(NICEntryActivity.this, NameActivity.class);
            if (name != null) {
                backIntent.putExtra("name", name);
            }
            if (email != null) {
                backIntent.putExtra("email", email);
            }
            // Clear current activity from stack to avoid multiple instances
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(backIntent);
            finish();
        });

        // Next button: validate NIC and proceed to TakePhotoActivity
        btnNext.setOnClickListener(v -> {
            String nic = etNic.getText().toString().trim();
            if (TextUtils.isEmpty(nic)) {
                etNic.setError("Please enter your NIC number");
                etNic.requestFocus();
                return;
            }

            Intent intent = new Intent(NICEntryActivity.this, TakePhotoActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("email", email);
            intent.putExtra("nic", nic);
            startActivity(intent);
            finish();
        });
    }
}

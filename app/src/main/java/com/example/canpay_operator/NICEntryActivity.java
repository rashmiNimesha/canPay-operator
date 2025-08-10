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

        btnBack.setOnClickListener(v -> finish());
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");

        btnNext.setOnClickListener(v -> {
            String nic = etNic.getText().toString().trim();

            if (TextUtils.isEmpty(nic)) {
                etNic.setError("Please enter your NIC number");
                etNic.requestFocus();
                return;
            }

            // Validate NIC format
            boolean isValid = false;

            // Pattern 1: 12 digits exactly
            if (nic.matches("\\d{12}")) {
                isValid = true;
            }
            // Pattern 2: 9 digits followed by V or v
            else if (nic.matches("\\d{9}[Vv]")) {
                isValid = true;
            }

            if (!isValid) {
                etNic.setError("NIC must be 12 digits OR 9 digits followed by 'V' or 'v'");
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

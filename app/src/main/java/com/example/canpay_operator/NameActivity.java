package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

        // NEXT button: validate and go to NICEntryActivity
        btnNext.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Please enter your name");
                return;
            }

            if (name.length() < 3) {
                etName.setError("Name must be at least 3 characters");
                return;
            }

            if (name.length() > 50) {
                etName.setError("Name cannot exceed 50 characters");
                return;
            }

            // Check for digits
            if (name.matches(".*\\d.*")) {
                etName.setError("Name cannot contain numbers");
                return;
            }

            // Check spaces count (max one space)
            int spaceCount = name.length() - name.replace(" ", "").length();
            if (spaceCount > 1) {
                etName.setError("Name can contain at most one space");
                return;
            }

            // Optional: validate allowed characters (letters, space, apostrophes, hyphens)
            if (!name.matches("^[a-zA-Z'\\- ]+$")) {
                etName.setError("Name contains invalid characters");
                return;
            }

            // Passed all checks, proceed
            Intent intent = new Intent(NameActivity.this, NICEntryActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        });
    }
}

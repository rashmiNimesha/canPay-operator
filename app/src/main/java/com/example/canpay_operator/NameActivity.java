package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NameActivity extends AppCompatActivity {

    // Regex pattern to allow letters, spaces, apostrophes, hyphens; length 2-50 chars
    private static final String NAME_PATTERN = "^[a-zA-Z\\s'-]{2,50}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        EditText etName = findViewById(R.id.et_name);
        Button btnNext = findViewById(R.id.btn_next);
        ImageButton btnBack = findViewById(R.id.btn_back);

        // Back button: navigate to OtpActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(NameActivity.this, OtpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // NEXT button: validate name and go to NIC entry screen
        btnNext.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            // Check if name is empty
            if (name.isEmpty()) {
                etName.setError("Please enter your name");
                etName.requestFocus();
                return;
            }

            // Check if name matches the pattern
            if (!name.matches(NAME_PATTERN)) {
                etName.setError("Please enter a valid name (letters, spaces, apostrophes, hyphens only)");
                etName.requestFocus();
                return;
            }

            // Proceed to NICEntryActivity, passing the name only
            Intent intent = new Intent(NameActivity.this, NICEntryActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
            finish();
        });
    }
}

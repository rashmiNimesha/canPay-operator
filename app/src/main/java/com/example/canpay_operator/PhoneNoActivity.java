package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class PhoneNoActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etPhone;
    private Button btnLogin;
    private TextView tvTermsLink;

    // Allows optional +, 10-15 digits (international and local)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_no); // Use your actual layout file name here

        btnBack = findViewById(R.id.btn_back);
        etPhone = findViewById(R.id.et_phone);
        btnLogin = findViewById(R.id.btn_login);
        tvTermsLink = findViewById(R.id.tv_terms_link);

        // Back button: navigate explicitly to LoginActivity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(PhoneNoActivity.this, LoginActivity.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(backIntent);
                finish();
            }
        });

        // Login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = etPhone.getText().toString().trim();
                if (isValidPhoneNumber(phoneNumber)) {
                    // Navigate to OTP Activity, passing the phone number
                    Intent intent = new Intent(PhoneNoActivity.this, OtpActivity.class);
                    intent.putExtra("phone_number", phoneNumber);
                    startActivity(intent);
                } else {
                    if (TextUtils.isEmpty(phoneNumber)) {
                        etPhone.setError("Phone number is required");
                        Toast.makeText(PhoneNoActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        etPhone.setError("Please enter a valid phone number");
                        Toast.makeText(PhoneNoActivity.this, "Enter a valid phone number (10–15 digits)", Toast.LENGTH_SHORT).show();
                    }
                    etPhone.requestFocus();
                }
            }
        });

        // Terms & Conditions link is clickable but does nothing (UI only)
        tvTermsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // No action needed
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) return false;
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
}

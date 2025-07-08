package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OtpActivity extends AppCompatActivity {

    private EditText[] otpBoxes = new EditText[6];
    private TextView tvResend;
    private Button btnNext;
    private ImageButton btnBack;
    private CountDownTimer timer;
    private int resendSeconds = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otpBoxes[0] = findViewById(R.id.otp1);
        otpBoxes[1] = findViewById(R.id.otp2);
        otpBoxes[2] = findViewById(R.id.otp3);
        otpBoxes[3] = findViewById(R.id.otp4);
        otpBoxes[4] = findViewById(R.id.otp5);
        otpBoxes[5] = findViewById(R.id.otp6);
        tvResend = findViewById(R.id.tv_resend);
        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);

        // Auto move focus between OTP boxes
        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpBoxes[index - 1].requestFocus();
                    }
                }
            });
        }

        btnNext.setOnClickListener(v -> {
            if (validateOtpInputs()) {
                StringBuilder otp = new StringBuilder();
                for (EditText box : otpBoxes) {
                    otp.append(box.getText().toString().trim());
                }
                String phoneNumber = getIntent().getStringExtra("phone_number"); // Ensure phone number is passed
                onOtpValidated(phoneNumber, otp.toString());
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(OtpActivity.this, PhoneNoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        startResendTimer();

        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                // Your partner handles backend resend logic
                Toast.makeText(this, "Resend OTP clicked", Toast.LENGTH_SHORT).show();
                startResendTimer();
            }
        });
    }

    private boolean validateOtpInputs() {
        for (int i = 0; i < otpBoxes.length; i++) {
            String digit = otpBoxes[i].getText().toString().trim();
            if (digit.isEmpty()) {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
                otpBoxes[i].requestFocus();
                return false;
            }
            if (!digit.matches("\\d")) {
                Toast.makeText(this, "OTP digits must be numeric", Toast.LENGTH_SHORT).show();
                otpBoxes[i].requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * Called after front-end validation.
     * Replace simulated checks with real backend calls.
     */
    private void onOtpValidated(String phoneNumber, String otp) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Missing phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Replace with real backend OTP validation
        boolean isOtpValid = simulateOtpValidation(otp);
        if (!isOtpValid) {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Replace with real backend phone number existence check
        boolean phoneExistsInDb = simulatePhoneNumberCheck(phoneNumber);

        Intent intent;
        if (phoneExistsInDb) {
            // Phone number exists -> Navigate to SetPinActivity
            intent = new Intent(OtpActivity.this, SetPinActivity.class);
        } else {
            // Phone number does NOT exist -> Navigate to NameActivity
            intent = new Intent(OtpActivity.this, NameActivity.class);
        }

        intent.putExtra("phone_number", phoneNumber);
        startActivity(intent);
        finish();
    }

    // Simulated OTP validation - replace with real backend validation
    private boolean simulateOtpValidation(String otp) {
        // For example purposes only, accept "123456" as valid OTP
        return otp.equals("123456");
    }

    // Simulated phone number existence check - replace with real backend call
    private boolean simulatePhoneNumberCheck(String phoneNumber) {
        // For example, phone numbers ending with even digit exist
        char lastDigit = phoneNumber.charAt(phoneNumber.length() - 1);
        return (lastDigit - '0') % 2 == 0;
    }

    private void startResendTimer() {
        tvResend.setEnabled(false);
        timer = new CountDownTimer(resendSeconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvResend.setText("RESEND CODE IN " + (millisUntilFinished / 1000) + " SECONDS");
            }
            public void onFinish() {
                tvResend.setText("RESEND CODE");
                tvResend.setEnabled(true);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}

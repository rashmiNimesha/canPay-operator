package com.example.canpay_operator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfirmNewPinActivity extends AppCompatActivity {

    private EditText[] pinFields = new EditText[4];
    private ImageButton btnBack;
    private Button btnEnter;
    private String originalPin;
    private int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_new_pin);

        originalPin = getIntent().getStringExtra("new_pin");
        if (originalPin == null || originalPin.isEmpty()) {
            Toast.makeText(this, "Error: No PIN to confirm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupPinListeners();
        setupButtons();
    }

    private void initializeViews() {
        pinFields[0] = findViewById(R.id.pin1);
        pinFields[1] = findViewById(R.id.pin2);
        pinFields[2] = findViewById(R.id.pin3);
        pinFields[3] = findViewById(R.id.pin4);
        btnBack = findViewById(R.id.btn_back);
        btnEnter = findViewById(R.id.btn_enter);
    }

    private void setupPinListeners() {
        for (int i = 0; i < pinFields.length; i++) {
            final int currentIndex = i;

            pinFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    pinFields[currentIndex].setError(null);
                    if (s.length() == 1 && currentIndex < 3) {
                        pinFields[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && currentIndex > 0) {
                        pinFields[currentIndex - 1].requestFocus();
                    }
                    if (isAllFieldsFilled()) validateAndSubmitPin();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String input = s.toString();
                    if (!input.isEmpty() && !input.matches("\\d")) {
                        pinFields[currentIndex].setText("");
                        showError("Only numbers are allowed");
                    }
                }
            });

            pinFields[i].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (pinFields[currentIndex].getText().length() == 0 && currentIndex > 0) {
                            pinFields[currentIndex - 1].requestFocus();
                            pinFields[currentIndex - 1].setText("");
                        }
                    }
                    return false;
                }
            });
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnEnter.setOnClickListener(v -> validateAndSubmitPin());
    }

    private boolean isAllFieldsFilled() {
        for (EditText field : pinFields) {
            if (field.getText().toString().trim().isEmpty()) return false;
        }
        return true;
    }

    private void validateAndSubmitPin() {
        if (!isAllFieldsFilled()) {
            showError("Please enter complete 4-digit PIN");
            return;
        }

        String confirmPin = getCurrentPin();

        if (attemptCount >= MAX_ATTEMPTS) {
            showError("Too many failed attempts. Please try again later.");
            navigateToFailedScreen("Maximum attempts exceeded");
            return;
        }

        if (!confirmPin.equals(originalPin)) {
            attemptCount++;
            String errorMsg = "PINs don't match. " + (MAX_ATTEMPTS - attemptCount) + " attempts remaining.";
            showError(errorMsg);
            clearAllFields();
            if (attemptCount >= MAX_ATTEMPTS) navigateToFailedScreen("Too many failed attempts");
            return;
        }

        savePinSecurely(confirmPin);
        navigateToSuccessScreen();
    }

    private String getCurrentPin() {
        StringBuilder pin = new StringBuilder();
        for (EditText field : pinFields) pin.append(field.getText().toString().trim());
        return pin.toString();
    }

    private void savePinSecurely(String pin) {
        try {
            SharedPreferences prefs = getSharedPreferences("CanPayPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_pin", pin);
            editor.apply();
            Toast.makeText(this, "PIN saved securely", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            showError("Failed to save PIN. Please try again.");
        }
    }

    private void navigateToSuccessScreen() {
        startActivity(new Intent(this, PinUpdateSuccessActivity.class));
        finish();
    }

    private void navigateToFailedScreen(String reason) {
        Intent intent = new Intent(this, PinUpdateFailedActivity.class);
        intent.putExtra("failure_reason", reason);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        for (EditText field : pinFields) field.setError("");
    }

    private void clearAllFields() {
        for (EditText field : pinFields) {
            field.setText("");
            field.setError(null);
        }
        pinFields[0].requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pinFields[0].requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearAllFields();
    }
}

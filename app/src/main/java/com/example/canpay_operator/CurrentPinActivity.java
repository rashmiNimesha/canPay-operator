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

public class CurrentPinActivity extends AppCompatActivity {

    private EditText[] pinFields = new EditText[4];
    private ImageButton btnBack;
    private Button btnEnter;
    private int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_pin);
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
                    if (s.length() == 1 && currentIndex < 3) {
                        pinFields[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && currentIndex > 0) {
                        pinFields[currentIndex - 1].requestFocus();
                    }
                    if (isAllFieldsFilled()) validatePin();
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
        btnEnter.setOnClickListener(v -> validatePin());
    }

    private boolean isAllFieldsFilled() {
        for (EditText field : pinFields) {
            if (field.getText().toString().trim().isEmpty()) return false;
        }
        return true;
    }

    private void validatePin() {
        if (!isAllFieldsFilled()) {
            showError("Please enter complete 4-digit PIN");
            return;
        }

        String enteredPin = getCurrentPin();
        String savedPin = getSavedPin();

        if (savedPin == null || savedPin.isEmpty()) {
            startActivity(new Intent(this, CreateNewPinActivity.class));
            finish();
            return;
        }

        if (attemptCount >= MAX_ATTEMPTS) {
            showError("Too many failed attempts. Try again later.");
            return;
        }

        if (enteredPin.equals(savedPin)) {
            Intent intent = new Intent(this, CreateNewPinActivity.class);
            intent.putExtra("is_change_pin", true);
            startActivity(intent);
            finish();
        } else {
            attemptCount++;
            String errorMsg = "Incorrect PIN. " + (MAX_ATTEMPTS - attemptCount) + " attempts left.";
            showError(errorMsg);
            clearAllFields();
        }
    }

    private String getCurrentPin() {
        StringBuilder pin = new StringBuilder();
        for (EditText field : pinFields) pin.append(field.getText().toString().trim());
        return pin.toString();
    }

    private String getSavedPin() {
        SharedPreferences prefs = getSharedPreferences("CanPayPrefs", MODE_PRIVATE);
        return prefs.getString("user_pin", null);
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
}

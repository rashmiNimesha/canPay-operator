package com.example.canpay_operator;

import android.content.Intent;
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

public class CreateNewPinActivity extends AppCompatActivity {

    private EditText[] pinFields = new EditText[4];
    private ImageButton btnBack;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_pin);

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

                    if (isAllFieldsFilled()) validateAndSubmitPin();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Ensure only numeric input
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

        String pin = getCurrentPin();

        if (!isValidPin(pin)) return;

        Intent intent = new Intent(this, ConfirmNewPinActivity.class);
        intent.putExtra("new_pin", pin);
        startActivity(intent);
        finish();
    }

    private String getCurrentPin() {
        StringBuilder pin = new StringBuilder();
        for (EditText field : pinFields) pin.append(field.getText().toString().trim());
        return pin.toString();
    }

    private boolean isValidPin(String pin) {
        if (pin.length() != 4) {
            showError("PIN must be exactly 4 digits");
            clearAllFields();
            return false;
        }
        if (!pin.matches("\\d{4}")) {
            showError("PIN must contain only numbers");
            clearAllFields();
            return false;
        }
        if (pin.matches("(\\d)\\1{3}")) {
            showError("PIN cannot be all same digits");
            clearAllFields();
            return false;
        }
        if (isSequentialPin(pin)) {
            showError("PIN cannot be sequential numbers");
            clearAllFields();
            return false;
        }
        if (isWeakPin(pin)) {
            showError("Please choose a stronger PIN");
            clearAllFields();
            return false;
        }
        return true;
    }

    private boolean isSequentialPin(String pin) {
        boolean ascending = true;
        boolean descending = true;
        for (int i = 1; i < pin.length(); i++) {
            int current = Character.getNumericValue(pin.charAt(i));
            int prev = Character.getNumericValue(pin.charAt(i-1));

            if (current != prev + 1) ascending = false;
            if (current != prev - 1) descending = false;
        }
        return ascending || descending;
    }

    private boolean isWeakPin(String pin) {
        String[] weakPins = {"0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999",
                "1234", "4321", "1122", "2211", "1212", "2121", "0123", "3210", "1010", "2020"};
        for (String weakPin : weakPins) {
            if (pin.equals(weakPin)) return true;
        }
        return false;
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

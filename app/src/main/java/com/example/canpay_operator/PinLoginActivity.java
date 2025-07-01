package com.example.canpay_operator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinLoginActivity extends AppCompatActivity {

    private EditText pin1, pin2, pin3, pin4;
    private Button btnResetPin;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CanpayPrefs";
    private static final String KEY_SET_PIN = "set_pin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login); // Use your actual XML layout filename

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        btnResetPin = findViewById(R.id.btn_enter); // Your button id is btn_enter but text is "RESET PIN"

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupPinEditTexts();

        btnResetPin.setOnClickListener(v -> {
            // Navigate to ResetPinActivity when reset pin button clicked
            Intent intent = new Intent(PinLoginActivity.this, PhoneNoActivity.class);
            startActivity(intent);
        });

        // Listen for PIN input completion and validate automatically
        // Alternatively, you can add a separate "Login" button and validate on click
        LinearLayout pinContainer = findViewById(R.id.pin_container);
        // Optional: add a listener to validate PIN automatically once 4 digits entered
        // Here, we add TextWatchers to do that:
        EditText[] pins = {pin1, pin2, pin3, pin4};
        for (EditText pin : pins) {
            pin.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (allPinsFilled()) {
                        validatePinAndLogin();
                    }
                }
            });
        }
    }

    private void setupPinEditTexts() {
        EditText[] pins = {pin1, pin2, pin3, pin4};

        for (int i = 0; i < pins.length; i++) {
            final int index = i;
            pins[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < pins.length - 1) {
                        pins[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        pins[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private boolean allPinsFilled() {
        return !pin1.getText().toString().trim().isEmpty() &&
                !pin2.getText().toString().trim().isEmpty() &&
                !pin3.getText().toString().trim().isEmpty() &&
                !pin4.getText().toString().trim().isEmpty();
    }

    private void validatePinAndLogin() {
        String enteredPin = pin1.getText().toString().trim() +
                pin2.getText().toString().trim() +
                pin3.getText().toString().trim() +
                pin4.getText().toString().trim();

        if (!enteredPin.matches("\\d{4}")) {
            Toast.makeText(this, "PIN must be 4 numeric digits", Toast.LENGTH_SHORT).show();
            return;
        }

        String savedPin = sharedPreferences.getString(KEY_SET_PIN, null);
        if (savedPin == null) {
            Toast.makeText(this, "No PIN set. Please reset your PIN.", Toast.LENGTH_LONG).show();
            return;
        }

        if (enteredPin.equals(savedPin)) {
            // PIN correct, navigate to HomeActivity
            Intent intent = new Intent(PinLoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Incorrect PIN. Please try again.", Toast.LENGTH_SHORT).show();
            clearPinInputs();
            pin1.requestFocus();
        }
    }

    private void clearPinInputs() {
        pin1.setText("");
        pin2.setText("");
        pin3.setText("");
        pin4.setText("");
    }
}

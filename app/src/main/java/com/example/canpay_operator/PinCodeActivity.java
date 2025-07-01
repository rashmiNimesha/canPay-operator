package com.example.canpay_operator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinCodeActivity extends AppCompatActivity {

    private EditText pin1, pin2, pin3, pin4;
    private Button btnEnter;
    private ImageButton btnBack;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CanpayPrefs";
    private static final String KEY_PIN = "user_pin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code); // Use your actual layout file name here

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);

        btnEnter = findViewById(R.id.btn_enter);
        btnBack = findViewById(R.id.btn_back);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupPinEditTexts();

        btnBack.setOnClickListener(v -> {
            // Navigate back to TakePhotoActivity
            Intent intent = new Intent(PinCodeActivity.this, TakePhotoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnEnter.setOnClickListener(v -> {
            if (validatePinInputs()) {
                String pin = getPinFromInputs();
                savePinLocally(pin);

                // Navigate to ConfirmPinCodeActivity
                Intent intent = new Intent(PinCodeActivity.this, ConfirmPinCodeActivity.class);
                intent.putExtra("pin", pin); // pass the pin to confirm screen if needed
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Sets up TextWatchers on PIN EditTexts to auto-move focus and clear error states.
     */
    private void setupPinEditTexts() {
        EditText[] pins = {pin1, pin2, pin3, pin4};

        for (int i = 0; i < pins.length; i++) {
            final int index = i;
            pins[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

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

    /**
     * Validates that all PIN fields are filled with exactly one digit.
     * Shows Toast if validation fails.
     */
    private boolean validatePinInputs() {
        EditText[] pins = {pin1, pin2, pin3, pin4};
        for (int i = 0; i < pins.length; i++) {
            String digit = pins[i].getText().toString().trim();
            if (digit.isEmpty()) {
                Toast.makeText(this, "Please enter all 4 digits of the PIN", Toast.LENGTH_SHORT).show();
                pins[i].requestFocus();
                return false;
            }
            if (!digit.matches("\\d")) {
                Toast.makeText(this, "PIN digits must be numeric", Toast.LENGTH_SHORT).show();
                pins[i].requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * Concatenates the 4 PIN digits into a single String.
     */
    private String getPinFromInputs() {
        return pin1.getText().toString().trim() +
                pin2.getText().toString().trim() +
                pin3.getText().toString().trim() +
                pin4.getText().toString().trim();
    }

    /**
     * Saves the PIN locally using SharedPreferences.
     */
    private void savePinLocally(String pin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PIN, pin);
        editor.apply();
    }
}

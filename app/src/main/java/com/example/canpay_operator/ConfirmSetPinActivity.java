package com.example.canpay_operator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfirmSetPinActivity extends AppCompatActivity {

    private EditText pin1, pin2, pin3, pin4;
    private Button btnConfirm;
    private ImageButton btnBack;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CanpayPrefs";
    private static final String KEY_SET_PIN = "set_pin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_set_pin); // Use your actual layout filename

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);

        btnConfirm = findViewById(R.id.btn_enter);
        btnBack = findViewById(R.id.btn_back);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupPinEditTexts();

        btnBack.setOnClickListener(v -> {
            // Navigate back to SetPinActivity
            Intent intent = new Intent(ConfirmSetPinActivity.this, SetPinActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            if (validatePinInputs()) {
                String enteredPin = getPinFromInputs();
                String savedPin = sharedPreferences.getString(KEY_SET_PIN, null);

                if (savedPin == null) {
                    Toast.makeText(this, "No PIN set. Please create a PIN first.", Toast.LENGTH_LONG).show();
                    // Optionally redirect to SetPinActivity
                    Intent intent = new Intent(ConfirmSetPinActivity.this, SetPinActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                if (enteredPin.equals(savedPin)) {
                    // PIN confirmed successfully, go to HomeActivity
                    Intent intent = new Intent(ConfirmSetPinActivity.this, HomeActivity.class);
                    // Clear back stack so user can't go back to PIN screens
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "PIN does not match. Please try again.", Toast.LENGTH_SHORT).show();
                    clearPinInputs();
                    pin1.requestFocus();
                }
            }
        });
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

    private String getPinFromInputs() {
        return pin1.getText().toString().trim() +
                pin2.getText().toString().trim() +
                pin3.getText().toString().trim() +
                pin4.getText().toString().trim();
    }

    private void clearPinInputs() {
        pin1.setText("");
        pin2.setText("");
        pin3.setText("");
        pin4.setText("");
    }
}

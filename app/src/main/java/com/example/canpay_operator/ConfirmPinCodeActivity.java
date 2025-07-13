package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.canpay_operator.utils.PreferenceManager;

public class ConfirmPinCodeActivity extends AppCompatActivity {

    private EditText pin1, pin2, pin3, pin4;
    private Button btnEnter;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_pin_code);

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        btnEnter = findViewById(R.id.btn_enter);
        btnBack = findViewById(R.id.btn_back);

        setupPinEditTexts();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmPinCodeActivity.this, PinCodeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnEnter.setOnClickListener(v -> {
            if (validatePinInputs()) {
                String enteredPin = getPinFromInputs();
                String savedPin = PreferenceManager.getUserPin(this);  // retrieve from secure prefs

                if (savedPin == null || savedPin.isEmpty()) {
                    Toast.makeText(this, "No PIN set. Please create a PIN first.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ConfirmPinCodeActivity.this, PinCodeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                if (enteredPin.equals(savedPin)) {
                    // Optionally: you can also validate token here if needed
                    String token = PreferenceManager.getToken(this);
                    if (token == null || token.isEmpty()) {
                        Toast.makeText(this, "Authorization token missing", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ConfirmPinCodeActivity.this, AccountFailureActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }

                    Intent intent = new Intent(ConfirmPinCodeActivity.this, AccountSuccessActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(ConfirmPinCodeActivity.this, AccountFailureActivity.class);
                    startActivity(intent);
                    finish();
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
        for (EditText pin : pins) {
            String digit = pin.getText().toString().trim();
            if (digit.isEmpty() || !digit.matches("\\d")) {
                Toast.makeText(this, "PIN must be 4 numeric digits", Toast.LENGTH_SHORT).show();
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
}

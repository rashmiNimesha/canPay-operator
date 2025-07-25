package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.canpay_operator.utils.PreferenceManager;

public class PinCodeActivity extends AppCompatActivity {

    private EditText pin1, pin2, pin3, pin4;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code);

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        btnEnter = findViewById(R.id.btn_enter);

        setupPinEditTexts();

        btnEnter.setOnClickListener(v -> {
            if (allPinsFilled()) {
                String pin = getEnteredPin();
                if (!pin.matches("\\d{4}")) {
                    Toast.makeText(PinCodeActivity.this, "PIN must be 4 numeric digits", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save PIN securely using PreferenceManager
                PreferenceManager.saveUserPin(PinCodeActivity.this, pin);

                Toast.makeText(PinCodeActivity.this, "PIN saved successfully", Toast.LENGTH_SHORT).show();

                // Navigate to ConfirmPinCodeActivity (pass token or user info if needed)
                Intent intent = new Intent(PinCodeActivity.this, ConfirmPinCodeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(PinCodeActivity.this, "Please enter all 4 digits", Toast.LENGTH_SHORT).show();
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

    private boolean allPinsFilled() {
        return !pin1.getText().toString().trim().isEmpty() &&
                !pin2.getText().toString().trim().isEmpty() &&
                !pin3.getText().toString().trim().isEmpty() &&
                !pin4.getText().toString().trim().isEmpty();
    }

    private String getEnteredPin() {
        return pin1.getText().toString().trim() +
                pin2.getText().toString().trim() +
                pin3.getText().toString().trim() +
                pin4.getText().toString().trim();
    }
}

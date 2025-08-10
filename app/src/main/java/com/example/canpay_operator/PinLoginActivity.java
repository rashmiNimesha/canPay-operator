package com.example.canpay_operator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class PinLoginActivity extends AppCompatActivity {

    private static final String TAG = "PinLoginActivity";

    private EditText pin1, pin2, pin3, pin4;
    private Button btnEnter;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CanpayPrefs";
    private static final String KEY_SET_PIN = "set_pin";
    private static final String KEY_JWT_TOKEN = "jwt_token"; // Key where JWT token is stored

    private static final String VALIDATE_TOKEN_URL = "http://10.0.2.2:8081/validate-token"; // Replace with your backend URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);

        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        btnEnter = findViewById(R.id.btn_enter);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupPinEditTexts();

        btnEnter.setOnClickListener(v -> {
            if (allPinsFilled()) {
                validateTokenThenPin();
            } else {
                Toast.makeText(this, "Please enter all 4 digits of your PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPinEditTexts() {
        EditText[] pins = {pin1, pin2, pin3, pin4};

        for (int i = 0; i < pins.length; i++) {
            final int index = i;
            EditText pinField = pins[i];

            // Add filters: max length 1, only digits allowed
            pinField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            pinField.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

            pinField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    // Immediate check: only allow empty or single digit
                    if (!s.toString().matches("\\d?")) { // empty or 1 digit
                        pinField.setError("Only digits allowed");
                        pinField.setText("");
                        return;
                    }
                    // Auto-move focus
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

    private void validateTokenThenPin() {
        String token = sharedPreferences.getString(KEY_JWT_TOKEN, null);
        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, VALIDATE_TOKEN_URL, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            validatePinAndLogin();
                        } else {
                            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                            redirectToLogin();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Unexpected response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Token validation error: " + error.getMessage());
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
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
            Toast.makeText(this, "No PIN set. Please set your PIN first.", Toast.LENGTH_LONG).show();
            clearPinInputs();
            pin1.requestFocus();
            return;
        }

        if (enteredPin.equals(savedPin)) {
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

    private void redirectToLogin() {
        Intent intent = new Intent(PinLoginActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

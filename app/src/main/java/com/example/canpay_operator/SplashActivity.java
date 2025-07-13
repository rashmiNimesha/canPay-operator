package com.example.canpay_operator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private static final String PREFS_NAME = "CanPayPrefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";  // Store your JWT token here

    private static final int SPLASH_DELAY = 2000; // 2 seconds delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(this::validateSessionWithBackend, SPLASH_DELAY);
    }

    private void validateSessionWithBackend() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(KEY_JWT_TOKEN, null);

        if (token == null || token.isEmpty()) {
            // No token saved, go to LoginActivity
            navigateToLogin();
            return;
        }

        String url = "http://10.0.2.2:8081";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            // Token valid, go to PinLoginActivity
                            navigateToPinLogin();
                        } else {
                            // Token invalid or expired, go to LoginActivity
                            navigateToLogin();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        navigateToLogin();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.getMessage());
                    Toast.makeText(SplashActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToPinLogin() {
        Intent intent = new Intent(SplashActivity.this, PinLoginActivity.class);
        startActivity(intent);
        finish();
    }
}

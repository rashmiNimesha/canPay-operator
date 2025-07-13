package com.example.canpay_operator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;

import org.json.JSONException;
import org.json.JSONObject;

public class PhoneNoActivity extends AppCompatActivity {
    private static final String TAG = "PhoneNoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_no);

        EditText etEmail = findViewById(R.id.et_phone);  // Assuming your EditText id is et_phone but holds email
        Button btnLogin = findViewById(R.id.btn_login);
        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvTermsLink = findViewById(R.id.tv_terms_link);

        // Back button: navigate to LoginActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(PhoneNoActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Terms & Conditions link: open in browser
        tvTermsLink.setOnClickListener(v -> {
            String url = "https://www.termsfeed.com/terms-conditions-generator/";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        // Login button: send OTP
        btnLogin.setOnClickListener(v -> {
            if (!btnLogin.isEnabled()) return;
            btnLogin.setEnabled(false);

            String email = etEmail.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                btnLogin.setEnabled(true);
                return;
            }

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("email", email);
            } catch (JSONException e) {
                Log.e(TAG, "JSON creation error", e);
                Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
                btnLogin.setEnabled(true);
                return;
            }

            ApiHelper.postJson(this, Endpoints.SEND_OTP, jsonBody, null, new ApiHelper.Callback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d(TAG, "OTP sent successfully: " + response.toString());
                    Toast.makeText(PhoneNoActivity.this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PhoneNoActivity.this, OtpActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    btnLogin.setEnabled(true);
                }

                @Override
                public void onError(VolleyError error) {
                    ApiHelper.handleVolleyError(PhoneNoActivity.this, error, TAG);
                    btnLogin.setEnabled(true);
                }
            });
        });
    }
}

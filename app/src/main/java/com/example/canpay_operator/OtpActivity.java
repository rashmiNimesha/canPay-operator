package com.example.canpay_operator;

import static com.android.volley.VolleyLog.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.VolleyError;
import com.example.canpay_operator.config.ApiConfig;
import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

public class OtpActivity extends AppCompatActivity {

    private EditText[] otpBoxes = new EditText[6];
    private TextView tvResend;
    private Button btnNext;
    private CountDownTimer timer;
    private int resendSeconds = 60;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otpBoxes[0] = findViewById(R.id.otp1);
        otpBoxes[1] = findViewById(R.id.otp2);
        otpBoxes[2] = findViewById(R.id.otp3);
        otpBoxes[3] = findViewById(R.id.otp4);
        otpBoxes[4] = findViewById(R.id.otp5);
        otpBoxes[5] = findViewById(R.id.otp6);
        tvResend = findViewById(R.id.tv_resend);
        btnNext = findViewById(R.id.btn_next);
        ImageButton btnBack = findViewById(R.id.btn_back);
        email = getIntent().getStringExtra("email");

        // Auto move focus between OTP boxes
        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpBoxes[index - 1].requestFocus();
                    }
                }
            });
        }

        btnNext.setOnClickListener(v -> handleOtpVerification());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(OtpActivity.this, PhoneNoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        startResendTimer();

        tvResend.setOnClickListener(v -> {
            if (tvResend.isEnabled()) {
                resendOtp(email);
                startResendTimer();
            }
        });
    }

    private void handleOtpVerification() {
        StringBuilder otp = new StringBuilder();
        for (EditText box : otpBoxes) {
            String digit = box.getText().toString().trim();
            if (digit.isEmpty()) {
                Toast.makeText(this, "Enter all 6 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            otp.append(digit);
        }

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Missing email", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("otp", otp.toString());
            body.put("role", ApiConfig.USER_ROLE);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error", e);
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiHelper.postJson(this, Endpoints.VERIFY_OTP, body, null, new ApiHelper.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");
                    boolean isNewUser = data.getBoolean("newUser");
                    String token = data.getString("token");
                    JSONObject profile = data.getJSONObject("profile");
                    String userEmail = profile.getString("email");
                    String userRole = profile.getString("role");
                    String userName = profile.optString("name", null);
                    String photo = profile.optString("photo", null);
                    String nic = profile.optString("nic", null);
                    String userId = profile.optString("id", "");  // Changed to String here

                    // Save user session with userId as String UUID
                    PreferenceManager.saveUserSession(OtpActivity.this, userEmail, token, userRole, userName, userId, nic, photo);
                    Log.d(TAG, "Saved session for email: " + userEmail);

                    if (isNewUser) {
                        Toast.makeText(OtpActivity.this, "OTP verified. Please complete your profile.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OtpActivity.this, NameActivity.class);
                        intent.putExtra("email", userEmail);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OtpActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OtpActivity.this, HomeActivity.class);
                        intent.putExtra("email", userEmail);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing response", e);
                    Toast.makeText(OtpActivity.this, "Invalid server response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                ApiHelper.handleVolleyError(OtpActivity.this, error, TAG);
                Toast.makeText(OtpActivity.this, "Invalid OTP or other error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startResendTimer() {
        tvResend.setEnabled(false);
        timer = new CountDownTimer(resendSeconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvResend.setText("RESEND CODE IN " + (millisUntilFinished / 1000) + " SECONDS");
            }
            public void onFinish() {
                tvResend.setText("RESEND CODE");
                tvResend.setEnabled(true);
            }
        }.start();
    }

    private void resendOtp(String email) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Missing email address", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        ApiHelper.postJson(this, Endpoints.SEND_OTP, body, null, new ApiHelper.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(OtpActivity.this, "OTP resent to " + email, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError error) {
                ApiHelper.handleVolleyError(OtpActivity.this, error, TAG);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}

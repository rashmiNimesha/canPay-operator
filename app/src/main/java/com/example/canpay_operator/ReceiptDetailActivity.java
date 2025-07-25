package com.example.canpay_operator;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReceiptDetailActivity extends AppCompatActivity {

    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);

        ImageButton btnClose = findViewById(R.id.btn_close);
        Button btnGoHome = findViewById(R.id.btn_go_home);

        TextView tvPaymentSuccess = findViewById(R.id.tv_payment_success);
        TextView tvPaidAmount = findViewById(R.id.tv_paid_amount);
        TextView tvConductor = findViewById(R.id.tv_conductor);
        TextView tvBusNumber = findViewById(R.id.tv_bus_number);
        TextView tvBusRoute = findViewById(R.id.tv_bus_route);
        TextView tvDateTime = findViewById(R.id.tv_date_time);

        // Get data from intent (replace with backend data later)
        Intent intent = getIntent();
        String paymentStatus = intent.getStringExtra("payment_status");
        String amount = intent.getStringExtra("amount");
        String conductor = intent.getStringExtra("conductor");
        String busNumber = intent.getStringExtra("bus_number");
        String busRoute = intent.getStringExtra("bus_route");
        String dateTime = intent.getStringExtra("date_time");

        // Set values with fallback for nulls
        tvPaymentSuccess.setText(paymentStatus != null ? paymentStatus : "Payment Successful!");
        tvPaidAmount.setText(amount != null ? amount : "LKR 0.00");
        tvConductor.setText(conductor != null ? conductor : "-");
        tvBusNumber.setText(busNumber != null ? busNumber : "-");
        tvBusRoute.setText(busRoute != null ? busRoute : "-");
        tvDateTime.setText(dateTime != null ? dateTime : "-");

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Prevent accidental double clicks
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();

                // Go to TransactionsActivity
                Intent intent = new Intent(ReceiptDetailActivity.this, TransactionsFragment.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();

                // Go to HomeActivity
                Intent intent = new Intent(ReceiptDetailActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}

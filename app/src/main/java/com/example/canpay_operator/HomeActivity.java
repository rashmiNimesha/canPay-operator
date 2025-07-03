package com.example.canpay_operator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // Assigned UI
    private TextView tvName, tvBusNumber, tvEarnings;
    private RecyclerView rvTransactions;
    private TextView tvNoTransactions;

    // Unassigned UI
    private ImageView imgQr;
    private TextView tvPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("CanpayPrefs", MODE_PRIVATE);
        boolean isAssigned = prefs.getBoolean("is_assigned", false);

        if (!isAssigned) {
            setContentView(R.layout.activity_home_unassigned);
            setupUnassignedUI(prefs);
        } else {
            setContentView(R.layout.activity_home_assigned);
            setupAssignedUI(prefs);
        }

        // Bottom navigation setup (common for both)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void setupUnassignedUI(SharedPreferences prefs) {
        imgQr = findViewById(R.id.img_qr);
        tvPhone = findViewById(R.id.tv_phone);

        String phone = prefs.getString("phone", "071 23 45 678");
        tvPhone.setText(phone);

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(phone, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);
            imgQr.setImageBitmap(bitmap);
        } catch (Exception e) {
            imgQr.setImageResource(R.drawable.ic_no_transactions); // fallback image
        }
    }

    private void setupAssignedUI(SharedPreferences prefs) {
        tvName = findViewById(R.id.tv_name);
        tvBusNumber = findViewById(R.id.tv_bus_number);
        tvEarnings = findViewById(R.id.tv_earnings);
        rvTransactions = findViewById(R.id.rv_transactions);

        String name = prefs.getString("user_name", "Sehan");
        String busNumber = prefs.getString("bus_number", "ND-1234");
        float earnings = prefs.getFloat("earnings", 7950.00f);

        tvName.setText(name);
        tvBusNumber.setText(busNumber);
        tvEarnings.setText("LKR " + String.format("%,.2f", earnings));

        List<Transaction> transactions = loadTransactions();

        if (transactions.isEmpty()) {
            findViewById(R.id.layout_no_transactions).setVisibility(android.view.View.VISIBLE);
            rvTransactions.setVisibility(android.view.View.GONE);
        } else {
            findViewById(R.id.layout_no_transactions).setVisibility(android.view.View.GONE);
            rvTransactions.setVisibility(android.view.View.VISIBLE);
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setAdapter(new TransactionAdapter(transactions));
        }
    }

    // Dummy transaction loader
    private List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        // Uncomment for demo:
        /*
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 970.00));
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 570.00));
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 570.00));
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 570.00));
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 570.00));
        */
        return list;
    }
}

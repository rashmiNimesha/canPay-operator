package com.example.canpay_operator;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private static final String PREFS_NAME = "CanpayPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Layout with fragment container + bottom nav

        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Load default fragment based on assignment state
        loadHomeFragmentBasedOnAssignment();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_home) {
                selectedFragment = getHomeFragmentBasedOnAssignment();
            } else if (id == R.id.nav_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (id == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            //} else if (id == R.id.nav_settings) {
             //   selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void loadHomeFragmentBasedOnAssignment() {
        Fragment homeFragment = getHomeFragmentBasedOnAssignment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private Fragment getHomeFragmentBasedOnAssignment() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isAssigned = prefs.getBoolean("is_assigned", false);
        if (isAssigned) {
            return new HomeAssignedFragment();
        } else {
            return new HomeUnassignedFragment();
        }
    }
}

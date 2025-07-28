package com.example.canpay_operator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;
import com.example.canpay_operator.config.HiveMqttManager;

import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeActivity extends AppCompatActivity {
    private static final Logger logger = LoggerFactory.getLogger(HomeActivity.class);
    private BottomNavigationView bottomNavigationView;
    private HiveMqttManager mqttManager;
    private final String busId = "31fe8d9e-e2b0-4ca5-a468-8190a465690a"; // Replace dynamically if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        mqttManager = new HiveMqttManager(busId);

        logger.info("Attempting MQTT connect...");
        mqttManager.connect(
                () -> {
                    logger.info("MQTT Connected to HiveMQ");
                    logger.info("Subscribing to MQTT topic: bus/" + busId + "/payment");
                    mqttManager.subscribe(payload -> {
                        logger.info("MQTT message received: " + payload);
                        try {
                            logger.info("Payment notification received: " + payload);
                            JSONObject json = new JSONObject(payload);
                            String amount = json.getString("amount");
                            String date = "Today";
                            Transaction transaction = new Transaction(date, "Payment Received", Double.parseDouble(amount));

                            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                            if (currentFragment instanceof HomeAssignedFragment) {
                                runOnUiThread(() -> ((HomeAssignedFragment) currentFragment).addTransaction(transaction));
                            }
                            showNotification("Payment received: LKR " + amount);
                        } catch (Exception e) {
                            logger.error("Error processing MQTT message: " + e.getMessage(), e);
                        }
                    });
                    logger.info("MQTT subscribe() called");
                },
                throwable -> logger.error("MQTT connection failed: " + throwable.getMessage(), throwable)
        );

        // Add a delayed log to check if no message is received after 30 seconds
        new android.os.Handler().postDelayed(() -> {
            logger.warn("No MQTT message received after 30 seconds. Check topic, broker, and backend publishing.");
        }, 30000);

        checkAssignmentStatus();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                checkAssignmentStatus();
                return true;
            } else if (id == R.id.nav_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (id == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
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

    private void checkAssignmentStatus() {
        String operatorId = PreferenceManager.getUserId(this);
        String token = PreferenceManager.getToken(this);
        String endpoint = Endpoints.GET_OPERATOR_ASSIGNMENT + operatorId;

        logger.info("Checking assignment status for operatorId: " + operatorId);

        ApiHelper.getJson(this, endpoint, token, new ApiHelper.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                JSONObject data = response.optJSONObject("data");
                boolean assigned;
                if (data != null) {
                    assigned = data.optBoolean("assigned", false);
                    if (assigned) {
                        JSONObject bus = data.optJSONObject("bus");
                        if (bus != null) {
                            String busNumber = bus.optString("busNumber", null);
                            String busID = bus.optString("id", null);
                            if (busNumber != null) {
                                PreferenceManager.setBusNumber(HomeActivity.this, busNumber);
                                PreferenceManager.setBusID(HomeActivity.this, busID);
                            }
                        }
                    }
                } else {
                    assigned = false;
                }
                runOnUiThread(() -> loadHomeFragment(assigned));
            }

            @Override
            public void onError(VolleyError error) {
                ApiHelper.handleVolleyError(HomeActivity.this, error, "HomeActivity");
                runOnUiThread(() -> loadHomeFragment(false));
            }
        });
    }

    private void loadHomeFragment(boolean isAssigned) {
        Fragment homeFragment = isAssigned ? new HomeAssignedFragment() : new HomeUnassignedFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    private void showNotification(String message) {
        String channelId = "payment_notifications";
        int notificationId = (int) System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Payment")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttManager.disconnect();
    }
}
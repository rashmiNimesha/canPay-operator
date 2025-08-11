package com.example.canpay_operator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;
import com.example.canpay_operator.config.HiveMqttManager;

import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.NotificationStore;
import com.example.canpay_operator.utils.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeActivity extends AppCompatActivity {
    private static final Logger logger = LoggerFactory.getLogger(HomeActivity.class);
    private BottomNavigationView bottomNavigationView;
    private HiveMqttManager mqttManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_nav);

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

    private void setupMqttIfAssigned() {
        String busId = PreferenceManager.getBusID(this);
        if (busId == null) {
            logger.warn("No busId found in preferences, MQTT not initialized.");
            return;
        }
        if (mqttManager != null) {
            mqttManager.disconnect();
        }
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
                            String passengerName = json.optString("passengerName", "Passenger");

                            // Remove TransactionStore usage, instead call API to refresh transactions
                            // TransactionStore.addTransaction(getApplicationContext(), transaction);

                            // Store notification in NotificationStore
                            NotificationStore.getInstance(getApplicationContext()).addNotification(
                                "Payment received: " + passengerName + " LKR " + amount
                            );

                            // Call API to refresh transactions in HomeAssignedFragment
                            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                            if (currentFragment instanceof HomeAssignedFragment) {
                                runOnUiThread(() -> ((HomeAssignedFragment) currentFragment).refreshTransactions());
                            }
                            // Vibrate and play sound for user feedback
                            runOnUiThread(() -> {
                                vibrateAndPlaySound();
                                showNotification("Payment received: " + passengerName + " LKR " + amount);
                            });
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
                            }
                            if (busID != null) {
                                PreferenceManager.setBusID(HomeActivity.this, busID);
                            }
                        }
                        // Setup MQTT only if assigned
                        runOnUiThread(() -> setupMqttIfAssigned());
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

    // Add this method for vibration and sound
    private void vibrateAndPlaySound() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(new long[]{0, 300, 100, 300}, -1);
            }
        } catch (Exception e) {
            logger.warn("Vibration failed: " + e.getMessage());
        }
        try {
            MediaPlayer mp = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception e) {
            logger.warn("Sound playback failed: " + e.getMessage());
        }
    }

    private void showNotification(String message) {
        String channelId = "payment_notifications";
        int notificationId = (int) System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, null);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Payment")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        notificationManager.notify(notificationId, builder.build());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttManager != null) {
            mqttManager.disconnect();
        }
    }
}
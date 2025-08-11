package com.example.canpay_operator;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

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

    private static final String PAYMENT_CHANNEL_ID = "payment_notifications_v2"; // new channel id
    private static final int REQ_NOTIF_PERMISSION = 1001;

    private BottomNavigationView bottomNavigationView;
    private HiveMqttManager mqttManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Create/update channel and ask runtime permission (Android 13+)
        createPaymentChannel();
        requestPostNotificationsPermissionIfNeeded();

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

                            // Store notification locally (for in-app list)
                            NotificationStore.getInstance(getApplicationContext()).addNotification(
                                    "Payment received: " + passengerName + " LKR " + amount
                            );

                            // If HomeAssignedFragment visible, refresh its transactions
                            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                            if (currentFragment instanceof HomeAssignedFragment) {
                                runOnUiThread(() -> ((HomeAssignedFragment) currentFragment).refreshTransactions());
                            }

                            // Immediate strong haptic + system notification with default sound
                            runOnUiThread(() -> {
                                strongVibrateNow();
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

        // Delayed log to help debug if nothing arrives
        new Handler().postDelayed(() -> {
            logger.warn("No MQTT message received after 30 seconds. Check topic, broker, and backend publishing.");
        }, 30_000);
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

    /**
     * Strong vibration using modern API (API 26+), simple fallback pre-26.
     */
    private void strongVibrateNow() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(
                        700, VibrationEffect.DEFAULT_AMPLITUDE)); // ~0.7s strong buzz
            } else {
                vibrator.vibrate(700);
            }
        } catch (Exception e) {
            logger.warn("Vibration failed: " + e.getMessage());
        }
    }

    /**
     * Posts a notification. On O+ the sound/vibration are defined by the channel.
     * On pre-O devices we set defaults directly on the Builder.
     */
    private void showNotification(String message) {
        String channelId = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? PAYMENT_CHANNEL_ID
                : ""; // ignored pre-O

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Payment")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH); // affects <26

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Pre-O: ask for default sound + vibrate
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
            builder.setVibrate(new long[]{0, 600, 150, 700});
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * Create a channel that uses the user's default notification sound and strong vibration.
     * If an older channel existed with different settings, delete it so new settings take effect.
     */
    private void createPaymentChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            try {
                // Remove legacy channel if present so new config applies
                nm.deleteNotificationChannel("payment_notifications");
            } catch (Exception ignored) {}

            NotificationChannel channel = new NotificationChannel(
                    PAYMENT_CHANNEL_ID,
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_HIGH // plays sound
            );

            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 600, 150, 700});

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(defaultSound, attrs);

            nm.createNotificationChannel(channel);
        }
    }

    /**
     * Android 13+ needs runtime permission to post notifications.
     */
    private void requestPostNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF_PERMISSION);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttManager != null) {
            mqttManager.disconnect();
        }
    }
}
package com.example.canpay_operator.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotificationStore {
    private static NotificationStore instance;
    private static final String PREFS_NAME = "CanpayPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private List<String> notifications = new ArrayList<>();
    private SharedPreferences prefs;

    private NotificationStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadFromPrefs();
    }

    public static synchronized NotificationStore getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationStore(context);
        }
        return instance;
    }

    public synchronized void addNotification(String message) {
        notifications.add(0, message); // newest first
        saveToPrefs();
    }

    public synchronized List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public synchronized void clear() {
        notifications.clear();
        saveToPrefs();
    }

    private void loadFromPrefs() {
        String joined = prefs.getString(KEY_NOTIFICATIONS, "");
        if (!joined.isEmpty()) {
            notifications = new ArrayList<>(Arrays.asList(joined.split("\u2028")));
        }
    }

    private void saveToPrefs() {
        String joined = String.join("\u2028", notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS, joined).apply();
    }
}

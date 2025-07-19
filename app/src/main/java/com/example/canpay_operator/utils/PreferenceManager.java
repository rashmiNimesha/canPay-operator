package com.example.canpay_operator.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "MyPrefs";

    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_PIN = "pin";

    // New keys
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USERNAME = "userName";
    private static final String KEY_NIC = "nic";
    private static final String KEY_PHOTO = "photo";

    // Save all user session details at once
    public static void saveUserSession(Context context,
                                       String email,
                                       String token,
                                       String role,
                                       String userName,
                                       String userId,    // <-- UUID string
                                       String nic,
                                       String photo) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_USERNAME, userName);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_NIC, nic);
        editor.putString(KEY_PHOTO, photo);

        editor.apply();
    }

    // Existing methods below:

    public static void setEmail(Context context, String email) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_EMAIL, email).apply();
    }

    public static String getEmail(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_EMAIL, "");
    }

    public static void setPhone(Context context, String phone) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PHONE, phone).apply();
    }

    public static String getPhone(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PHONE, "");
    }

    public static void setUserId(Context context, String userId) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public static String getUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USER_ID, "");
    }

    public static void setPin(Context context, String pin) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PIN, pin).apply();
    }

    public static String getPin(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PIN, "");
    }

    /**
     * Alias for getPin to support existing calls to getUserPin
     */
    public static String getUserPin(Context context) {
        return getPin(context);
    }

    // New getters for the additional fields

    public static String getToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_TOKEN, "");
    }

    public static String getRole(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_ROLE, "");
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USERNAME, "");
    }

    public static String getNic(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_NIC, "");
    }

    public static String getPhoto(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PHOTO, "");
    }

    public static void saveUserPin(Context context, String pin) {
        setPin(context, pin);
    }
}

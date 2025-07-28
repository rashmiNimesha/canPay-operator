package com.example.canpay_operator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PreferenceManager {

    private static final String TAG = "PreferenceManager";
    private static final String PREF_NAME = "CanPayPrefs";
    private static final String KEY_USER_ID = "id";
    private static final String KEY_PIN = "pin";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NIC = "nic";

    private static final String KEY_USER_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BUS_PLATE_NUMBER = "busNumber";
    private static final String KEY_BUS_ID = "busID";



    //
//    // Save all user session details at once
//    public static void saveUserSession(Context context,
//                                       String email,
//                                       String token,
//                                       String role,
//                                       String userName,
//                                       String userId,    // <-- UUID string
//                                       String nic,
//                                       String photo) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//
//        editor.putString(KEY_EMAIL, email);
//        editor.putString(KEY_TOKEN, token);
//        editor.putString(KEY_ROLE, role);
//        editor.putString(KEY_USERNAME, userName);
//        editor.putString(KEY_USER_ID, userId);
//        editor.putString(KEY_NIC, nic);
//        editor.putString(KEY_PHOTO, photo);
//
//        editor.apply();
//    }
//
//    // Existing methods below:
//
//    public static void setEmail(Context context, String email) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        preferences.edit().putString(KEY_EMAIL, email).apply();
//    }
//
//    public static String getEmail(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_EMAIL, "");
//    }
//
//    public static void setPhone(Context context, String phone) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        preferences.edit().putString(KEY_PHONE, phone).apply();
//    }
//
//    public static String getPhone(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_PHONE, "");
//    }
//
//    public static void setUserId(Context context, String userId) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        preferences.edit().putString(KEY_USER_ID, userId).apply();
//    }
//
//    public static String getUserId(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_USER_ID, "");
//    }
//
    public static void setPin(Context context, String pin) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_PIN, pin).apply();
    }

    //
    public static String getPin(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PIN, "");
    }

    //
//    /**
//     * Alias for getPin to support existing calls to getUserPin
//     */
    public static String getUserPin(Context context) {
        return getPin(context);
    }
//
//    // New getters for the additional fields
//
//    public static String getToken(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_TOKEN, "");
//    }
//
//    public static String getRole(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_ROLE, "");
//    }
//
//    public static String getUserName(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_USERNAME, "");
//    }
//
//    public static String getNic(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_NIC, "");
//    }
//
//    public static String getPhoto(Context context) {
//        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        return preferences.getString(KEY_PHOTO, "");
//    }

    public static void saveUserPin(Context context, String pin) {
        setPin(context, pin);
    }


    private static SharedPreferences getEncryptedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences", e);
            throw new RuntimeException("Failed to initialize encrypted preferences", e);
        }
    }

    public static void saveUserSession(Context context, String email, String token, String role, String userName, String userId, String nic) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();
            if (email != null) editor.putString(KEY_EMAIL, email);
            if (token != null) editor.putString(KEY_TOKEN, token);
            if (role != null) editor.putString(KEY_ROLE, role);
            if (userName != null) editor.putString(KEY_USER_NAME, userName);
            if (userId != null) editor.putString(KEY_USER_ID, userId);
            if (nic != null) editor.putString(KEY_NIC, nic);
            editor.apply();
            Log.d(TAG, "Saved user session: email=" + email + ", token=" + token + ", role=" + role);
        } catch (Exception e) {
            Log.e(TAG, "Error saving user session", e);
            throw new RuntimeException("Failed to save user session", e);
        }
    }

    public static String getEmail(Context context) {
        String email = getEncryptedPrefs(context).getString(KEY_EMAIL, null);
        Log.d(TAG, "Retrieved email: " + email);
        return email;
    }

    public static String getToken(Context context) {
        String token = getEncryptedPrefs(context).getString("token", null);
        Log.d(TAG, "Retrieved token: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
        return token;
    }

    public static String getRole(Context context) {
        String role = getEncryptedPrefs(context).getString(KEY_ROLE, null);
        Log.d(TAG, "Retrieved role: " + role);
        return role;
    }

    public static String getUserName(Context context) {
        String userName = getEncryptedPrefs(context).getString(KEY_USER_NAME, "User");
        Log.d(TAG, "Retrieved user_name: " + userName);
        return userName;
    }

    public static String getUserId(Context context) {
        return getEncryptedPrefs(context).getString(KEY_USER_ID, null);
    }

    public static String getNic(Context context) {
        String nic = getEncryptedPrefs(context).getString(KEY_NIC, null);
        Log.d(TAG, "Retrieved nic: " + nic);
        return nic;
    }

    public static void setUserName(Context context, String userName) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_NAME, userName);
            editor.apply();
            Log.d(TAG, "Set user_name: " + userName);
        } catch (Exception e) {
            Log.e(TAG, "Error setting user_name", e);
            throw new RuntimeException("Failed to set user name", e);
        }
    }

    public static void setEmail(Context context, String email) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_EMAIL, email);
            editor.apply();
            Log.d(TAG, "Set email: " + email);
        } catch (Exception e) {
            Log.e(TAG, "Error setting email", e);
            throw new RuntimeException("Failed to set email", e);
        }
    }

    public static void setBusNumber(Context context, String busNumber) {
        SharedPreferences prefs = getEncryptedPrefs(context);
        prefs.edit().putString(KEY_BUS_PLATE_NUMBER, busNumber).apply();
    }

    public static String getBusNumber(Context context) {
        return getEncryptedPrefs(context).getString(KEY_BUS_PLATE_NUMBER, null);
    }

    public static void setBusID(Context context, String busID) {
        SharedPreferences prefs = getEncryptedPrefs(context);
        prefs.edit().putString(KEY_BUS_ID, busID).apply();
    }

    public static String getBusID(Context context) {
        return getEncryptedPrefs(context).getString(KEY_BUS_ID, null);
    }


    public static void clearSession(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            prefs.edit().clear().apply();
            Log.d(TAG, "Cleared user session");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user session", e);
        }
    }
}

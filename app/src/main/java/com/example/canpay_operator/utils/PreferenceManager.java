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

    // Save user session info
    public static void saveUserSession(Context context, String email, String token, String role, String userName, int userId, String nic, String photo) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("email", email != null ? email : "");
            editor.putString("token", token != null ? token : "");
            editor.putString("role", role != null ? role : "");
            editor.putString("user_name", userName != null ? userName : "");
            editor.putInt("user_id", userId);
            editor.putString("nic", nic != null ? nic : "");
            editor.apply();
            Log.d(TAG, "Saved user session: email=" + email + ", token=" + token + ", role=" + role);
        } catch (Exception e) {
            Log.e(TAG, "Error saving user session", e);
            throw new RuntimeException("Failed to save user session", e);
        }
    }

    // Save 4-digit user PIN
    public static void saveUserPin(Context context, String pin) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            prefs.edit().putString("user_pin", pin).apply();
            Log.d(TAG, "Saved user PIN");
        } catch (Exception e) {
            Log.e(TAG, "Error saving PIN", e);
        }
    }

    // Retrieve saved PIN
    public static String getUserPin(Context context) {
        try {
            return getEncryptedPrefs(context).getString("user_pin", "");
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving PIN", e);
            return "";
        }
    }

    // Getters for session values
    public static String getEmail(Context context) {
        return getEncryptedPrefs(context).getString("email", "");
    }

    public static String getToken(Context context) {
        String token = getEncryptedPrefs(context).getString("token", "");
        Log.d(TAG, "Retrieved token: " + (token.length() > 0 ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
        return token;
    }

    public static String getRole(Context context) {
        return getEncryptedPrefs(context).getString("role", "");
    }

    public static String getUserName(Context context) {
        return getEncryptedPrefs(context).getString("user_name", "User");
    }

    public static int getUserId(Context context) {
        return getEncryptedPrefs(context).getInt("user_id", 0);
    }

    public static String getNic(Context context) {
        return getEncryptedPrefs(context).getString("nic", "");
    }

    // Clear all stored preferences
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

package com.example.canpay_operator.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class JwtUtils {
    public static JSONObject decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);
            return new JSONObject(payloadJson);
        } catch (Exception e) {
            Log.e("JWT_UTIL", "Failed to decode JWT payload: " + e.getMessage());
            return null;
        }
    }

    public static String getRoleFromToken(String token) {
        JSONObject payload = decodeJwtPayload(token);
        return payload != null ? payload.optString("role", "") : "";
    }

    public static int getUserIdFromToken(String token) {
        JSONObject payload = decodeJwtPayload(token);
        return payload != null ? payload.optInt("id", -1) : -1;
    }

    public static String getEmailFromToken(String token) {
        JSONObject payload = decodeJwtPayload(token);
        return payload != null ? payload.optString("sub", "") : "";
    }

    public static String getNameFromToken(String token) {
        JSONObject payload = decodeJwtPayload(token);
        return payload != null ? payload.optString("name", "") : "";
    }
    public static String getNicFromToken(String token) {
        JSONObject payload = decodeJwtPayload(token);
        return payload != null ? payload.optString("nic", "") : "";
    }
}

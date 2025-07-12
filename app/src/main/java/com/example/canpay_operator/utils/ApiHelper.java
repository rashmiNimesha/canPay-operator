package com.example.canpay_operator.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.canpay_operator.config.ApiConfig;
import com.example.canpay_operator.request.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiHelper {
    private static final String TAG = "ApiHelper";

    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }

    public static void postJson(Context context, String endpoint, JSONObject body, String token, Callback callback) {
        String url = ApiConfig.getBaseUrl() + endpoint;
        Log.d(TAG, "Posting to URL: " + url + ", with body: " + body.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Log.d(TAG, "Success response: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error in request to " + url, error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "HTTP Status Code: " + error.networkResponse.statusCode);
                    }
                    callback.onError(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                    Log.d(TAG, "Added Authorization header: Bearer " + token.substring(0, Math.min(token.length(), 20)) + "...");
                } else {
                    Log.w(TAG, "No token provided for request");
                }
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // Increased to 30s for remote server
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }


    public static void patchJson(Context context, String endpoint, JSONObject body, String token, Callback callback) {
        String url = ApiConfig.getBaseUrl() + endpoint;
        Log.d(TAG, "Patching to URL: " + url + ", with body: " + body.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                body,
                response -> {
                    Log.d(TAG, "Success response: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Error in request to " + url + ": " + error.toString(), error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "HTTP Status Code: " + error.networkResponse.statusCode);
                    }
                    callback.onError(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                    Log.d(TAG, "Added Authorization header: Bearer " + token.substring(0, Math.min(token.length(), 20)) + "...");
                } else {
                    Log.w(TAG, "No token provided for request");
                }
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, // Increased timeout to 15s for remote server
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static void handleVolleyError(Context context, VolleyError error, String tag) {
        String errorMessage = "Unknown error occurred";

        if (error.networkResponse != null && error.networkResponse.data != null) {
            int statusCode = error.networkResponse.statusCode;
            Log.e(tag, "HTTP Status Code: " + statusCode);

            try {
                String responseBody = new String(error.networkResponse.data, "UTF-8");
                JSONObject errorJson = new JSONObject(responseBody);
                errorMessage = errorJson.optString("message", "Server error");
                Log.e(tag, "Error Response: " + responseBody);
            } catch (Exception e) {
                Log.e(tag, "Error parsing error response", e);
            }

            switch (statusCode) {
                case 400:
                    errorMessage = errorMessage.contains("Server error") ? "Bad request - please check your input" : errorMessage;
                    break;
                case 401:
                    errorMessage = errorMessage.contains("Server error") ? "Unauthorized - invalid token" : errorMessage;
                    break;
                case 404:
                    errorMessage = "Server endpoint not found";
                    break;
                case 500:
                    errorMessage = errorMessage.contains("Server error") ? "Server error - please try again later" : errorMessage;
                    break;
                default:
                    errorMessage = errorMessage.contains("Server error") ? "Server error (Code: " + statusCode + ")" : errorMessage;
            }
        } else {
            if (error.getCause() instanceof java.net.ConnectException) {
                errorMessage = "Cannot connect to server - check your connection";
            } else if (error.getCause() instanceof java.net.UnknownHostException) {
                errorMessage = "Server not found - check server address";
            } else if (error.getCause() instanceof java.net.SocketTimeoutException) {
                errorMessage = "Request timeout - server might be slow";
            } else {
                errorMessage = error.getMessage() != null ? "Network error: " + error.getMessage() : "Unknown network error";
            }
        }

        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(tag, "Final error message: " + errorMessage);
    }
}

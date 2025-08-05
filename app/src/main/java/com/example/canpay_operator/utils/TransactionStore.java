package com.example.canpay_operator.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.canpay_operator.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransactionStore {
    private static final String PREFS_NAME = "CanpayPrefs";
    private static final String KEY_TRANSACTIONS = "transactions";

    public static void addTransaction(Context context, Transaction transaction) {
        List<Transaction> transactions = getTransactions(context);
        transactions.add(0, transaction); // newest first
        saveTransactions(context, transactions);
    }

    public static List<Transaction> getTransactions(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TRANSACTIONS, "[]");
        List<Transaction> transactions = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String date = obj.optString("date");
                String description = obj.optString("description");
                double amount = obj.optDouble("amount");
                transactions.add(new Transaction(date, description, amount));
            }
        } catch (JSONException e) {
            // ignore
        }
        return transactions;
    }

    private static void saveTransactions(Context context, List<Transaction> transactions) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (Transaction t : transactions) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("date", t.getDate());
                obj.put("description", t.getDescription());
                obj.put("amount", t.getAmount());
                arr.put(obj);
            } catch (JSONException e) {
                // ignore
            }
        }
        prefs.edit().putString(KEY_TRANSACTIONS, arr.toString()).apply();
    }

    public static void clear(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_TRANSACTIONS).apply();
    }
}


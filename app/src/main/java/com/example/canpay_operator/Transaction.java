package com.example.canpay_operator;

public class Transaction {
    public String date;
    public String description;
    public double amount;

    public Transaction(String date, String description, double amount) {
        this.date = date;
        this.description = description;
        this.amount = amount;
    }

    public float getAmount() {
        return (float) amount;
    }

    public String getDescription() {
        return description;
    }
}

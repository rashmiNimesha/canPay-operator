package com.example.canpay_operator;

public class Transaction {
    public String date;
    public String description;
    public String note;
    public double amount;

    public Transaction(String date, String description, double amount) {
        this.date = date;
        this.description = description;
        this.amount = amount;
    }

    public Transaction(String date, String description, String note, double amount) {
        this.date = date;
        this.description = description;
        this.note = note;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return (float) amount;
    }

    public String getDescription() {
        return description;
    }
}

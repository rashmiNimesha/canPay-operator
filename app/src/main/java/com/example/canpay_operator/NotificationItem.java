package com.example.canpay_operator;

public class NotificationItem {
    public String title;
    public String date;
    public int iconResId;
    public boolean isUnread;

    public NotificationItem(int iconResId, String title, String date, boolean isUnread) {
        this.iconResId = iconResId;
        this.title = title;
        this.date = date;
        this.isUnread = isUnread;
    }
}


package com.example.canpay_operator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private List<NotificationItem> notifications;

    public NotificationsAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem n = notifications.get(position);
        holder.ivIcon.setImageResource(n.iconResId);
        holder.tvTitle.setText(n.title);
        holder.tvDate.setText(n.date);
        holder.ivUnread.setVisibility(n.isUnread ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivUnread;
        TextView tvTitle, tvDate;
        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvDate = itemView.findViewById(R.id.tv_notification_date);
            ivUnread = itemView.findViewById(R.id.iv_unread_dot);
        }
    }
}

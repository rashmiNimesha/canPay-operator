package com.example.canpay_operator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private LinearLayout llEmptyState;
    private NotificationsAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNotifications = view.findViewById(R.id.rv_notifications);
        llEmptyState = view.findViewById(R.id.ll_empty_state);

        // Load notifications (replace with real data source)
        notificationList = loadNotifications();

        if (notificationList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);

            rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
            notificationAdapter = new NotificationsAdapter(notificationList);
            rvNotifications.setAdapter(notificationAdapter);
        }
    }

    // Dummy data loader - replace with your real notifications
    private List<NotificationItem> loadNotifications() {
        List<NotificationItem> list = new ArrayList<>();
        // Sample data for testing
        list.add(new NotificationItem(R.drawable.ic_notification, "Your payment was successful.", "Nov 20, 2024", true));
        list.add(new NotificationItem(R.drawable.ic_notification, "New feature released!", "Nov 19, 2024", false));
        return list;
    }
}

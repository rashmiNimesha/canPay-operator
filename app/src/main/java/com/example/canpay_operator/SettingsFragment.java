package com.example.canpay_operator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private TextView tvAssignedBus, tvName, tvPhone, tvNIC;
    private LinearLayout layoutBusDetails, layoutChangePin, layoutLogout;

    private static final String PREFS_NAME = "CanpayPrefs";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAssignedBus = view.findViewById(R.id.tvAssignedBus);
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvNIC = view.findViewById(R.id.tvNIC);

        layoutBusDetails = view.findViewById(R.id.layoutBusDetails);
        layoutChangePin = view.findViewById(R.id.layoutChangePin);
        layoutLogout = view.findViewById(R.id.layoutLogout);

        loadUserDetailsFromPrefs();

        // Handle click on Assigned Bus section - open QR code activity
        layoutBusDetails.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AssignedBusQRActivity.class);
            startActivity(intent);
        });

        // Handle click on Change PIN section - open current PIN activity
        layoutChangePin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CurrentPinActivity.class);
            startActivity(intent);
        });

        // Show logout dialog on logout layout click
        layoutLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserDetailsFromPrefs() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String assignedBus = prefs.getString("bus_number", "ND - 1234");
        String operatorName = prefs.getString("operator_name", "Gamage");
        String operatorPhone = prefs.getString("operator_phone", "+94 71 12 12 123");
        String operatorNIC = prefs.getString("operator_nic", "2000XXXXXXXXX");

        tvAssignedBus.setText(assignedBus);
        tvName.setText(operatorName);
        tvPhone.setText(operatorPhone);
        tvNIC.setText(operatorNIC);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true); // Allow cancel on outside touch or back press

        Button btnLogout = dialogView.findViewById(R.id.btnLogout);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            // Clear session or preferences on logout
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Navigate to LoginActivity and clear back stack
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Optional: set dialog width for better appearance
        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.85);
            dialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }
}

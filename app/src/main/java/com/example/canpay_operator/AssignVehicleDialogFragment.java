package com.example.canpay_operator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AssignVehicleDialogFragment extends DialogFragment {

    public interface AssignVehicleListener {
        void onAcceptAssign(String ownerName, String busNumber);
        void onRejectAssign(String ownerName, String busNumber);
    }

    private static final String ARG_OWNER_NAME = "owner_name";
    private static final String ARG_BUS_NUMBER = "bus_number";
    private AssignVehicleListener listener;

    public static AssignVehicleDialogFragment newInstance(String ownerName, String busNumber) {
        AssignVehicleDialogFragment fragment = new AssignVehicleDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OWNER_NAME, ownerName);
        args.putString(ARG_BUS_NUMBER, busNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AssignVehicleListener) {
            listener = (AssignVehicleListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AssignVehicleListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_assign_vehicle, null);

        String ownerName = getArguments().getString(ARG_OWNER_NAME, "Someone");
        String busNumber = getArguments().getString(ARG_BUS_NUMBER, "ND-1234");

        TextView tvTitle = view.findViewById(R.id.tv_assign_title);
        tvTitle.setText(ownerName + " is trying to assign you to bus " + busNumber);

        Button btnAccept = view.findViewById(R.id.btn_accept_assign);
        Button btnReject = view.findViewById(R.id.btn_reject_assign);

        btnAccept.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onAcceptAssign(ownerName, busNumber);
        });

        btnReject.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onRejectAssign(ownerName, busNumber);
        });

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(view);
        dialog.setCancelable(false);
        return dialog;
    }
}

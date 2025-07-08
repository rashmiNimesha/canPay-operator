package com.example.canpay_operator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeAssignedFragment extends Fragment {

    private TextView tvName, tvBusNumber, tvEarnings;
    private RecyclerView rvTransactions;
    private LinearLayout layoutNoTransactions;

    private static final String PREFS_NAME = "CanpayPrefs";

    public HomeAssignedFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_assigned, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tv_name);
        tvBusNumber = view.findViewById(R.id.tv_bus_number);
        tvEarnings = view.findViewById(R.id.tv_earnings);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        layoutNoTransactions = view.findViewById(R.id.layout_no_transactions);

        loadUserDataAndSetupUI();
    }

    private void loadUserDataAndSetupUI() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);

        String name = prefs.getString("user_name", "Sehan");
        String busNumber = prefs.getString("bus_number", "ND-1234");
        float earnings = prefs.getFloat("earnings", 7950.00f);

        tvName.setText(name);
        tvBusNumber.setText(busNumber);
        tvEarnings.setText(String.format("LKR %,.2f", earnings));

        List<Transaction> transactions = loadTransactions();

        if (transactions.isEmpty()) {
            layoutNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            layoutNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);

            rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTransactions.setAdapter(new TransactionAdapter(transactions));
        }
    }

    private List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        // TODO: Replace with real data loading logic
        /*
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 970.00));
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 570.00));
        */
        return list;
    }
}

package com.example.canpay_operator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;
import com.example.canpay_operator.utils.TransactionStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HomeAssignedFragment extends Fragment {
    private static final Logger logger = LoggerFactory.getLogger(HomeAssignedFragment.class);
    private TextView tvName, tvBusNumber, tvEarnings;
    private RecyclerView rvTransactions;
    private LinearLayout layoutNoTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactions;
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

        transactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(transactionAdapter);

        // Load transactions from TransactionStore
        List<Transaction> transactions = TransactionStore.getTransactions(requireContext());
        // Replace your current transaction list with this one
        // For example, if you use a RecyclerView adapter:
        transactionAdapter.setTransactions(transactions);
        transactionAdapter.notifyDataSetChanged();

        fetchAndDisplayEarnings();
    }


    private void fetchAndDisplayEarnings() {
        String name = PreferenceManager.getUserName(requireContext());
        String busID = PreferenceManager.getBusID(requireContext());
        String operatorId = PreferenceManager.getUserId(requireContext());
        String token = PreferenceManager.getToken(requireContext());
        String busNumber = PreferenceManager.getBusNumber(requireContext());

        tvName.setText(name);
        tvBusNumber.setText(busNumber);
        transactions.addAll(loadTransactions());
        if (operatorId == null || busID == null || token == null) {
            tvEarnings.setText("LKR 0.00");
            return;
        }

        if (transactions.isEmpty()) {
            layoutNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            layoutNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }

        if (operatorId == null || busID == null || token == null) {
            tvEarnings.setText("LKR 0.00");
            return;
        }

        String endpoint = Endpoints.GET_OPERATOR_FINANCE_DETAILS + "?busId=" + busID + "&operatorId=" + operatorId;

        com.example.canpay_operator.utils.ApiHelper.getJson(
                requireContext(),
                endpoint,
                token,
                new com.example.canpay_operator.utils.ApiHelper.Callback() {
                    @Override
                    public void onSuccess(org.json.JSONObject response) {
                        if (response.optBoolean("success")) {
                            org.json.JSONObject data = response.optJSONObject("data");
                            if (data != null) {
                                double earnings = data.optDouble("earningsAmount", 0.0);
                                tvEarnings.setText(String.format("LKR %,.2f", earnings));
                            }
                        }
                    }

                    @Override
                    public void onError(com.android.volley.VolleyError error) {
                        tvEarnings.setText("LKR 0.00");
                        com.example.canpay_operator.utils.ApiHelper.handleVolleyError(requireContext(), error, "EARNINGS");
                    }
                }
        );
    }

    private List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        // TODO: Replace with real data loading logic (e.g., from API or database)
        /*
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 970.00));
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 570.00));
        */
        return list;
    }

    public void addTransaction(Transaction transaction) {
        if (transactions != null && transactionAdapter != null) {
            transactions.add(0, transaction); // Add to top of list
            transactionAdapter.notifyItemInserted(0);
            rvTransactions.scrollToPosition(0);
            layoutNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);

            // Update earnings
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
            float currentEarnings = prefs.getFloat("earnings", 7950.00f);
            float newEarnings = (float) (currentEarnings + transaction.getAmount());
            tvEarnings.setText(String.format("LKR %,.2f", newEarnings));
            prefs.edit().putFloat("earnings", newEarnings).apply();

            logger.info("Added transaction: " + transaction.getDescription() + ", " + transaction.getAmount());
        }
    }
}

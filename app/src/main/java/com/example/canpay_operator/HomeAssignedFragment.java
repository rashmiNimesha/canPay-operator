package com.example.canpay_operator;

import android.os.Bundle;
import android.os.Handler;
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

import com.android.volley.VolleyError;
import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;
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

    private Handler handler = new Handler();
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 2000; // 2 seconds

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

        // Connected message logic
        TextView tvConnectionMessage = view.findViewById(R.id.tv_connection_message);
        String busNumber = PreferenceManager.getBusNumber(requireContext());
        if (busNumber != null && !busNumber.isEmpty()) {
            tvConnectionMessage.setVisibility(View.VISIBLE);
            tvConnectionMessage.setText("Connected to vehicle " + busNumber);
        } else {
            tvConnectionMessage.setVisibility(View.GONE);
        }

        transactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(transactionAdapter);

        // Initial data fetch
        fetchTransactions();
        fetchAndDisplayEarnings();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start auto-refresh
        startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop auto-refresh to avoid leaks when fragment not visible
        stopAutoRefresh();
    }

    private void startAutoRefresh() {
        if (refreshRunnable == null) {
            refreshRunnable = new Runnable() {
                @Override
                public void run() {
                    fetchTransactions();
                    fetchAndDisplayEarnings();
                    handler.postDelayed(this, REFRESH_INTERVAL);
                }
            };
        }
        handler.post(refreshRunnable);
    }

    private void stopAutoRefresh() {
        if (refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    private void fetchTransactions() {
        String busId = PreferenceManager.getBusID(getContext());
        String operatorId = PreferenceManager.getUserId(getContext());
        String token = PreferenceManager.getToken(getContext());

        if (busId == null || operatorId == null || token == null) {
            layoutNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
            return;
        }

        String endpoint = String.format(Endpoints.GET_RECENT_TRANSACTIONS, busId, operatorId);

        ApiHelper.getJson(getContext(), endpoint, token, new ApiHelper.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray dataArray = response.getJSONArray("data");
                    List<Transaction> fetchedTransactions = new ArrayList<>();

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject tx = dataArray.getJSONObject(i);
                        fetchedTransactions.add(new Transaction(
                                tx.getString("happenedAt"),
                                tx.getString("note"),
                                tx.getDouble("amount")
                        ));
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            transactions.clear();
                            transactions.addAll(fetchedTransactions);
                            transactionAdapter.notifyDataSetChanged();

                            if (transactions.isEmpty()) {
                                layoutNoTransactions.setVisibility(View.VISIBLE);
                                rvTransactions.setVisibility(View.GONE);
                            } else {
                                layoutNoTransactions.setVisibility(View.GONE);
                                rvTransactions.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error("Error parsing transactions: " + e.getMessage(), e);
                }
            }

            @Override
            public void onError(VolleyError error) {
                logger.error("Error fetching transactions: " + error.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        layoutNoTransactions.setVisibility(View.VISIBLE);
                        rvTransactions.setVisibility(View.GONE);
                    });
                }
            }
        });
    }

    private void fetchAndDisplayEarnings() {
        String name = PreferenceManager.getUserName(requireContext());
        String busID = PreferenceManager.getBusID(requireContext());
        String operatorId = PreferenceManager.getUserId(requireContext());
        String token = PreferenceManager.getToken(requireContext());
        String busNumber = PreferenceManager.getBusNumber(requireContext());

        tvName.setText(name);
        tvBusNumber.setText(busNumber);

        if (operatorId == null || busID == null || token == null) {
            tvEarnings.setText("LKR 0.00");
            return;
        }

        String endpoint = Endpoints.GET_OPERATOR_FINANCE_DETAILS + "?busId=" + busID + "&operatorId=" + operatorId;

        ApiHelper.getJson(
                requireContext(),
                endpoint,
                token,
                new ApiHelper.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        if (response.optBoolean("success")) {
                            JSONObject data = response.optJSONObject("data");
                            if (data != null) {
                                double earnings = data.optDouble("earningsAmount", 0.0);
                                tvEarnings.setText(String.format("LKR %,.2f", earnings));
                            }
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        tvEarnings.setText("LKR 0.00");
                        ApiHelper.handleVolleyError(requireContext(), error, "EARNINGS");
                    }
                }
        );
    }

    //  TO ALLOW EXTERNAL REFRESH CALLS --->
    public void refreshTransactions() {
        fetchTransactions();
        fetchAndDisplayEarnings();
    }
}

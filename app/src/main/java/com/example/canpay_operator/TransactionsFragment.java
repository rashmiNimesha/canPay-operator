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

import com.android.volley.VolleyError;
import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private RecyclerView rvTransactions;
    private LinearLayout layoutNoTransactions;
    private TransactionAdapter transactionAdapter;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTransactions = view.findViewById(R.id.rv_transactions);
        layoutNoTransactions = view.findViewById(R.id.layout_no_transactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Call new fetchTransactions() method for API integration
        fetchTransactions();
    }

    private void fetchTransactions() {
        // Hardcoded busId and operatorId; adjust as needed.
        String busId = PreferenceManager.getBusID(getContext());
        String operatorId = PreferenceManager.getUserId(getContext());
        String endpoint = String.format(Endpoints.GET_RECENT_TRANSACTIONS, busId, operatorId);
        String token = PreferenceManager.getToken(getContext());

        ApiHelper.getJson(getContext(), endpoint, token, new ApiHelper.Callback()  {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray dataArray = response.getJSONArray("data");
                    final List<Transaction> transactions = new ArrayList<>();
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject tx = dataArray.getJSONObject(i);
                        // Using 'happenedAt' for date and 'note' as description.
                        transactions.add(new Transaction(
                                tx.getString("happenedAt"),
                                tx.getString("note"),
                                tx.getDouble("amount")
                        ));
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (transactions.isEmpty()) {
                                    layoutNoTransactions.setVisibility(View.VISIBLE);
                                    rvTransactions.setVisibility(View.GONE);
                                } else {
                                    layoutNoTransactions.setVisibility(View.GONE);
                                    rvTransactions.setVisibility(View.VISIBLE);
                                    if (transactionAdapter == null) {
                                        transactionAdapter = new TransactionAdapter(transactions);
                                        rvTransactions.setAdapter(transactionAdapter);
                                    } else {
                                        transactionAdapter.setTransactions(transactions);
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }
}

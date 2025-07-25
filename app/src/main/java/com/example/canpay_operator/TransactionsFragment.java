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

public class TransactionsFragment extends Fragment {

    private RecyclerView rvTransactions;
    private LinearLayout layoutNoTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;

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

        // Load transactions (replace with real backend/API call)
        transactionList = loadTransactions();

        if (transactionList.isEmpty()) {
            layoutNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            layoutNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);

            rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            transactionAdapter = new TransactionAdapter(transactionList);
            rvTransactions.setAdapter(transactionAdapter);
        }
    }

    // Dummy data loader - replace with your real data source
    private List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        // Uncomment below to test with sample data:
        /*
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 970.00));
        list.add(new Transaction("Nov 18, 2024", "Payment Received", 570.00));
        */
        return list;
    }
}

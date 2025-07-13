//package com.example.canpay_operator;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.fragment.app.Fragment;
//import com.example.canpay_operator.R;
//
//public class SettingsFragment extends Fragment {
//
//    private TextView tvAssignedBus, tvName, tvPhone, tvNIC;
//    private LinearLayout layoutChangePin, layoutLogout;
//
//    public SettingsFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_settings, container, false);
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        tvAssignedBus = view.findViewById(R.id.tvAssignedBus);
//        tvName = view.findViewById(R.id.tvName);
//        tvPhone = view.findViewById(R.id.tvPhone);
//        tvNIC = view.findViewById(R.id.tvNIC);
//        layoutChangePin = view.findViewById(R.id.layoutChangePin);
//        layoutLogout = view.findViewById(R.id.layoutLogout);
//
//        // Fetch data (replace with real data source)
//        tvAssignedBus.setText("BBX-2231");
//        tvName.setText("Gamage");
//        tvPhone.setText("+94 71 12 12 123");
//        tvNIC.setText("2000XXXXXXXXX");
//
//        layoutChangePin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: Implement Change PIN dialog or navigation
//                Toast.makeText(getActivity(), "Change PIN clicked", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        layoutLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO: Implement logout logic (clear session, navigate to login)
//                Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
//                // Example:
//                // Intent intent = new Intent(getActivity(), LoginActivity.class);
//                // startActivity(intent);
//                // getActivity().finish();
//            }
//        });
//    }
//}

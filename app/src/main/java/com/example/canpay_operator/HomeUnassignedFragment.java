package com.example.canpay_operator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.VolleyError;
import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONObject;

public class HomeUnassignedFragment extends Fragment {

    private ImageView imgQr;
    private TextView tvEmail, tvTitle, tvInstructions;

    public HomeUnassignedFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_unassigned, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgQr = view.findViewById(R.id.img_qr);
        tvEmail = view.findViewById(R.id.tv_phone); // Reusing tv_phone as per your XML
        tvTitle = view.findViewById(R.id.tv_title);
        tvInstructions = view.findViewById(R.id.tv_instructions);

        generateQrWithUserData();
        checkAssignmentStatus();
    }

    private void generateQrWithUserData() {
        String userId = PreferenceManager.getUserId(requireContext());
        String email = PreferenceManager.getEmail(requireContext());

        // Show email or fallback text
        tvEmail.setText(email != null && !email.isEmpty() ? email : "Email not available");

        if (userId != null && !userId.isEmpty()) {
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap qrBitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 400, 400);
                imgQr.setImageBitmap(qrBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imgQr.setImageResource(R.drawable.ic_no_transactions);
                tvInstructions.setText("Failed to generate QR code.");
            }
        } else {
            imgQr.setImageResource(R.drawable.ic_no_transactions);
            tvInstructions.setText("User ID is not available.");
        }
    }

    // New method to check assignment status asynchronously
    private void checkAssignmentStatus() {
        String operatorId = PreferenceManager.getUserId(requireContext());
        String token = PreferenceManager.getToken(requireContext());

        if (operatorId == null || operatorId.isEmpty()) {
            return;
        }

        String endpoint = Endpoints.GET_OPERATOR_ASSIGNMENT + operatorId;

        ApiHelper.getJson(requireContext(), endpoint, token, new ApiHelper.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                JSONObject data = response.optJSONObject("data");
                boolean assigned = data != null && data.optBoolean("assigned", false);
                if (assigned) {
                    // If assigned, start HomeActivity and finish current activity if needed
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
                // else remain in HomeUnassignedFragment, no action needed
            }

            @Override
            public void onError(VolleyError error) {
                // On error, do not change the current fragment
            }
        });
    }
}

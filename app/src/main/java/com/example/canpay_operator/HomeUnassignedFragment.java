package com.example.canpay_operator;

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

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.example.canpay_operator.utils.PreferenceManager;

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
}

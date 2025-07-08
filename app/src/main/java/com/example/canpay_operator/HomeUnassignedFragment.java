package com.example.canpay_operator;

import android.content.SharedPreferences;
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

public class HomeUnassignedFragment extends Fragment {

    private ImageView imgQr;
    private TextView tvPhone, tvTitle, tvInstructions;

    private static final String PREFS_NAME = "CanpayPrefs";

    public HomeUnassignedFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_unassigned, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgQr = view.findViewById(R.id.img_qr);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvTitle = view.findViewById(R.id.tv_title);
        tvInstructions = view.findViewById(R.id.tv_instructions);

        loadUserPhoneAndGenerateQr();
    }

    private void loadUserPhoneAndGenerateQr() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        String phone = prefs.getString("phone", "071 23 45 678");
        tvPhone.setText(phone);

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(phone, BarcodeFormat.QR_CODE, 400, 400);
            imgQr.setImageBitmap(bitmap);
        } catch (Exception e) {
            // Fallback image or error handling
            imgQr.setImageResource(R.drawable.ic_no_transactions);
        }
    }
}

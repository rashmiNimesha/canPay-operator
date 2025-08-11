package com.example.canpay_operator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONObject;

public class AssignedBusQRActivity extends AppCompatActivity {

    private static final String TAG = "AssignedBusQRActivity";
    private static final String PREFS_NAME = "CanpayPrefs";
    private static final String KEY_OPERATOR_ID = "operator_id";
    private static final String KEY_BUS_NUMBER = "bus_number";
    private static final String KEY_BUS_ROUTE = "bus_route";

    private ImageView qrCodeImageView;
    private TextView tvBusNumberDisplay;
    private TextView tvBusRouteDisplay;
    private ImageButton btnBack;
    private ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_bus_qr);

        // Initialize views
        qrCodeImageView = findViewById(R.id.img_qr);
        tvBusNumberDisplay = findViewById(R.id.tv_bus_number_display);
        tvBusRouteDisplay = findViewById(R.id.tv_bus_route_display);
        btnBack = findViewById(R.id.btn_back);
        btnLogout = findViewById(R.id.btn_logout);

        // Setup button listeners
        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> showUnassignConfirmationDialog());

        generateAndDisplayQrCode();
    }

    private void generateAndDisplayQrCode() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String operatorId = sharedPreferences.getString(KEY_OPERATOR_ID, null);
        String busNumber = sharedPreferences.getString(KEY_BUS_NUMBER, "N/A");
        String busRoute = sharedPreferences.getString(KEY_BUS_ROUTE, "N/A");

        if (operatorId == null || operatorId.isEmpty()) {
            Log.e(TAG, "Operator ID not found in SharedPreferences.");
         //   Toast.makeText(this, "Operator ID not available. Cannot generate QR.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(operatorId, BarcodeFormat.QR_CODE, 500, 500);
            qrCodeImageView.setImageBitmap(bitmap);
            tvBusNumberDisplay.setText(busNumber);
            tvBusRouteDisplay.setText(busRoute);
        } catch (Exception e) {
            Log.e(TAG, "Error generating QR Code: " + e.getMessage());
            Toast.makeText(this, "Error generating QR Code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnassignConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_unassign_confirm, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        Button btnUnassign = dialogView.findViewById(R.id.btn_unassign);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnUnassign.setOnClickListener(v -> {
            dialog.dismiss();
            // --- API Integration Start ---
            String operatorId = PreferenceManager.getUserId(this);
            String busId = PreferenceManager.getBusID(this);
            String token = PreferenceManager.getToken(this);

            if (operatorId == null || busId == null || token == null) {
                Toast.makeText(this, "Missing operator or bus info.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject body = new JSONObject();
                body.put("operatorId", operatorId);
                body.put("busId", busId);

                // Use POST instead of DELETE
                ApiHelper.postJson(this, Endpoints.UNASSIGNED_BUS, body, token, new ApiHelper.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Clear bus info from preferences
                        PreferenceManager.setBusNumber(AssignedBusQRActivity.this, null);
                        PreferenceManager.setBusID(AssignedBusQRActivity.this, null);

                        Toast.makeText(AssignedBusQRActivity.this, "You have been unassigned from the bus.", Toast.LENGTH_SHORT).show();

                        // Navigate to HomeUnassignedFragment or appropriate screen
                        Intent intent = new Intent(AssignedBusQRActivity.this, HomeActivity.class);
                        intent.putExtra("showUnassigned", true);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(com.android.volley.VolleyError error) {
                        ApiHelper.handleVolleyError(AssignedBusQRActivity.this, error, TAG);
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Error preparing unassign request.", Toast.LENGTH_SHORT).show();
            }
            // --- API Integration End ---
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog centered and well sized
        dialog.show();

        if (dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.85);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

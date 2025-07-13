package com.example.canpay_operator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;
import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class TakePhotoActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1001;
    private static final int CAMERA_PERMISSION_CODE = 2001;
    private static final String TAG = "TakePhotoActivity";

    private CircleImageView imgPhoto;
    private ImageView imgCameraIcon;
    private Button btnTakePhoto, btnNext, btnRetake;
    private Bitmap capturedPhoto = null;

    private String email;
    private String authToken;
    private String name;
    private String nic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        imgPhoto = findViewById(R.id.img_photo);
        imgCameraIcon = findViewById(R.id.img_camera_icon);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnNext = findViewById(R.id.btn_next);
        btnRetake = findViewById(R.id.btn_retake);
        ImageButton btnBack = findViewById(R.id.btn_back);

        // Get authToken from PreferenceManager (fixed)
        authToken = PreferenceManager.getToken(this);

        // Retrieve other user info from Intent
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        nic = getIntent().getStringExtra("nic");

        if (email == null || authToken == null || name == null || nic == null) {
            Toast.makeText(this, "Missing user data or session expired. Please restart the registration.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TakePhotoActivity.this, NICEntryActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("name", name);
            intent.putExtra("nic", nic);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnTakePhoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        btnRetake.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (capturedPhoto == null) {
                Toast.makeText(this, "Please take a photo first.", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadPhotoAndCompleteProfile();
        });

        setPhotoUIState(false);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null) {
                capturedPhoto = photo;
                imgPhoto.setImageBitmap(photo);
                setPhotoUIState(true);
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setPhotoUIState(boolean photoTaken) {
        if (photoTaken) {
            imgCameraIcon.setVisibility(View.GONE);
            btnTakePhoto.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
            btnRetake.setVisibility(View.VISIBLE);
            btnNext.setEnabled(true);
            btnRetake.setEnabled(true);
        } else {
            imgPhoto.setImageResource(R.drawable.ic_camera_placeholder); // Your placeholder drawable
            imgCameraIcon.setVisibility(View.VISIBLE);
            btnTakePhoto.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
            btnRetake.setVisibility(View.GONE);
            btnNext.setEnabled(false);
            btnRetake.setEnabled(false);
        }
    }

    private void uploadPhotoAndCompleteProfile() {
        btnNext.setEnabled(false);

        // Convert Bitmap to Base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedPhoto.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("name", name);
            jsonBody.put("nic", nic);
            jsonBody.put("profileImage", encodedImage); // Backend expects Base64 string
        } catch (Exception e) {
            Log.e(TAG, "JSON creation error", e);
            Toast.makeText(this, "Error preparing data", Toast.LENGTH_SHORT).show();
            btnNext.setEnabled(true);
            return;
        }

        // Pass authToken properly as a string (no JSON stringify of headers)
        ApiHelper.postJson(this, Endpoints.SAVE_USER_PROFILE, jsonBody, authToken, new ApiHelper.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                btnNext.setEnabled(true);
                Toast.makeText(TakePhotoActivity.this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(TakePhotoActivity.this, PinCodeActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("token", authToken);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(VolleyError error) {
                btnNext.setEnabled(true);
                ApiHelper.handleVolleyError(TakePhotoActivity.this, error, TAG);
            }
        });
    }
}
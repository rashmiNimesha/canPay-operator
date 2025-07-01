package com.example.canpay_operator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

public class TakePhotoActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1001;
    private static final int CAMERA_PERMISSION_CODE = 2001;

    private CircleImageView imgPhoto;
    private ImageView imgCameraIcon;
    private Button btnTakePhoto, btnNext, btnRetake;
    private Bitmap capturedPhoto = null;

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

        // Back button: Go to NICEntryActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TakePhotoActivity.this, NICEntryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Take Photo button
        btnTakePhoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        // Retake Photo button
        btnRetake.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            if (capturedPhoto == null) {
                Toast.makeText(this, "Please take a photo first.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Proceed to PincodeActivity
            Intent intent = new Intent(TakePhotoActivity.this, PinCodeActivity.class);
            // Optionally, you can pass the photo as a byte array or save it to file and pass the URI
            startActivity(intent);
            finish();
        });

        // Initial UI state
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

    /**
     * Update UI depending on whether a photo has been taken.
     */
    private void setPhotoUIState(boolean photoTaken) {
        if (photoTaken) {
            imgCameraIcon.setVisibility(View.GONE);
            btnTakePhoto.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
            btnRetake.setVisibility(View.VISIBLE);
            btnNext.setEnabled(true);
            btnRetake.setEnabled(true);
        } else {
            imgPhoto.setImageResource(R.drawable.ic_camera_placeholder); // Placeholder icon
            imgCameraIcon.setVisibility(View.VISIBLE);
            btnTakePhoto.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
            btnRetake.setVisibility(View.GONE);
            btnNext.setEnabled(false);
            btnRetake.setEnabled(false);
        }
    }
}

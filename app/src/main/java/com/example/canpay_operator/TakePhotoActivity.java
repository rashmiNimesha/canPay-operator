package com.example.canpay_operator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import androidx.core.content.FileProvider;

import com.android.volley.VolleyError;
import com.example.canpay_operator.utils.ApiHelper;
import com.example.canpay_operator.utils.Endpoints;
import com.example.canpay_operator.utils.PreferenceManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class TakePhotoActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1001;
    private static final int CAMERA_PERMISSION_CODE = 2001;
    private static final String TAG = "TakePhotoActivity";

    private CircleImageView imgPhoto;
    private ImageView imgCameraIcon;
    private Button btnTakePhoto, btnNext, btnRetake;

    private Uri photoUri;
    private File photoFile;

    private String email, authToken, name, nic;

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

        authToken = PreferenceManager.getToken(this);
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

        btnTakePhoto.setOnClickListener(v -> handleCameraLaunch());
        btnRetake.setOnClickListener(v -> handleCameraLaunch());

        btnNext.setOnClickListener(v -> {
            if (photoFile == null || !photoFile.exists()) {
                Toast.makeText(this, "Please take a photo first.", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadPhotoAndCompleteProfile();
        });

        setPhotoUIState(false);
    }

    private void handleCameraLaunch() {
        if (checkCameraPermission()) {
            openCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            photoFile
                    );
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, CAMERA_REQUEST);
                } else {
                    Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app found on device", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(null); // Internal app directory
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && photoFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imgPhoto.setImageBitmap(bitmap);
            setPhotoUIState(true);
        }
    }

    private void setPhotoUIState(boolean photoTaken) {
        imgCameraIcon.setVisibility(photoTaken ? View.GONE : View.VISIBLE);
        btnTakePhoto.setVisibility(photoTaken ? View.GONE : View.VISIBLE);
        btnNext.setVisibility(photoTaken ? View.VISIBLE : View.GONE);
        btnRetake.setVisibility(photoTaken ? View.VISIBLE : View.GONE);
        btnNext.setEnabled(photoTaken);
        btnRetake.setEnabled(photoTaken);
    }

    private void uploadPhotoAndCompleteProfile() {
        btnNext.setEnabled(false);

        try {
            FileInputStream fis = new FileInputStream(photoFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            fis.close();

            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("name", name);
            jsonBody.put("nic", nic);
            jsonBody.put("profileImage", "data:image/jpeg;base64," + base64Image); // ✅ Correct format
            jsonBody.put("role", "OPERATOR");

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

        } catch (Exception e) {
            Log.e(TAG, "Image conversion error", e);
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            btnNext.setEnabled(true);
        }
    }
}

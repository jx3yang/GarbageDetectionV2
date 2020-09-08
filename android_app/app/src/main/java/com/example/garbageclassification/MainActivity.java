package com.example.garbageclassification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garbageclassification.model.IModel;
import com.example.garbageclassification.model.ModelFactory;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button imgCaptureBtn;
    ImageView imgView;

    TextView predictionText;

    Uri imgUri;

    IModel classificationModel;

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int OPEN_CAMERA_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        try {
            loadModel();
        }
        catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void captureImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int[] permissionCodes = {
                    checkSelfPermission(Manifest.permission.CAMERA),
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            };

            if (!allPermissionsGranted(permissionCodes))
                askPermissions();
            else
                openCamera();
        }
        else
            openCamera();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void askPermissions() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (allPermissionsGranted(grantResults)) {
                openCamera();
            }
            else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_CAMERA_CODE && resultCode == RESULT_OK) {
            this.imgView.setImageURI(imgUri);
            Bitmap bitmap = ((BitmapDrawable)this.imgView.getDrawable()).getBitmap();
            this.predictionText.setText(this.classificationModel.predict(bitmap));
        }
    }

    private boolean allPermissionsGranted(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From classifier application");
        this.imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.imgUri);
        startActivityForResult(cameraIntent, OPEN_CAMERA_CODE);
    }

    private void bindViews() {
        this.imgView = findViewById(R.id.image_view);
        this.imgCaptureBtn = findViewById(R.id.capture_image_btn);
        this.predictionText = findViewById(R.id.prediction_text);
    }

    private void loadModel() throws IOException {
        this.classificationModel = ModelFactory.createModel(this);
    }
}
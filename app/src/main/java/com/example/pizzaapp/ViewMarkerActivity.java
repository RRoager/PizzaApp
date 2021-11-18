package com.example.pizzaapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pizzaapp.Repositories.Repository;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ViewMarkerActivity extends AppCompatActivity implements Updatable {

    private EditText editName;
    private EditText editContent;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_marker);
        editName = findViewById(R.id.editText);
        editName.setText(Repository.getCurrentMarker().getName());
        editContent = findViewById(R.id.editContent);
        editContent.setText(Repository.getCurrentMarker().getContent());
        imageView = findViewById(R.id.imageView);
        Repository.downloadBitmapForCurrentNote(this);
        setupGalleryLauncher();
        setupCameraLauncher();
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    // Så der ikke kommer nullpointer ved tilbage tryk fra gallery uden billede
                    if(result.getData() != null) {
                        try {
                            InputStream is = getContentResolver().openInputStream(result.getData().getData());
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            imageView.setImageBitmap(bitmap);
                            Repository.getCurrentMarker().setBitmap(bitmap);
                            Repository.getCurrentMarker().setHasNewImage(true);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                        Repository.getCurrentMarker().setBitmap(bitmap);
                        Repository.getCurrentMarker().setHasNewImage(true);
                    }
                });
    }

    public void photoChoice(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("How would you like to add an image?")
                .setCancelable(true)
                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(intent);
                    }
                })
                .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(ActivityCompat.checkSelfPermission(ViewMarkerActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(ViewMarkerActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                        }else {
                            launchCamera();
                        }
                    }
                }).show();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    public void saveButtonPressed(View view) {
        Repository.updateMarker(editName.getText().toString(), editContent.getText().toString());
        Toast.makeText(this, "Note has been saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void update(Object o) {
        //Bitmap er blevet downloaded og gemt i currentNote hos Repo
        imageView.setImageBitmap(Repository.getCurrentMarker().getBitmap());
    }

    public void deleteButtonPressed(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you would like to delete the note?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Repository.deleteMarker();
                        Intent intent=new Intent(ViewMarkerActivity.this, MapsActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            }
        }
    }
}
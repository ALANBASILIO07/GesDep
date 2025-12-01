package com.uaemex.gesdep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uaemex.gesdep.models.VenueModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.UUID;

public class CreateVenueActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private MaterialButton btnSelectLocation, btnSaveVenue;
    private TextView tvSelectedAddress;
    private MaterialCardView btnSelectImage;
    private ImageView ivVenueImage;
    private LinearLayout layoutImagePlaceholder;
    private ProgressBar progressBar;

    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private String selectedAddressText = "";
    private Uri selectedImageUri = null;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Launcher MAPA
    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedLat = data.getDoubleExtra("latitude", 0.0);
                    selectedLng = data.getDoubleExtra("longitude", 0.0);
                    selectedAddressText = data.getStringExtra("address");

                    tvSelectedAddress.setText(selectedAddressText);
                    tvSelectedAddress.setTextColor(getColor(R.color.text_primary_color));
                }
            }
    );

    // Launcher GALERÍA
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).centerCrop().into(ivVenueImage);
                    layoutImagePlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_venue);

        WindowUtils.setGreenStatusBar(this);
        db = FirebaseFirestore.getInstance("gesdep");
        storage = FirebaseStorage.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivVenueImage = findViewById(R.id.ivVenueImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        btnSaveVenue = findViewById(R.id.btnSaveVenue);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapLauncher.launch(intent);
        });

        btnSelectImage.setOnClickListener(v -> checkPermissionAndOpenGallery());

        btnSaveVenue.setOnClickListener(v -> validateAndSave());
    }

    private void checkPermissionAndOpenGallery() {
        if (android.os.Build.VERSION.SDK_INT >= 33 ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    private void validateAndSave() {
        String name = etName.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Nombre requerido");
            return;
        }
        if (selectedLat == 0.0 || selectedLng == 0.0) {
            Toast.makeText(this, "Debes seleccionar una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveVenue.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndSave(name);
        } else {
            saveVenueToFirestore(name, null);
        }
    }

    private void uploadImageAndSave(String name) {
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("venues/" + filename);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveVenueToFirestore(name, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveVenueToFirestore(name, null); // Guardar sin foto
                });
    }

    private void saveVenueToFirestore(String name, String imageUrl) {
        VenueModel venue = new VenueModel();
        venue.setName(name);
        venue.setAddress(selectedAddressText);
        venue.setLatitude(selectedLat);
        venue.setLongitude(selectedLng);
        venue.setImageUrl(imageUrl);

        db.collection("venues").add(venue)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Sede registrada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveVenue.setEnabled(true);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
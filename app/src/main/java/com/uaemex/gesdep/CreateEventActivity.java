// CreateEventActivity.java  ←  VERSIÓN SIMPLIFICADA Y 100% FUNCIONAL
package com.uaemex.gesdep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.Timestamp;
import com.uaemex.gesdep.models.EventModel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {

    private EditText etName, etDescription, etPlace;
    private Button btnAddPhotos, btnCreate;
    private TextView tvPhotoCount;
    private ProgressBar progressBar;

    private final List<Uri> selectedPhotos = new ArrayList<>();

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openGallery();
                else Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedPhotos.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedPhotos.add(result.getData().getData());
                    }
                    tvPhotoCount.setText(selectedPhotos.size() + " foto(s) seleccionada(s)");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPlace = findViewById(R.id.etPlace);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        btnCreate = findViewById(R.id.btnCreate);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        progressBar = findViewById(R.id.progressBar);

        btnAddPhotos.setOnClickListener(v -> checkPermissionAndOpenGallery());
        btnCreate.setOnClickListener(v -> createEvent());
    }

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT >= 33) {
            openGallery();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(Intent.createChooser(intent, "Selecciona fotos"));
    }

    private void createEvent() {
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String place = etPlace.getText().toString().trim();

        if (name.isEmpty() || desc.isEmpty() || place.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreate.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        if (selectedPhotos.isEmpty()) {
            saveEvent(new ArrayList<>());
        } else {
            uploadPhotos();
        }
    }

    private void uploadPhotos() {
        List<String> urls = new ArrayList<>();
        // Asegúrate de que la ruta sea consistente
        StorageReference eventRef = storage.getReference().child("events/" + UUID.randomUUID().toString());

        final int totalPhotos = selectedPhotos.size(); // Guardamos el total
        final int[] uploadedCount = {0}; // Contador atómico simple

        for (Uri uri : selectedPhotos) { // <--- Aquí está la primera "uri" (archivo local)
            StorageReference fileRef = eventRef.child(UUID.randomUUID() + ".jpg");

            fileRef.putFile(uri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnSuccessListener(downloadUri -> { // <--- CAMBIO AQUÍ: Usamos "downloadUri" en lugar de "uri"

                urls.add(downloadUri.toString());
                uploadedCount[0]++;

                // Verificamos si ya subieron todas
                if (uploadedCount[0] == totalPhotos) {
                    saveEvent(urls);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Opcional: Decidir si detener todo o continuar
                btnCreate.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            });
        }
    }

    private void saveEvent(List<String> photoUrls) {
        EventModel event = new EventModel();
        event.name = etName.getText().toString().trim();
        event.description = etDescription.getText().toString().trim();
        event.placeName = etPlace.getText().toString().trim();
        event.type = "deportivo";
        event.category = "futbol";
        event.latitude = 19.2836;
        event.longitude = -99.6578;
        event.distanceFromCenterMinutes = 15;
        event.eventDateTime = Timestamp.now();
        event.registrationDeadline = Timestamp.now();
        event.durationMinutes = 120;
        event.registrationType = "individual";
        event.minParticipants = 2;
        event.maxParticipants = 50;
        event.organizerId = auth.getCurrentUser().getUid();
        event.organizerName = auth.getCurrentUser().getDisplayName() != null ? auth.getCurrentUser().getDisplayName() : "Anónimo";
        event.organizerEmail = auth.getCurrentUser().getEmail();
        event.photoUrls = photoUrls;
        if (!photoUrls.isEmpty()) event.thumbnailUrl = photoUrls.get(0);

        db.collection("events").add(event)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "¡Evento creado con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnCreate.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
    }
}
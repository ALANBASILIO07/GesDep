package com.uaemex.gesdep;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etTitle, etMacroEvent, etDescription, etPlace, etMinQuota, etMaxQuota;
    private TextInputEditText etDate, etTime;
    private AutoCompleteTextView dropdownCategory, dropdownDiscipline, dropdownModality;
    private MaterialButton btnSelectLocation, btnPublishEvent;
    private MaterialCardView btnSelectImage; // Ahora es la tarjeta completa
    private ImageView ivEventImage;
    private LinearLayout layoutImagePlaceholder;
    private View loadingOverlay;

    // Variables de Datos
    private Calendar eventCalendar = Calendar.getInstance();
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private Uri selectedImageUri = null;

    // Firebase
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // --- LAUNCHERS ---

    // 1. Mapa
    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedLat = data.getDoubleExtra("latitude", 0.0);
                    selectedLng = data.getDoubleExtra("longitude", 0.0);
                    String address = data.getStringExtra("address");

                    etPlace.setText(address); // Llenar campo automáticamente
                    Toast.makeText(this, "Ubicación guardada", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // 2. Galería
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // Mostrar imagen
                    Glide.with(this).load(selectedImageUri).centerCrop().into(ivEventImage);
                    layoutImagePlaceholder.setVisibility(View.GONE); // Ocultar icono cámara
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Estilo
        WindowUtils.setGreenStatusBar(this);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");
        storage = FirebaseStorage.getInstance();

        initViews();
        setupDropdowns();
        setupListeners();
    }

    private void initViews() {
        // TextFields
        etTitle = findViewById(R.id.etTitle);
        etMacroEvent = findViewById(R.id.etMacroEvent);
        etDescription = findViewById(R.id.etDescription);
        etPlace = findViewById(R.id.etPlace);
        etMinQuota = findViewById(R.id.etMinQuota);
        etMaxQuota = findViewById(R.id.etMaxQuota);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);

        // Dropdowns
        dropdownCategory = findViewById(R.id.dropdownCategory);
        dropdownDiscipline = findViewById(R.id.dropdownDiscipline);
        dropdownModality = findViewById(R.id.dropdownModality);

        // Botones y Multimedia
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnSelectImage = findViewById(R.id.btnSelectImage); // CardView clickeable
        ivEventImage = findViewById(R.id.ivEventImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        btnPublishEvent = findViewById(R.id.btnPublishEvent);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Toolbar Back
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // Llenar los menús desplegables con los arrays de strings.xml
        ArrayAdapter<CharSequence> adapterCat = ArrayAdapter.createFromResource(this, R.array.event_categories, android.R.layout.simple_dropdown_item_1line);
        dropdownCategory.setAdapter(adapterCat);

        ArrayAdapter<CharSequence> adapterDisc = ArrayAdapter.createFromResource(this, R.array.event_disciplines, android.R.layout.simple_dropdown_item_1line);
        dropdownDiscipline.setAdapter(adapterDisc);

        ArrayAdapter<CharSequence> adapterMod = ArrayAdapter.createFromResource(this, R.array.event_modalities, android.R.layout.simple_dropdown_item_1line);
        dropdownModality.setAdapter(adapterMod);
    }

    private void setupListeners() {
        // Fecha
        etDate.setOnClickListener(v -> showDatePicker());
        // Hora
        etTime.setOnClickListener(v -> showTimePicker());

        // Mapa
        btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            mapLauncher.launch(intent);
        });

        // Imagen (Permisos + Galería)
        btnSelectImage.setOnClickListener(v -> checkPermissionAndOpenGallery());

        // Publicar
        btnPublishEvent.setOnClickListener(v -> validateAndCreateEvent());
    }

    // --- PICKERS ---
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            eventCalendar.set(Calendar.YEAR, year);
            eventCalendar.set(Calendar.MONTH, month);
            eventCalendar.set(Calendar.DAY_OF_MONTH, day);
            etDate.setText(day + "/" + (month + 1) + "/" + year);
        }, eventCalendar.get(Calendar.YEAR), eventCalendar.get(Calendar.MONTH), eventCalendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hour, minute) -> {
            eventCalendar.set(Calendar.HOUR_OF_DAY, hour);
            eventCalendar.set(Calendar.MINUTE, minute);
            etTime.setText(String.format("%02d:%02d", hour, minute));
        }, eventCalendar.get(Calendar.HOUR_OF_DAY), eventCalendar.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    // --- IMAGEN ---
    private void checkPermissionAndOpenGallery() {
        // Android 13+ no necesita permiso de lectura normal, usa PhotoPicker, pero para compatibilidad:
        if (android.os.Build.VERSION.SDK_INT >= 33 ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // --- LÓGICA DE GUARDADO ---
    private void validateAndCreateEvent() {
        String title = etTitle.getText().toString().trim();
        String category = dropdownCategory.getText().toString();
        String minQ = etMinQuota.getText().toString();
        String maxQ = etMaxQuota.getText().toString();

        if (title.isEmpty() || category.isEmpty() || etDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);
        btnPublishEvent.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImage(title);
        } else {
            saveEventToFirestore(null);
        }
    }

    private void uploadImage(String eventTitle) {
        // events/UID_Random.jpg
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("events/" + filename);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveEventToFirestore(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error subiendo imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveEventToFirestore(null); // Guardar sin imagen si falla
                });
    }

    private void saveEventToFirestore(String imageUrl) {
        EventModel event = new EventModel();

        // Llenar modelo usando los nuevos SETTERS
        event.setTitle(etTitle.getText().toString().trim());
        event.setDescription(etDescription.getText().toString().trim());
        event.setMacroEvent(etMacroEvent.getText().toString().trim());

        event.setCategory(dropdownCategory.getText().toString());
        event.setDiscipline(dropdownDiscipline.getText().toString());
        event.setModality(dropdownModality.getText().toString());

        event.setPlaceName(etPlace.getText().toString().trim());
        event.setLatitude(selectedLat);
        event.setLongitude(selectedLng);

        // Convertir Calendar a Timestamp
        event.setEventDateTime(new Timestamp(new Date(eventCalendar.getTimeInMillis())));

        // Cupos
        try {
            event.setMinQuota(Integer.parseInt(etMinQuota.getText().toString()));
            event.setMaxQuota(Integer.parseInt(etMaxQuota.getText().toString()));
        } catch (NumberFormatException e) {
            event.setMinQuota(2);
            event.setMaxQuota(50);
        }

        // Datos automáticos
        event.setOrganizerId(auth.getCurrentUser().getUid());
        event.setOrganizerName("IMCUFIDE"); // O el nombre del usuario si lo tienes cargado
        event.setStatus("ACTIVO");
        event.setCreatedAt(Timestamp.now());

        // Imagen (Lista de 1 por ahora)
        if (imageUrl != null) {
            List<String> images = new ArrayList<>();
            images.add(imageUrl);
            event.setImageUrls(images);
        }

        // Guardar
        db.collection("events").add(event)
                .addOnSuccessListener(docRef -> {
                    // Guardar el ID generado dentro del documento
                    docRef.update("id", docRef.getId());

                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "¡Evento Publicado!", Toast.LENGTH_LONG).show();
                    finish(); // Volver al Home
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    btnPublishEvent.setEnabled(true);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
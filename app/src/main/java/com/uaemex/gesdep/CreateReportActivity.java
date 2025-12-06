package com.uaemex.gesdep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uaemex.gesdep.adapters.ReportPhotoAdapter;
import com.uaemex.gesdep.models.NotificationModel;
import com.uaemex.gesdep.models.ReportModel;
import com.uaemex.gesdep.utils.NotificationHelper;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateReportActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvEventName, tvPriorityValue;
    private TextInputEditText etSubject, etDescription;
    private AutoCompleteTextView actvCategory;
    private RecyclerView rvPhotos;
    private MaterialButton btnAddPhoto, btnSubmitReport;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;

    private String eventId;
    private String eventName;
    private String currentUserRole;
    private String currentUserName;

    private List<Uri> selectedPhotos = new ArrayList<>();
    private ReportPhotoAdapter photoAdapter;

    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri photoUri = result.getData().getData();
                    if (photoUri != null && selectedPhotos.size() < 5) {
                        selectedPhotos.add(photoUri);
                        photoAdapter.notifyDataSetChanged();
                    } else if (selectedPhotos.size() >= 5) {
                        Toast.makeText(this, "Máximo 5 fotografías", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        // Configurar barra de estado verde
        WindowUtils.setGreenStatusBar(this);

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        if (eventId == null || eventName == null) {
            Toast.makeText(this, "Error: Datos del evento no encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance("gesdep");
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("report_photos");

        initViews();
        setupCategoryDropdown();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvEventName = findViewById(R.id.tvEventName);
        tvPriorityValue = findViewById(R.id.tvPriorityValue);
        etSubject = findViewById(R.id.etSubject);
        etDescription = findViewById(R.id.etDescription);
        actvCategory = findViewById(R.id.actvCategory);
        rvPhotos = findViewById(R.id.rvPhotos);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        progressBar = findViewById(R.id.progressBar);

        tvEventName.setText(eventName);

        photoAdapter = new ReportPhotoAdapter(selectedPhotos, this::removePhoto);
        rvPhotos.setLayoutManager(new GridLayoutManager(this, 3));
        rvPhotos.setAdapter(photoAdapter);
    }

    private void setupCategoryDropdown() {
        String[] categories = ReportModel.getCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);

        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = categories[position];
            String priority = ReportModel.assignPriorityByCategory(selectedCategory);
            tvPriorityValue.setText(priority);
            updatePriorityColor(priority);
        });
    }

    private void updatePriorityColor(String priority) {
        int color;
        switch (priority) {
            case ReportModel.PRIORITY_HIGH: color = getResources().getColor(android.R.color.holo_red_dark, getTheme()); break;
            case ReportModel.PRIORITY_MEDIUM: color = getResources().getColor(android.R.color.holo_orange_dark, getTheme()); break;
            default: color = getResources().getColor(android.R.color.holo_green_dark, getTheme()); break;
        }
        tvPriorityValue.setTextColor(color);
    }

    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserRole = documentSnapshot.getString("role");
                        currentUserName = documentSnapshot.getString("name");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        btnAddPhoto.setOnClickListener(v -> {
            if (selectedPhotos.size() < 5) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Máximo 5 fotografías", Toast.LENGTH_SHORT).show();
            }
        });
        btnSubmitReport.setOnClickListener(v -> validateAndSubmit());
    }

    private void removePhoto(int position) {
        selectedPhotos.remove(position);
        photoAdapter.notifyDataSetChanged();
    }

    private void validateAndSubmit() {
        String subject = etSubject.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();

        if (subject.isEmpty()) { etSubject.setError("Requerido"); return; }
        if (category.isEmpty()) { Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show(); return; }
        if (description.isEmpty()) { etDescription.setError("Requerido"); return; }

        createReport(subject, description, category);
    }

    private void createReport(String subject, String description, String category) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmitReport.setEnabled(false);

        String uid = auth.getCurrentUser().getUid();
        ReportModel report = new ReportModel(
                eventId, eventName, uid,
                currentUserName != null ? currentUserName : "Usuario",
                currentUserRole != null ? currentUserRole : "user",
                subject, description, category
        );

        DocumentReference reportRef = db.collection("reports").document();
        report.setReportId(reportRef.getId());

        if (selectedPhotos.isEmpty()) {
            saveReportToFirestore(reportRef, report);
        } else {
            uploadPhotosAndSaveReport(reportRef, report);
        }
    }

    private void uploadPhotosAndSaveReport(DocumentReference reportRef, ReportModel report) {
        List<String> photoUrls = new ArrayList<>();
        int[] uploadCount = {0};
        int totalPhotos = selectedPhotos.size();

        for (int i = 0; i < selectedPhotos.size(); i++) {
            Uri photoUri = selectedPhotos.get(i);
            StorageReference fileRef = storageRef.child(reportRef.getId() + "_" + i + ".jpg");

            fileRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            photoUrls.add(uri.toString());
                            uploadCount[0]++;
                            if (uploadCount[0] == totalPhotos) {
                                report.setPhotoUrls(photoUrls);
                                saveReportToFirestore(reportRef, report);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        uploadCount[0]++;
                        if (uploadCount[0] == totalPhotos) {
                            report.setPhotoUrls(photoUrls);
                            saveReportToFirestore(reportRef, report);
                        }
                    });
        }
    }

    private void saveReportToFirestore(DocumentReference reportRef, ReportModel report) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportId", report.getReportId());
        reportData.put("eventId", report.getEventId());
        reportData.put("eventName", report.getEventName());

        // --- AQUÍ ESTÁ EL CAMBIO CRÍTICO: Usamos "reporterId" ---
        reportData.put("reporterId", report.getCreatedByUid());

        reportData.put("createdByName", report.getCreatedByName());
        reportData.put("createdByRole", report.getCreatedByRole());
        reportData.put("subject", report.getSubject());
        reportData.put("description", report.getDescription());
        reportData.put("photoUrls", report.getPhotoUrls());
        reportData.put("category", report.getCategory());
        reportData.put("priority", report.getPriority());
        reportData.put("status", report.getStatus());
        reportData.put("createdAt", report.getCreatedAt());
        reportData.put("updatedAt", report.getUpdatedAt());

        reportRef.set(reportData)
                .addOnSuccessListener(aVoid -> {
                    createNotificationsForReport(report);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Reporte enviado exitosamente", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitReport.setEnabled(true);
                    Toast.makeText(this, "Error al enviar reporte", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNotificationsForReport(ReportModel report) {
        notifyAdmins(report);
        notifyEventParticipants(report);
    }

    private void notifyAdmins(ReportModel report) {
        db.collection("users").whereEqualTo("role", "admin").get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> {
                        NotificationModel notification = new NotificationModel(
                                "Nuevo Reporte: " + report.getEventName(),
                                report.getSubject() + " - Prioridad: " + report.getPriority(),
                                report.getEventId(), report.getReportId(), doc.getId()
                        );
                        NotificationHelper.saveNotification(db, notification);
                    });
                });
    }

    private void notifyEventParticipants(ReportModel report) {
        db.collection("eventParticipants").whereEqualTo("eventId", report.getEventId()).get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> {
                        String participantUid = doc.getString("userId");
                        if (participantUid != null && !participantUid.equals(report.getCreatedByUid())) {
                            NotificationModel notification = new NotificationModel(
                                    "Reporte en: " + report.getEventName(),
                                    report.getSubject(),
                                    report.getEventId(), report.getReportId(), participantUid
                            );
                            NotificationHelper.saveNotification(db, notification);
                        }
                    });
                });
    }
}
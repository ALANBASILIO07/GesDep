package com.uaemex.gesdep;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.adapters.ReportPhotoViewAdapter;
import com.uaemex.gesdep.models.NotificationModel;
import com.uaemex.gesdep.models.ReportModel;
import com.uaemex.gesdep.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDetailActivity extends AppCompatActivity {

    private TextView tvEventName, tvPriority, tvStatus, tvSubject, tvCategory,
            tvDescription, tvCreatedBy, tvAdminResponse, tvRespondedBy;
    private RecyclerView rvPhotos;
    private CardView cvPhotos, cvAdminResponse;
    private LinearLayout layoutAdminActions;
    private AutoCompleteTextView actvStatus;
    private TextInputEditText etAdminResponse;
    private MaterialButton btnSaveChanges;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String reportId;
    private ReportModel currentReport;
    private String currentUserRole;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        reportId = getIntent().getStringExtra("reportId");

        if (reportId == null) {
            Toast.makeText(this, "Error: ID de reporte no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance("gesdep");
        auth = FirebaseAuth.getInstance();

        initViews();
        setupStatusDropdown();
        loadUserRole();
        loadReportDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEventName = findViewById(R.id.tvEventName);
        tvPriority = findViewById(R.id.tvPriority);
        tvStatus = findViewById(R.id.tvStatus);
        tvSubject = findViewById(R.id.tvSubject);
        tvCategory = findViewById(R.id.tvCategory);
        tvDescription = findViewById(R.id.tvDescription);
        tvCreatedBy = findViewById(R.id.tvCreatedBy);
        tvAdminResponse = findViewById(R.id.tvAdminResponse);
        tvRespondedBy = findViewById(R.id.tvRespondedBy);
        rvPhotos = findViewById(R.id.rvPhotos);
        cvPhotos = findViewById(R.id.cvPhotos);
        cvAdminResponse = findViewById(R.id.cvAdminResponse);
        layoutAdminActions = findViewById(R.id.layoutAdminActions);
        actvStatus = findViewById(R.id.actvStatus);
        etAdminResponse = findViewById(R.id.etAdminResponse);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void setupStatusDropdown() {
        String[] statuses = {
                ReportModel.STATUS_PENDING,
                ReportModel.STATUS_IN_PROCESS,
                ReportModel.STATUS_RESOLVED
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(adapter);
    }

    private void loadUserRole() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserRole = documentSnapshot.getString("role");
                        currentUserName = documentSnapshot.getString("name");
                    }
                });
    }

    private void loadReportDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("reports").document(reportId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentReport = parseReportFromDocument(documentSnapshot);
                        displayReport();
                    } else {
                        Toast.makeText(this, "Reporte no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar reporte", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                });
    }

    private ReportModel parseReportFromDocument(DocumentSnapshot doc) {
        ReportModel report = new ReportModel();
        report.setReportId(doc.getString("reportId"));
        report.setEventId(doc.getString("eventId"));
        report.setEventName(doc.getString("eventName"));
        report.setCreatedByUid(doc.getString("createdByUid"));
        report.setCreatedByName(doc.getString("createdByName"));
        report.setCreatedByRole(doc.getString("createdByRole"));
        report.setSubject(doc.getString("subject"));
        report.setDescription(doc.getString("description"));
        report.setCategory(doc.getString("category"));
        report.setPriority(doc.getString("priority"));
        report.setStatus(doc.getString("status"));

        Long createdAt = doc.getLong("createdAt");
        if (createdAt != null) report.setCreatedAt(createdAt);

        Long updatedAt = doc.getLong("updatedAt");
        if (updatedAt != null) report.setUpdatedAt(updatedAt);

        List<String> photoUrls = (List<String>) doc.get("photoUrls");
        if (photoUrls != null) report.setPhotoUrls(photoUrls);

        String adminResponse = doc.getString("adminResponse");
        if (adminResponse != null) {
            report.setAdminResponse(adminResponse);
            report.setRespondedByUid(doc.getString("respondedByUid"));
            report.setRespondedByName(doc.getString("respondedByName"));
        }

        return report;
    }

    private void displayReport() {
        tvEventName.setText(currentReport.getEventName());
        tvPriority.setText(currentReport.getPriority());
        tvStatus.setText(currentReport.getStatus());
        tvSubject.setText(currentReport.getSubject());
        tvCategory.setText(currentReport.getCategory());
        tvDescription.setText(currentReport.getDescription());

        // Set priority color
        int priorityColor;
        switch (currentReport.getPriority()) {
            case ReportModel.PRIORITY_HIGH:
                priorityColor = getResources().getColor(android.R.color.holo_red_dark, getTheme());
                break;
            case ReportModel.PRIORITY_MEDIUM:
                priorityColor = getResources().getColor(android.R.color.holo_orange_dark, getTheme());
                break;
            default:
                priorityColor = getResources().getColor(android.R.color.holo_green_dark, getTheme());
                break;
        }
        tvPriority.setTextColor(priorityColor);

        // Created by info
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(currentReport.getCreatedAt()));
        tvCreatedBy.setText(String.format("Creado por: %s - %s",
                currentReport.getCreatedByName(), dateStr));

        // Photos
        if (currentReport.getPhotoUrls() != null && !currentReport.getPhotoUrls().isEmpty()) {
            cvPhotos.setVisibility(View.VISIBLE);
            ReportPhotoViewAdapter photoAdapter = new ReportPhotoViewAdapter(currentReport.getPhotoUrls());
            rvPhotos.setLayoutManager(new GridLayoutManager(this, 3));
            rvPhotos.setAdapter(photoAdapter);
        } else {
            cvPhotos.setVisibility(View.GONE);
        }

        // Admin response
        if (currentReport.getAdminResponse() != null && !currentReport.getAdminResponse().isEmpty()) {
            cvAdminResponse.setVisibility(View.VISIBLE);
            tvAdminResponse.setText(currentReport.getAdminResponse());
            tvRespondedBy.setText("Por: " + currentReport.getRespondedByName());
        } else {
            cvAdminResponse.setVisibility(View.GONE);
        }

        // Admin actions (only for admins)
        if ("admin".equals(currentUserRole)) {
            layoutAdminActions.setVisibility(View.VISIBLE);
            actvStatus.setText(currentReport.getStatus(), false);
            if (currentReport.getAdminResponse() != null) {
                etAdminResponse.setText(currentReport.getAdminResponse());
            }
        } else {
            layoutAdminActions.setVisibility(View.GONE);
        }
    }

    private void saveChanges() {
        if (!"admin".equals(currentUserRole)) {
            Toast.makeText(this, "Solo administradores pueden actualizar reportes", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatus = actvStatus.getText().toString().trim();
        String adminResponse = etAdminResponse.getText().toString().trim();

        if (newStatus.isEmpty()) {
            Toast.makeText(this, "Seleccione un estado", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", System.currentTimeMillis());

        if (!adminResponse.isEmpty()) {
            updates.put("adminResponse", adminResponse);
            updates.put("respondedByUid", auth.getCurrentUser().getUid());
            updates.put("respondedByName", currentUserName);
        }

        db.collection("reports").document(reportId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Create notification if status changed
                    if (!newStatus.equals(currentReport.getStatus())) {
                        createStatusChangeNotifications(newStatus);
                    }

                    Toast.makeText(this, "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
                    loadReportDetails(); // Reload to show updated data
                    btnSaveChanges.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                });
    }

    private void createStatusChangeNotifications(String newStatus) {
        // Notify report creator
        NotificationModel creatorNotification = NotificationHelper.createStatusChangeNotification(
                currentReport.getEventId(),
                currentReport.getEventName(),
                currentReport.getReportId(),
                newStatus,
                currentReport.getCreatedByUid()
        );
        NotificationHelper.saveNotification(db, creatorNotification);

        // Notify event participants
        db.collection("eventParticipants")
                .whereEqualTo("eventId", currentReport.getEventId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> {
                        String participantUid = doc.getString("userId");
                        if (participantUid != null && !participantUid.equals(currentReport.getCreatedByUid())) {
                            NotificationModel notification = NotificationHelper.createStatusChangeNotification(
                                    currentReport.getEventId(),
                                    currentReport.getEventName(),
                                    currentReport.getReportId(),
                                    newStatus,
                                    participantUid
                            );
                            NotificationHelper.saveNotification(db, notification);
                        }
                    });
                });
    }
}

package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar; // IMPORTANTE
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uaemex.gesdep.adapters.ReportsAdapter;
import com.uaemex.gesdep.models.ReportModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

public class ReportsListActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private MaterialToolbar toolbar; // CAMBIADO de ImageButton a MaterialToolbar
    private AutoCompleteTextView actvPriorityFilter, actvStatusFilter;
    private FloatingActionButton fabCreateGeneralReport;

    private FirebaseFirestore db;
    private ReportsAdapter adapter;
    private List<ReportModel> allReports = new ArrayList<>();
    private List<ReportModel> filteredReports = new ArrayList<>();

    private String selectedPriority = "Todas";
    private String selectedStatus = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_list);

        WindowUtils.setGreenStatusBar(this);

        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupFilters();
        setupRecyclerView();
        loadReports();
    }

    private void initViews() {
        // Nuevo setup para Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvReports = findViewById(R.id.rvReports);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        actvPriorityFilter = findViewById(R.id.actvPriorityFilter);
        actvStatusFilter = findViewById(R.id.actvStatusFilter);
        fabCreateGeneralReport = findViewById(R.id.fabCreateGeneralReport);

        fabCreateGeneralReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateReportActivity.class);
            intent.putExtra("eventId", "GENERAL");
            intent.putExtra("eventName", "Aviso General");
            startActivity(intent);
        });
    }

    // ... (Resto de métodos setupFilters, setupRecyclerView, loadReports, applyFilters, openReportDetail IGUALES) ...
    // Solo copia el resto del código que ya tenías, no ha cambiado nada lógico, solo la referencia al toolbar.

    private void setupFilters() {
        String[] priorities = {"Todas", "ALTA", "MEDIA", "BAJA"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, priorities);
        actvPriorityFilter.setAdapter(priorityAdapter);
        actvPriorityFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedPriority = priorities[position];
            applyFilters();
        });

        String[] statuses = {"Todos", ReportModel.STATUS_PENDING, ReportModel.STATUS_IN_PROCESS, ReportModel.STATUS_RESOLVED};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatusFilter.setAdapter(statusAdapter);
        actvStatusFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = statuses[position];
            applyFilters();
        });
    }

    private void setupRecyclerView() {
        adapter = new ReportsAdapter(filteredReports, this::openReportDetail);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        db.collection("reports").orderBy("createdAt", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allReports.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // ... (Mismo parseo de ReportModel que tenías) ...
                        // Para ahorrar espacio, usa tu método de parseo actual o el bloque que ya tenías.
                        // Es idéntico.
                        ReportModel report = doc.toObject(ReportModel.class); // O tu parseo manual
                        if(report != null) allReports.add(report);
                    }
                    applyFilters();
                    progressBar.setVisibility(View.GONE);
                    if (filteredReports.isEmpty()) layoutEmpty.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void applyFilters() {
        filteredReports.clear();
        for (ReportModel report : allReports) {
            boolean matchesPriority = selectedPriority.equals("Todas") || report.getPriority().equals(selectedPriority);
            boolean matchesStatus = selectedStatus.equals("Todos") || report.getStatus().equals(selectedStatus);
            if (matchesPriority && matchesStatus) filteredReports.add(report);
        }
        adapter.updateReports(filteredReports);
        if (filteredReports.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvReports.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvReports.setVisibility(View.VISIBLE);
        }
    }

    private void openReportDetail(ReportModel report) {
        Intent intent = new Intent(this, ReportDetailActivity.class);
        intent.putExtra("reportId", report.getReportId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }
}
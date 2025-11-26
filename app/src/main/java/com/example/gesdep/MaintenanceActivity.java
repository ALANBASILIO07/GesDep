package com.example.gesdep;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private MaintenanceAdapter adapter;
    private List<MaintenanceReport> reportList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBarMaintenance);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvReports = findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaintenanceAdapter(reportList);
        rvReports.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddReport);
        fab.setOnClickListener(v -> mostrarDialogoNuevoReporte());

        escucharCambios();
    }

    private void escucharCambios() {
        db.collection("maintenance_reports")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(MaintenanceActivity.this,
                                    "Error cargando reportes",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<MaintenanceReport> temp = new ArrayList<>();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                MaintenanceReport r = doc.toObject(MaintenanceReport.class);
                                r.setId(doc.getId());
                                temp.add(r);
                            }
                        }
                        reportList = temp;
                        adapter.updateList(reportList);
                    }
                });
    }

    private void mostrarDialogoNuevoReporte() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo reporte");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Título (ej. Foco fundido en cancha)");
        layout.addView(etTitle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText etDesc = new EditText(this);
        etDesc.setHint("Descripción");
        layout.addView(etDesc, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText etLocation = new EditText(this);
        etLocation.setHint("Ubicación");
        layout.addView(etLocation, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "El título es obligatorio",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            MaintenanceReport r = new MaintenanceReport(
                    null, title, desc, location, "Pendiente"
            );

            db.collection("maintenance_reports")
                    .add(r)
                    .addOnSuccessListener(x ->
                            Toast.makeText(this, "Reporte agregado", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}

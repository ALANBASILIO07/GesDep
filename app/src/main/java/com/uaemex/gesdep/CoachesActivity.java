package com.uaemex.gesdep;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CoachesActivity extends AppCompatActivity {

    private String role = "user";
    private RecyclerView rvCoaches;
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;
    private List<CoachModel> coachList = new ArrayList<>();
    private CoachAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coaches);

        if (getIntent() != null) {
            String r = getIntent().getStringExtra("role");
            if (r != null) role = r;
        }

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBarCoaches);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCoaches = findViewById(R.id.rvCoaches);
        rvCoaches.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CoachAdapter(coachList, coach -> {
            if (role.equals("admin")) {
                confirmarEliminar(coach);
            } else {
                Toast.makeText(this, "Solo admin puede eliminar", Toast.LENGTH_SHORT).show();
            }
        });
        rvCoaches.setAdapter(adapter);

        fabAdd = findViewById(R.id.fabAddCoach);

        // Solo admin puede agregar
        if (role.equals("admin")) {
            fabAdd.setOnClickListener(v -> mostrarDialogoAgregar());
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        cargarCoaches();
    }

    private void cargarCoaches() {
        db.collection("coaches")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    coachList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CoachModel c = doc.toObject(CoachModel.class);
                        coachList.add(c);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando entrenadores", Toast.LENGTH_SHORT).show());
    }

    private void mostrarDialogoAgregar() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etName = new EditText(this);
        etName.setHint("Nombre");
        layout.addView(etName);

        EditText etSport = new EditText(this);
        etSport.setHint("Deporte");
        layout.addView(etSport);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Teléfono");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Correo");
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo entrenador")
                .setView(layout)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String sport = etSport.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    if (name.isEmpty() || sport.isEmpty()) {
                        Toast.makeText(this, "Nombre y deporte son obligatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String id = UUID.randomUUID().toString();
                    CoachModel coach = new CoachModel(id, name, sport, phone, email);

                    db.collection("coaches").document(id)
                            .set(coach)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Entrenador guardado", Toast.LENGTH_SHORT).show();
                                cargarCoaches();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error guardando entrenador", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminar(CoachModel coach) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar entrenador")
                .setMessage("¿Eliminar a " + coach.getName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("coaches").document(coach.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                                cargarCoaches();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error eliminando", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}

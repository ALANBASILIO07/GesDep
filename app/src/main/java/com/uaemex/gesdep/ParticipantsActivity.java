package com.uaemex.gesdep;

import android.os.Bundle;
import android.text.InputType;
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

public class ParticipantsActivity extends AppCompatActivity {

    private RecyclerView rvParticipants;
    private ParticipantAdapter adapter;
    private List<ParticipantModel> participantList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBarParticipants);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvParticipants = findViewById(R.id.rvParticipants);
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParticipantAdapter(participantList);
        rvParticipants.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddParticipant);
        fab.setOnClickListener(v -> mostrarDialogoNuevoParticipante());

        escucharCambios();
    }

    private void escucharCambios() {
        db.collection("participants")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(ParticipantsActivity.this,
                                    "Error cargando participantes",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<ParticipantModel> temp = new ArrayList<>();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                ParticipantModel p = doc.toObject(ParticipantModel.class);
                                p.setId(doc.getId());
                                temp.add(p);
                            }
                        }
                        participantList = temp;
                        adapter.updateList(participantList);
                    }
                });
    }

    private void mostrarDialogoNuevoParticipante() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo participante");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText etName = new EditText(this);
        etName.setHint("Nombre");
        layout.addView(etName, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText etAge = new EditText(this);
        etAge.setHint("Edad");
        etAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etAge, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText etActivity = new EditText(this);
        etActivity.setHint("Actividad (ej. Fútbol)");
        layout.addView(etActivity, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText etPhone = new EditText(this);
        etPhone.setHint("Teléfono");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String activity = etActivity.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty() || activity.isEmpty()) {
                Toast.makeText(this, "Nombre, edad y actividad son obligatorios",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Edad inválida", Toast.LENGTH_SHORT).show();
                return;
            }

            ParticipantModel p = new ParticipantModel(null, name, age, activity, phone);
            db.collection("participants")
                    .add(p)
                    .addOnSuccessListener(r ->
                            Toast.makeText(this, "Participante agregado", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}

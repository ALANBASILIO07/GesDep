package com.uaemex.gesdep;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Importación de Query
import java.util.Arrays; // Importación de Arrays

import com.uaemex.gesdep.adapters.ParticipantAdapter; // El nombre que ya corrigió
import com.uaemex.gesdep.models.UserModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsActivity extends AppCompatActivity {

    private RecyclerView rvParticipants;
    private LinearLayout emptyStateView;
    // Corregido: El adaptador debe usar el nombre de archivo correcto (ParticipantsAdapter)
    private ParticipantAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddParticipant;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        // Aplica el color verde al StatusBar
        WindowUtils.setGreenStatusBar(this);

        db = FirebaseFirestore.getInstance("gesdep");
        userList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void initViews() {
        rvParticipants = findViewById(R.id.rvParticipants);
        emptyStateView = findViewById(R.id.emptyState);
        fabAddParticipant = findViewById(R.id.fabAddParticipant);
        toolbar = findViewById(R.id.topAppBarParticipants);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        // El adaptador debe usar el nombre de clase correcto (ParticipantsAdapter)
        adapter = new ParticipantAdapter(this::onUserClick);
        rvParticipants.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddParticipant.setOnClickListener(v -> {
            // Lógica para registrar un nuevo usuario/participante
            Toast.makeText(this, "Abrir pantalla de registro de usuario", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, RegisterActivity.class);
            // startActivity(intent);
        });
    }

    private void loadUsers() {
        // Consultar la colección 'users' y filtrar por rol si es necesario
        db.collection("users")
                .whereIn("role", Arrays.asList("user", "participant"))
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            UserModel user = doc.toObject(UserModel.class);
                            if (user != null) {
                                // CORRECCIÓN CLAVE: Usar setUid para asignar el ID del documento
                                user.setUid(doc.getId());
                                userList.add(user);
                            }
                        }
                        adapter.updateUsers(userList);
                        toggleEmptyState(false);
                    } else {
                        adapter.updateUsers(new ArrayList<>());
                        toggleEmptyState(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    toggleEmptyState(true);
                });
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvParticipants.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            rvParticipants.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void onUserClick(UserModel user) {
        Toast.makeText(this, "Detalle de: " + user.getName(), Toast.LENGTH_SHORT).show();
        // Lógica para abrir el detalle del usuario (ej: UserDetailActivity)
    }
}
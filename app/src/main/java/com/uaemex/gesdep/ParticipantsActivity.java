package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uaemex.gesdep.adapters.ParticipantAdapter;
import com.uaemex.gesdep.models.UserModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ParticipantsActivity extends AppCompatActivity {

    private RecyclerView rvParticipants;
    private LinearLayout emptyStateView;
    private ParticipantAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FloatingActionButton fabAddParticipant;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        WindowUtils.setGreenStatusBar(this);

        db = FirebaseFirestore.getInstance("gesdep");
        auth = FirebaseAuth.getInstance();
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
        adapter = new ParticipantAdapter(user -> {
            Intent intent = new Intent(ParticipantsActivity.this, UserDetailActivity.class);
            intent.putExtra("user_data", user);
            startActivity(intent);
        });
        rvParticipants.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddParticipant.setOnClickListener(v -> {
            Intent intent = new Intent(ParticipantsActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loadUsers() {
        String currentUserId = auth.getUid();

        db.collection("users")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {

                            // --- FILTRO: Ocultar al usuario en sesión ---
                            if (currentUserId != null && currentUserId.equals(doc.getId())) {
                                continue;
                            }
                            // ---------------------------------------------

                            UserModel user = mapSnapshotToUserModel(doc);
                            if (user != null) {
                                userList.add(user);
                            }
                        }
                        adapter.updateUsers(userList);
                        toggleEmptyState(userList.isEmpty());
                    } else {
                        adapter.updateUsers(new ArrayList<>());
                        toggleEmptyState(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error de carga: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    toggleEmptyState(true);
                });
    }

    private UserModel mapSnapshotToUserModel(DocumentSnapshot doc) {
        // Intento primario con mapeo manual para máxima seguridad y evitar crashes
        UserModel model = new UserModel();
        try {
            model.setUid(doc.getId());
            model.setName(doc.getString("name"));
            model.setEmail(doc.getString("email"));
            model.setRole(doc.getString("role"));
            model.setPhone(doc.getString("phone"));

            // Prioridad a photoUrl, fallback a profilePhotoUrl
            String photo = doc.getString("photoUrl");
            if (photo == null) photo = doc.getString("profilePhotoUrl");
            model.setProfilePhotoUrl(photo);

            Object createdObj = doc.get("createdAt");
            if (createdObj instanceof Long) {
                model.setCreatedAt(new Date((Long) createdObj));
            } else if (createdObj instanceof Timestamp) {
                model.setCreatedAt(((Timestamp) createdObj).toDate());
            }
        } catch (Exception e) {
            // Silenciosamente fallamos o loggeamos, pero no mostramos Toast al usuario
            // Log.e("MappingError", "Error mapping user: " + e.getMessage());
        }
        return model;
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
}
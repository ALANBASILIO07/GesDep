package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.adapters.EventsAdapter;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.repositories.EventRepository;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.List;

/**
 * Activity para mostrar lista de eventos deportivos y culturales
 */
public class EventsActivity extends AppCompatActivity implements EventsAdapter.OnEventClickListener {

    private MaterialToolbar toolbar;
    private ChipGroup chipGroup;
    private Chip chipAll, chipSports, chipCultural;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyState;
    private FloatingActionButton fabAddEvent; // Nuevo botón

    private EventsAdapter adapter;
    private EventRepository repository;

    // Firebase para validar rol
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // 1. Estética: Barra Verde
        WindowUtils.setGreenStatusBar(this);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

        // 2. Lógica de Rol (Mostrar/Ocultar botón Crear)
        checkUserRole();

        repository = new EventRepository();
        loadAllEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipSports = findViewById(R.id.chipSports);
        chipCultural = findViewById(R.id.chipCultural);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        fabAddEvent = findViewById(R.id.fabAddEvent); // Vincular el botón
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Verifica si el usuario es ADMIN para mostrar el botón de crear evento.
     */
    private void checkUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            // Solo si es ADMIN mostramos el botón
                            if ("admin".equals(role)) {
                                fabAddEvent.setVisibility(View.VISIBLE);
                                fabAddEvent.setOnClickListener(v -> {
                                    // Ir al formulario de creación
                                    startActivity(new Intent(EventsActivity.this, CreateEventActivity.class));
                                });
                            } else {
                                fabAddEvent.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(e -> fabAddEvent.setVisibility(View.GONE));
        } else {
            fabAddEvent.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        adapter = new EventsAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFilters() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipAll) {
                loadAllEvents();
            } else if (checkedId == R.id.chipSports) {
                loadEventsByType("Deportivo"); // Ajustado a Mayúscula según tu modelo
            } else if (checkedId == R.id.chipCultural) {
                loadEventsByType("Cultural");
            }
        });
    }

    private void loadAllEvents() {
        showLoading(true);

        repository.getAllActiveEvents(new EventRepository.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<EventModel> events) {
                showLoading(false);

                if (events.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    adapter.updateEvents(events);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                // Si hay error, mostramos empty state por ahora
                if (adapter.getItemCount() == 0) showEmptyState(true);
                Toast.makeText(EventsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventsByType(String category) {
        showLoading(true);

        repository.getEventsByType(category, new EventRepository.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<EventModel> events) {
                showLoading(false);

                if (events.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    adapter.updateEvents(events);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                if (adapter.getItemCount() == 0) showEmptyState(true);
                Toast.makeText(EventsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(View.GONE); // Asegurar que se oculte
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEventClick(EventModel event) {
        // Actualizado para usar los nuevos Getters del EventModel
        String msg = "Evento: " + event.getTitle();
        if (event.getEventDateTime() != null) {
            msg += "\nFecha: " + event.getEventDateTime().toDate().toString();
        }
        msg += "\nLugar: " + event.getPlaceName();

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        // TODO: Aquí abriremos ActivityDetail en el futuro
        // Intent intent = new Intent(this, ActivityDetailActivity.class);
        // intent.putExtra("event", event); // EventModel es Serializable
        // startActivity(intent);
    }
}
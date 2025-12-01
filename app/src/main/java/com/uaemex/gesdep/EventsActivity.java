package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.adapters.EventsAdapter;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.models.VenueModel;
import com.uaemex.gesdep.repositories.EventRepository;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventsActivity extends AppCompatActivity implements EventsAdapter.OnEventClickListener {

    private MaterialToolbar toolbar;
    private AutoCompleteTextView filterDiscipline, filterModality, filterVenue;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyState;
    private FloatingActionButton fabAddEvent;

    private EventsAdapter adapter;
    private EventRepository repository;
    private List<EventModel> allEventsList = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupToolbar();
        setupRecyclerView();
        checkUserRole();

        repository = new EventRepository();

        // Cargar filtros y datos
        setupFilters();
        loadVenuesForFilter();
        loadAllEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        filterDiscipline = findViewById(R.id.filterDiscipline);
        filterModality = findViewById(R.id.filterModality);
        filterVenue = findViewById(R.id.filterVenue); // NUEVO
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        fabAddEvent = findViewById(R.id.fabAddEvent);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void checkUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && "admin".equals(doc.getString("role"))) {
                            fabAddEvent.setVisibility(View.VISIBLE);
                            fabAddEvent.setOnClickListener(v -> startActivity(new Intent(EventsActivity.this, CreateEventActivity.class)));
                        } else {
                            fabAddEvent.setVisibility(View.GONE);
                        }
                    });
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
        // Disciplina
        List<String> disciplines = new ArrayList<>();
        disciplines.add("Todas");
        disciplines.addAll(Arrays.asList(getResources().getStringArray(R.array.event_disciplines)));
        setFilterAdapter(filterDiscipline, disciplines);

        // Modalidad
        List<String> modalities = new ArrayList<>();
        modalities.add("Todas");
        modalities.addAll(Arrays.asList(getResources().getStringArray(R.array.event_modalities)));
        setFilterAdapter(filterModality, modalities);
    }

    private void loadVenuesForFilter() {
        db.collection("venues").get().addOnSuccessListener(querySnapshot -> {
            List<String> venues = new ArrayList<>();
            venues.add("Todas");
            for (DocumentSnapshot doc : querySnapshot) {
                VenueModel venue = doc.toObject(VenueModel.class);
                if (venue != null) venues.add(venue.getName());
            }
            setFilterAdapter(filterVenue, venues);
        });
    }

    private void setFilterAdapter(AutoCompleteTextView view, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, data);
        view.setAdapter(adapter);
        view.setText("Todas", false);
        view.setOnItemClickListener((parent, v, position, id) -> applyFilters());
    }

    private void loadAllEvents() {
        showLoading(true);
        repository.getAllActiveEvents(new EventRepository.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<EventModel> events) {
                showLoading(false);
                allEventsList = events;
                checkAutoCancellations(events); // Validar reglas de negocio
                applyFilters(); // Mostrar lista filtrada
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(EventsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String selDisc = filterDiscipline.getText().toString();
        String selMod = filterModality.getText().toString();
        String selVenue = filterVenue.getText().toString();

        List<EventModel> filteredList = new ArrayList<>();

        for (EventModel event : allEventsList) {
            boolean matchDisc = selDisc.equals("Todas") || selDisc.equals(event.getDiscipline());
            boolean matchMod = selMod.equals("Todas") || selMod.equals(event.getModality());
            boolean matchVenue = selVenue.equals("Todas") || selVenue.equals(event.getPlaceName());

            // Solo mostrar si no está cancelado (o mostrarlo con estilo diferente en el adaptador)
            if (matchDisc && matchMod && matchVenue) {
                filteredList.add(event);
            }
        }

        if (filteredList.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
            adapter.updateEvents(filteredList);
        }
    }

    /**
     * Lógica de Negocio: Cancelación Automática
     * Si la fecha ya pasó y no cumplió el cupo mínimo -> CANCELADO
     */
    private void checkAutoCancellations(List<EventModel> events) {
        long now = System.currentTimeMillis();
        for (EventModel event : events) {
            // Validar fecha y cupo
            if (event.getEventDateTime() != null &&
                    event.getEventDateTime().toDate().getTime() < now &&
                    event.getCurrentParticipants() < event.getMinQuota() &&
                    !"CANCELADO".equals(event.getStatus())) {

                // Actualizar estado localmente para la vista
                event.setStatus("CANCELADO");

                // Actualizar en Firebase (Idealmente esto va en Cloud Functions)
                repository.cancelEvent(event.getId(), event.getTitle(),
                        "Cancelado automáticamente por falta de quórum",
                        "SYSTEM", "Sistema",
                        new EventRepository.OnEventCancelledListener() {
                            @Override
                            public void onEventCancelled() {
                                // Evento cancelado en backend
                            }
                            @Override
                            public void onError(String error) { }
                        });
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEventClick(EventModel event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event", event);
        startActivity(intent);
    }
}
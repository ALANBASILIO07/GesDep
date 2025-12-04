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
    private AutoCompleteTextView filterDiscipline, filterModality, filterVenue, filterStatus; // AGREGADO Status
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyState;
    private FloatingActionButton fabAddEvent;

    private EventsAdapter adapter;
    private EventRepository repository;
    private List<EventModel> allEventsList = new ArrayList<>(); // Lista maestra con TODOS los datos

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

        setupFilters();
        loadVenuesForFilter();

        // Cargar todo una sola vez
        loadAllEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        filterDiscipline = findViewById(R.id.filterDiscipline);
        filterModality = findViewById(R.id.filterModality);
        filterVenue = findViewById(R.id.filterVenue);
        filterStatus = findViewById(R.id.filterStatus); // Nuevo campo
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

        // ESTATUS (Nuevo Filtro Manual)
        List<String> statuses = new ArrayList<>();
        statuses.add("Todos");
        statuses.add("ACTIVO");
        statuses.add("EN VIVO");
        statuses.add("PENDIENTE");
        statuses.add("FINALIZADO");
        statuses.add("CANCELADO");
        setFilterAdapter(filterStatus, statuses);
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
        // Seleccionar opción por defecto para evitar nulos
        if (view.getId() == R.id.filterStatus) view.setText("Todos", false);
        else view.setText("Todas", false);

        view.setOnItemClickListener((parent, v, position, id) -> applyFilters());
    }

    private void loadAllEvents() {
        showLoading(true);
        // Llamamos al método nuevo que trae TODO sin filtrar en servidor
        repository.getAllEvents(new EventRepository.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<EventModel> events) {
                showLoading(false);
                allEventsList = events;
                checkAutoCancellations(events);
                applyFilters(); // Aplicar filtros locales (incluido status)
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
        String selStatus = filterStatus.getText().toString(); // Nuevo filtro

        List<EventModel> filteredList = new ArrayList<>();

        for (EventModel event : allEventsList) {
            // Lógica de coincidencia (ignora mayúsculas/minúsculas para seguridad)
            boolean matchDisc = selDisc.equals("Todas") || selDisc.equalsIgnoreCase(event.getDiscipline());
            boolean matchMod = selMod.equals("Todas") || selMod.equalsIgnoreCase(event.getModality());
            boolean matchVenue = selVenue.equals("Todas") || selVenue.equalsIgnoreCase(event.getPlaceName());

            // Lógica de Estatus (Calculamos el estatus dinámico o usamos el guardado)
            String currentStatus = event.getTimeStatus(); // Usamos el helper del modelo
            if (event.getStatus() != null && event.getStatus().equals("CANCELADO")) {
                currentStatus = "CANCELADO";
            }

            boolean matchStatus = selStatus.equals("Todos") || selStatus.equalsIgnoreCase(currentStatus) || selStatus.equalsIgnoreCase(event.getStatus());

            if (matchDisc && matchMod && matchVenue && matchStatus) {
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
     */
    private void checkAutoCancellations(List<EventModel> events) {
        long now = System.currentTimeMillis();
        boolean needsRefresh = false;

        for (EventModel event : events) {
            if (event.getEventDateTime() != null &&
                    event.getEventDateTime().getTime() < now &&
                    event.getCurrentParticipants() < event.getMinQuota() &&
                    !"CANCELADO".equals(event.getStatus())) {

                event.setStatus("CANCELADO");
                needsRefresh = true; // Refrescar lista si algo cambió

                repository.cancelEvent(event.getId(), event.getTitle(),
                        "Cancelado automáticamente", "SYSTEM", "Sistema",
                        new EventRepository.OnEventCancelledListener() {
                            @Override public void onEventCancelled() { }
                            @Override public void onError(String error) { }
                        });
            }
        }

        if (needsRefresh) {
            adapter.notifyDataSetChanged();
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
        Intent intent = new Intent(this, ActivityEventDetail.class);
        intent.putExtra("eventModel", event);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar al volver por si se editó algo en el detalle
        loadAllEvents();
    }
}
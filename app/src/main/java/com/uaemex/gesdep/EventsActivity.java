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
    private AutoCompleteTextView filterDiscipline, filterModality, filterVenue, filterStatus;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyState;
    private FloatingActionButton fabAddEvent;

    private EventsAdapter adapter;
    private EventRepository repository;
    private List<EventModel> allEventsList = new ArrayList<>(); // Lista maestra con TODOS los datos

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isAdmin = false; // Variable para controlar visibilidad

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

        // Primero validamos el rol y LUEGO cargamos los eventos
        checkUserRoleAndLoad();

        repository = new EventRepository();
        setupFilters();
        loadVenuesForFilter();
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

    private void checkUserRoleAndLoad() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && "admin".equals(doc.getString("role"))) {
                            isAdmin = true;
                            fabAddEvent.setVisibility(View.VISIBLE);
                            fabAddEvent.setOnClickListener(v -> startActivity(new Intent(EventsActivity.this, CreateEventActivity.class)));
                        } else {
                            isAdmin = false;
                            fabAddEvent.setVisibility(View.GONE);
                        }
                        // Una vez que sabemos si es admin, cargamos los datos
                        loadAllEvents();
                    });
        } else {
            isAdmin = false;
            fabAddEvent.setVisibility(View.GONE);
            loadAllEvents();
        }
    }

    private void setupRecyclerView() {
        adapter = new EventsAdapter(this); // 'this' funciona porque implementamos la interfaz
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFilters() {
        List<String> disciplines = new ArrayList<>();
        disciplines.add("Todas");
        disciplines.addAll(Arrays.asList(getResources().getStringArray(R.array.event_disciplines)));
        setFilterAdapter(filterDiscipline, disciplines);

        List<String> modalities = new ArrayList<>();
        modalities.add("Todas");
        modalities.addAll(Arrays.asList(getResources().getStringArray(R.array.event_modalities)));
        setFilterAdapter(filterModality, modalities);

        // ESTATUS (Nuevo Filtro Manual) - Sin "Activo"
        List<String> statuses = new ArrayList<>();
        statuses.add("Todos");
        statuses.add("EN VIVO");
        statuses.add("PENDIENTE");
        statuses.add("CONFIRMADO");
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
        String selStatus = filterStatus.getText().toString();

        List<EventModel> filteredList = new ArrayList<>();

        for (EventModel event : allEventsList) {
            // FILTRO DE SEGURIDAD: Si no es admin y está oculto, saltar
            if (!isAdmin && !event.isVisible()) {
                continue;
            }

            boolean matchDisc = selDisc.equals("Todas") || selDisc.equalsIgnoreCase(event.getDiscipline());
            boolean matchMod = selMod.equals("Todas") || selMod.equalsIgnoreCase(event.getModality());
            boolean matchVenue = selVenue.equals("Todas") || selVenue.equalsIgnoreCase(event.getPlaceName());

            // Lógica de Estatus (Calculamos el estatus dinámico o usamos el guardado)
            String currentStatus = event.getTimeStatus(); // Usamos el helper del modelo

            // Si en base de datos dice CANCELADO o CONFIRMADO, eso tiene prioridad sobre la fecha
            if (event.getStatus() != null && (event.getStatus().equals("CANCELADO") || event.getStatus().equals("CONFIRMADO"))) {
                currentStatus = event.getStatus();
            }

            boolean matchStatus = selStatus.equals("Todos") || selStatus.equalsIgnoreCase(currentStatus);

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

    private void checkAutoCancellations(List<EventModel> events) {
        // Esta lógica ya la maneja en parte el repository.checkAndConfirmEvent,
        // pero mantenemos esta iteración rápida para refrescar vista si es necesario.
        for (EventModel event : events) {
            repository.checkAndConfirmEvent(event);
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
    protected void onResume() {
        super.onResume();
        loadAllEvents();
    }

    // --- ESTE ES EL MÉTODO QUE FALTABA Y CAUSABA EL ERROR ---
    @Override
    public void onEventClick(EventModel event) {
        Intent intent = new Intent(this, ActivityEventDetail.class);
        intent.putExtra("eventModel", event);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }
}
package com.uaemex.gesdep;

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
import com.uaemex.gesdep.adapters.EventsAdapter;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.repositories.EventRepository;

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

    private EventsAdapter adapter;
    private EventRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
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
                loadEventsByType("deportivo");
            } else if (checkedId == R.id.chipCultural) {
                loadEventsByType("cultural");
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
                showEmptyState(true);
                Toast.makeText(EventsActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventsByType(String type) {
        showLoading(true);

        repository.getEventsByType(type, new EventRepository.OnEventsLoadedListener() {
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
                showEmptyState(true);
                Toast.makeText(EventsActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEventClick(EventModel event) {
        // Por ahora solo mostramos un Toast
        // En la siguiente fase crearemos EventDetailActivity
        Toast.makeText(this,
                "Evento: " + event.name + "\n" +
                        "Fecha: " + event.eventDateTime.toDate().toString() + "\n" +
                        "Lugar: " + event.placeName,
                Toast.LENGTH_LONG).show();
    }
}

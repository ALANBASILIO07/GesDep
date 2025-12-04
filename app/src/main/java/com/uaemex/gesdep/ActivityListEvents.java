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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uaemex.gesdep.adapters.EventsAdapter;
import com.uaemex.gesdep.models.EventModel;

import java.util.ArrayList;
import java.util.List;

public class ActivityListEvents extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateView; // Para el layout de "No hay actividades"
    private EventsAdapter adapter;
    private List<EventModel> eventList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddEvent;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_list); // Asegúrate de que el XML se llame así

        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

        // 1. Vincular Vistas con los IDs de tu nuevo XML
        recyclerView = findViewById(R.id.rvActivities); // ID actualizado
        emptyStateView = findViewById(R.id.emptyState); // Vista de estado vacío
        fabAddEvent = findViewById(R.id.fabAddActivity);
        toolbar = findViewById(R.id.topAppBarActivities);

        // 2. Configurar Toolbar (Botón atrás)
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // Cierra la actividad al pulsar atrás

        // 3. Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter(this::onEventClick);
        recyclerView.setAdapter(adapter);

        // 4. Configurar FAB
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityListEvents.this, EditEventActivity.class);
            startActivity(intent);
        });

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        db.collection("events")
                .orderBy("eventDateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            EventModel event = doc.toObject(EventModel.class);
                            if (event != null) {
                                event.setId(doc.getId());
                                eventList.add(event);
                            }
                        }
                        adapter.updateEvents(eventList);
                        toggleEmptyState(false); // Hay datos, ocultar mensaje de vacío
                    } else {
                        adapter.updateEvents(new ArrayList<>()); // Lista vacía
                        toggleEmptyState(true); // No hay datos, mostrar mensaje
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show();
                    toggleEmptyState(true); // En caso de error, mostramos estado vacío por seguridad
                });
    }

    // Método auxiliar para alternar entre la lista y el mensaje de "No hay actividades"
    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void onEventClick(EventModel event) {
        Intent intent = new Intent(ActivityListEvents.this, ActivityEventDetail.class);
        intent.putExtra("eventModel", event);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }
}
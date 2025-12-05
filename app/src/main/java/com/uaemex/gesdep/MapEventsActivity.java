package com.uaemex.gesdep;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapEventsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private final String TAG = "MapEventsActivity";
    private LinearLayout tvLegendContainer;
    private String currentFilterStatus = "all_active";

    // Mapeo de estados a colores de marcador (HSV Hue)
    private static final Map<String, Float> STATUS_COLORS = new HashMap<>();
    static {
        // Mapeo de colores puros para los marcadores:
        STATUS_COLORS.put("EN VIVO", BitmapDescriptorFactory.HUE_RED);
        STATUS_COLORS.put("CONFIRMADO", BitmapDescriptorFactory.HUE_GREEN);
        STATUS_COLORS.put("PENDIENTE", BitmapDescriptorFactory.HUE_BLUE);
        STATUS_COLORS.put("FINALIZADO", BitmapDescriptorFactory.HUE_VIOLET); // Usamos VIOLET para Finalizado (Morado/Púrpura)
        STATUS_COLORS.put("CANCELADO", BitmapDescriptorFactory.HUE_ORANGE); // Usamos NARANJA para Cancelado por diferenciación
    }

    // Lista de estados operativos limpios
    private static final List<String> ALL_OPERATIONAL_STATUSES = Arrays.asList(
            "PENDIENTE", "CONFIRMADO", "EN VIVO", "FINALIZADO", "CANCELADO"
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_events);

        // FIX VISUAL: Aplica el color verde al Status Bar
        WindowUtils.setGreenStatusBar(this);

        db = FirebaseFirestore.getInstance("gesdep");
        tvLegendContainer = findViewById(R.id.tv_legend_states_container);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Inicializar el Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_events_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        displayLegend();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            loadActiveEventsOnMap(currentFilterStatus);
            return true;
        }

        // Manejar filtros del submenu
        String newFilter = null;
        if (id == R.id.filter_all) newFilter = "all_active";
        else if (id == R.id.filter_live) newFilter = "EN VIVO";
        else if (id == R.id.filter_confirmed) newFilter = "CONFIRMADO";
        else if (id == R.id.filter_pending) newFilter = "PENDIENTE";
        else if (id == R.id.filter_canceled) newFilter = "CANCELADO";
        else if (id == R.id.filter_finalized) newFilter = "FINALIZADO";

        if (newFilter != null) {
            currentFilterStatus = newFilter;
            loadActiveEventsOnMap(currentFilterStatus);
            displayLegend();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // La cámara debe iniciar centrada en México
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(19.4326, -99.1332), 10f));

        loadActiveEventsOnMap("all_active");
    }

    /**
     * Muestra la leyenda de estados con bolitas de color en el panel flotante.
     */
    private void displayLegend() {
        tvLegendContainer.removeAllViews();

        // Mapeo de estado a el COLOR int de tu paleta
        Map<String, Integer> legendMap = new HashMap<>();
        legendMap.put("EN VIVO", ContextCompat.getColor(this, R.color.red_error));
        legendMap.put("CONFIRMADO", ContextCompat.getColor(this, R.color.green_primary));
        legendMap.put("PENDIENTE", ContextCompat.getColor(this, R.color.blue_button));
        legendMap.put("FINALIZADO", ContextCompat.getColor(this, R.color.purple_participants)); // Morado
        legendMap.put("CANCELADO", ContextCompat.getColor(this, R.color.orange_coach)); // Naranja


        for (Map.Entry<String, Integer> entry : legendMap.entrySet()) {
            String status = entry.getKey();
            int color = entry.getValue();

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setPadding(0, 4, 0, 4);

            // 1. Bolita de Color (Icono) - Usando circle_shape
            View colorCircle = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (12 * getResources().getDisplayMetrics().density),
                    (int) (12 * getResources().getDisplayMetrics().density));
            params.setMargins(0, 0, 8, 0);
            colorCircle.setLayoutParams(params);
            colorCircle.setBackgroundResource(R.drawable.circle_shape);

            // Teñir el drawable circular con el color del estado.
            colorCircle.getBackground().setTint(color);

            // 2. Texto del Estado
            TextView statusText = new TextView(this);
            statusText.setText(status.toUpperCase());
            statusText.setTextColor(ContextCompat.getColor(this, R.color.text_primary_color));
            statusText.setTextSize(12);

            // 3. Indicador de Filtro (Resaltar el filtro activo)
            if (currentFilterStatus.equalsIgnoreCase(status) ||
                    (currentFilterStatus.equals("all_active") && Arrays.asList("EN VIVO", "CONFIRMADO", "PENDIENTE").contains(status))) {

                statusText.setText(statusText.getText() + (currentFilterStatus.equals("all_active") ? " (Activo)" : " (FILTRO)"));
                statusText.setTypeface(null, android.graphics.Typeface.BOLD);
                statusText.setTextColor(color);
            }

            layout.addView(colorCircle);
            layout.addView(statusText);
            tvLegendContainer.addView(layout);
        }
    }


    private void loadActiveEventsOnMap(String filterStatus) {
        mMap.clear();
        Query query;

        // FIX: Incluir el estado EN VIVO en la consulta inicial
        if ("all_active".equals(filterStatus)) {
            query = db.collection("events").whereIn("status", Arrays.asList("PENDIENTE", "CONFIRMADO", "EN VIVO"));
        } else {
            // Filtro por estado específico
            query = db.collection("events").whereEqualTo("status", filterStatus);
        }


        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    int eventCount = 0;
                    LatLng initialLocation = null;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        EventModel event = doc.toObject(EventModel.class);

                        if (event != null && event.getLatitude() != 0.0 && event.getLongitude() != 0.0) {
                            LatLng location = new LatLng(event.getLatitude(), event.getLongitude());

                            String status = event.getStatus();
                            float markerColor = STATUS_COLORS.getOrDefault(status, BitmapDescriptorFactory.HUE_ORANGE);

                            // Determinar el título y el snippet del marcador
                            String title = event.getTitle();
                            String snippet = status;
                            if (event.isFull()) {
                                snippet += " (LLENO)"; // Mantenemos LLENO en el snippet como dato de capacidad
                            }

                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(title)
                                    .snippet(snippet)
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                            if (initialLocation == null) {
                                initialLocation = location;
                            }
                            eventCount++;
                        }
                    }

                    if (eventCount > 0) {
                        Toast.makeText(this, eventCount + " eventos mostrados (" + filterStatus.toUpperCase() + ").", Toast.LENGTH_SHORT).show();
                        // Solo movemos la cámara a la ubicación inicial si es la primera carga (para ver los marcadores)
                        if (filterStatus.equals("all_active") && initialLocation != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12f));
                        }
                    } else {
                        Toast.makeText(this, "No hay eventos bajo el filtro actual: " + filterStatus.toUpperCase(), Toast.LENGTH_LONG).show();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(19.4326, -99.1332), 10f)); // CDMX
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando eventos para el mapa: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar datos del mapa.", Toast.LENGTH_SHORT).show();
                });

        displayLegend();
    }
}
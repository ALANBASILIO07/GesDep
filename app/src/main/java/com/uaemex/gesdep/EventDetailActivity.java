package com.uaemex.gesdep;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // <--- ESTA ERA LA LÍNEA QUE FALTABA
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EventModel event;

    // UI Components
    private TextView tvTitle, tvDate, tvTime, tvDesc, tvLocation;
    private TextView tvCostAmount, tvCostConcept;
    private ImageView ivBanner;
    private Chip chipCategory, chipDiscipline, chipCost;
    private LinearLayout layoutCost;
    private MaterialButton btnAction;
    private FloatingActionButton fabEdit;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        // Recibir evento (Serializable)
        event = (EventModel) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Error al cargar evento", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupData();
        setupMap();
        checkRole();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tvEventTitle);
        tvDate = findViewById(R.id.tvEventDate);
        tvTime = findViewById(R.id.tvEventTime);
        tvDesc = findViewById(R.id.tvEventDescription);
        tvLocation = findViewById(R.id.tvEventLocation);

        tvCostAmount = findViewById(R.id.tvCostAmount);
        tvCostConcept = findViewById(R.id.tvCostConcept);
        layoutCost = findViewById(R.id.layoutCost);

        ivBanner = findViewById(R.id.ivEventBanner);
        chipCategory = findViewById(R.id.chipCategory);
        chipDiscipline = findViewById(R.id.chipDiscipline);
        chipCost = findViewById(R.id.chipCost);

        btnAction = findViewById(R.id.btnAction);
        fabEdit = findViewById(R.id.fabEditEvent);
    }

    private void setupData() {
        tvTitle.setText(event.getTitle());
        tvDesc.setText(event.getDescription());
        tvLocation.setText(event.getPlaceName());
        chipCategory.setText(event.getCategory());
        chipDiscipline.setText(event.getDiscipline());

        // Formatear Fechas
        SimpleDateFormat dateSdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("es", "MX"));
        SimpleDateFormat timeSdf = new SimpleDateFormat("h:mm a", new Locale("es", "MX"));

        if (event.getStartTime() != null) {
            tvDate.setText(dateSdf.format(event.getStartTime()));
            String start = timeSdf.format(event.getStartTime());
            String end = (event.getEndTime() != null) ? timeSdf.format(event.getEndTime()) : "?";
            tvTime.setText(start + " - " + end);
        } else if (event.getEventDateTime() != null) {
            tvDate.setText(dateSdf.format(event.getEventDateTime()));
            tvTime.setText(timeSdf.format(event.getEventDateTime()));
        }

        // Costos
        if (event.isPaid()) {
            chipCost.setText("De Pago");
            layoutCost.setVisibility(View.VISIBLE);
            tvCostAmount.setText(String.format("$%.2f MXN", event.getCost()));
            tvCostConcept.setText(event.getPaymentConcept());
        } else {
            chipCost.setText("Gratuito");
            layoutCost.setVisibility(View.GONE);
        }

        // Imagen
        if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
            Glide.with(this)
                    .load(event.getImageUrls().get(0))
                    .centerCrop()
                    .placeholder(R.drawable.ic_calendar)
                    .into(ivBanner);
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapLite);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(location).title(event.getPlaceName()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));

        googleMap.setOnMapClickListener(latLng -> {
            Uri gmmIntentUri = Uri.parse("geo:" + event.getLatitude() + "," + event.getLongitude() + "?q=" + Uri.encode(event.getPlaceName()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });
    }

    private void checkRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return; // Seguridad

        String uid = user.getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String role = doc.getString("role");

                if ("admin".equals(role)) {
                    // VISTA DE ADMIN
                    fabEdit.setVisibility(View.VISIBLE);
                    btnAction.setText("VER LISTA DE INSCRITOS");
                    btnAction.setBackgroundColor(getColor(R.color.blue_button));

                    fabEdit.setOnClickListener(v -> Toast.makeText(this, "Editar evento", Toast.LENGTH_SHORT).show());
                } else {
                    // VISTA DE USUARIO
                    fabEdit.setVisibility(View.GONE);
                    setupUserActions(uid);
                }
            }
        });
    }

    private void setupUserActions(String uid) {
        btnAction.setText("INSCRIBIRME AHORA");
        btnAction.setOnClickListener(v -> Toast.makeText(this, "Procesando inscripción...", Toast.LENGTH_SHORT).show());
    }
}
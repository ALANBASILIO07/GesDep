package com.uaemex.gesdep;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.repositories.EventRepository;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ActivityEventDetail extends AppCompatActivity implements OnMapReadyCallback {

    // Vistas
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView tvEventTitle, tvEventDate, tvEventTime, tvCostAmount, tvCostConcept, tvEventDescription, tvEventLocation, tvParticipantsCount;
    private Chip chipCategory, chipDiscipline, chipStatus, chipCost;
    private ImageView ivEventBanner;
    private ProgressBar pbQuota;
    private FloatingActionButton fabEditEvent;
    private MaterialButton btnAction;

    // Variables
    private EventModel currentEvent;
    private EventRepository eventRepository;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String eventId;
    private GoogleMap mMap;

    // --- CAMBIO CLAVE: Launcher para recibir datos actualizados ---
    private final ActivityResultLauncher<Intent> editEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Recuperar el evento actualizado que nos mandó EditEventActivity
                    EventModel updatedEvent = (EventModel) result.getData().getSerializableExtra("updatedEvent");

                    if (updatedEvent != null) {
                        this.currentEvent = updatedEvent;

                        // Refrescamos UI y Mapa con los nuevos datos sin llamar a Firebase
                        updateUI(this.currentEvent);
                        updateMapLocation(this.currentEvent);

                        Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    // -------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Efecto Pantalla Completa
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_event_detail);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");
        eventRepository = new EventRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapLite);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        if (getIntent().hasExtra("eventModel")) {
            currentEvent = (EventModel) getIntent().getSerializableExtra("eventModel");
            if (currentEvent != null) {
                eventId = currentEvent.getId();
                updateUI(currentEvent);
                checkUserStatus();
            }
        } else if (getIntent().hasExtra("eventId")) {
            eventId = getIntent().getStringExtra("eventId");
            loadEventFromFirebase(eventId);
        } else {
            Toast.makeText(this, "Error: Evento no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnAction.setOnClickListener(v -> handleInscription());

        // Acción Editar
        fabEditEvent.setOnClickListener(v -> {
            if (currentEvent != null) {
                Intent intent = new Intent(ActivityEventDetail.this, EditEventActivity.class);
                intent.putExtra("eventModel", currentEvent);

                // --- CAMBIO CLAVE: Usamos el launcher en vez de startActivity ---
                editEventLauncher.launch(intent);
                // ---------------------------------------------------------------
            }
        });
    }

    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvCostAmount = findViewById(R.id.tvCostAmount);
        tvCostConcept = findViewById(R.id.tvCostConcept);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        chipCategory = findViewById(R.id.chipCategory);
        chipDiscipline = findViewById(R.id.chipDiscipline);
        chipStatus = findViewById(R.id.chipStatus);
        chipCost = findViewById(R.id.chipCost);
        ivEventBanner = findViewById(R.id.ivEventBanner);
        tvParticipantsCount = findViewById(R.id.tvParticipantsCount);
        pbQuota = findViewById(R.id.pbQuota);
        fabEditEvent = findViewById(R.id.fabEditEvent);
        btnAction = findViewById(R.id.btnAction);
    }

    private void checkUserStatus() {
        if (auth.getCurrentUser() == null || currentEvent == null) return;
        btnAction.setEnabled(false);
        btnAction.setText("Verificando...");

        eventRepository.checkUserRegistration(currentEvent.getId(), auth.getCurrentUser().getUid(), new EventRepository.OnCheckRegistrationListener() {
            @Override
            public void onResult(boolean isRegistered) {
                if (isRegistered) {
                    configureButtonAsRegistered();
                } else {
                    configureButtonAsAvailable();
                }
            }
            @Override
            public void onError(String error) {
                btnAction.setText("Error");
            }
        });
    }

    private void handleInscription() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAction.setEnabled(false);
        btnAction.setText("Inscribiendo...");

        String userId = auth.getCurrentUser().getUid();
        String userName = auth.getCurrentUser().getDisplayName() != null ? auth.getCurrentUser().getDisplayName() : auth.getCurrentUser().getEmail();

        eventRepository.registerUserToEvent(currentEvent.getId(), userId, userName, new EventRepository.OnRegistrationResultListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ActivityEventDetail.this, "¡Inscripción Exitosa!", Toast.LENGTH_LONG).show();
                configureButtonAsRegistered();
                currentEvent.setCurrentParticipants(currentEvent.getCurrentParticipants() + 1);
                updateQuotaUI(currentEvent.getCurrentParticipants(), currentEvent.getMaxQuota());
            }

            @Override
            public void onEventFull() {
                Toast.makeText(ActivityEventDetail.this, "Lo sentimos, el evento está lleno", Toast.LENGTH_LONG).show();
                btnAction.setText("CUPO LLENO");
                btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            }

            @Override
            public void onAlreadyRegistered() {
                Toast.makeText(ActivityEventDetail.this, "Ya estabas inscrito", Toast.LENGTH_SHORT).show();
                configureButtonAsRegistered();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ActivityEventDetail.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                configureButtonAsAvailable();
            }
        });
    }

    private void configureButtonAsRegistered() {
        btnAction.setText("YA ESTÁS INSCRITO");
        btnAction.setIconResource(R.drawable.ic_check);
        btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        btnAction.setEnabled(false);
    }

    private void configureButtonAsAvailable() {
        btnAction.setText("INSCRIBIRME");
        btnAction.setIcon(null);
        btnAction.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_primary)));
        btnAction.setEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        if (currentEvent != null) updateMapLocation(currentEvent);
    }

    private void loadEventFromFirebase(String id) {
        db.collection("events").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEvent = documentSnapshot.toObject(EventModel.class);
                        if (currentEvent != null) {
                            currentEvent.setId(documentSnapshot.getId());
                            updateUI(currentEvent);
                            checkUserStatus();
                        }
                    }
                });
    }

    private void updateUI(EventModel event) {
        tvEventTitle.setText(event.getTitle());
        collapsingToolbar.setTitle(event.getTitle());
        tvEventDescription.setText(event.getDescription());
        tvEventLocation.setText(event.getPlaceName());
        chipCategory.setText(event.getCategory());

        if (event.getDiscipline() != null && !event.getDiscipline().isEmpty()) {
            chipDiscipline.setText(event.getDiscipline());
            chipDiscipline.setVisibility(View.VISIBLE);
        } else {
            chipDiscipline.setVisibility(View.GONE);
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, d MMMM", new Locale("es", "MX"));
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        if (event.getEventDateTime() != null) tvEventDate.setText(sdfDate.format(event.getEventDateTime()));
        if (event.getStartTime() != null && event.getEndTime() != null) {
            tvEventTime.setText(sdfTime.format(event.getStartTime()) + " - " + sdfTime.format(event.getEndTime()));
        }

        if (event.isPaid()) {
            tvCostAmount.setText(String.format("$%.2f MXN", event.getCost()));
            tvCostConcept.setText(event.getPaymentConcept());
            tvCostConcept.setVisibility(View.VISIBLE);
            chipCost.setText("Costo");
        } else {
            tvCostAmount.setText("GRATUITO");
            tvCostConcept.setVisibility(View.GONE);
            chipCost.setText("Gratuito");
        }

        updateQuotaUI(event.getCurrentParticipants(), event.getMaxQuota());

        String status = event.getTimeStatus();
        chipStatus.setText(status);

        if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
            Glide.with(this).load(event.getImageUrls().get(0)).centerCrop().into(ivEventBanner);
        }

        updateMapLocation(event);
    }

    private void updateQuotaUI(int current, int max) {
        tvParticipantsCount.setText(current + " / " + max + " inscritos");
        pbQuota.setMax(max);
        pbQuota.setProgress(current);
        if (max > 0 && (double) current / max > 0.9) {
            pbQuota.setProgressTintList(ColorStateList.valueOf(Color.RED));
        } else {
            pbQuota.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_primary)));
        }
    }

    private void updateMapLocation(EventModel event) {
        if (mMap != null && event.getLatitude() != 0) {
            LatLng loc = new LatLng(event.getLatitude(), event.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(loc).title(event.getPlaceName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f));
        }
    }
}
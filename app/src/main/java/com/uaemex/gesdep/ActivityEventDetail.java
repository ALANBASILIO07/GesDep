package com.uaemex.gesdep;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log; // Agregado para logs de diagnóstico
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment; // <<-- FIX CLAVE: IMPORTACIÓN AGREGADA

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.repositories.EventRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    // Galería
    private TextView tvGalleryLabel;
    private RecyclerView rvEventGallery;

    // Variables
    private EventModel currentEvent;
    private EventRepository eventRepository;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String eventId;
    private GoogleMap mMap;
    private boolean isAdmin = false;
    private boolean isRegistered = false;
    private final String TAG = "EventDetail";

    private final ActivityResultLauncher<Intent> editEventLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    EventModel updatedEvent = (EventModel) result.getData().getSerializableExtra("updatedEvent");
                    if (updatedEvent != null) {
                        this.currentEvent = updatedEvent;
                        updateUI(this.currentEvent);
                        updateMapLocation(this.currentEvent);
                        checkUserStatus();
                        invalidateOptionsMenu();
                        Toast.makeText(this, "Información actualizada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        checkUserRole();
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
            finish();
        }

        btnAction.setOnClickListener(v -> {
            if (isRegistered) {
                handleCancellation();
            } else {
                handleInscription();
            }
        });

        fabEditEvent.setOnClickListener(v -> {
            if (currentEvent != null) {
                Intent intent = new Intent(ActivityEventDetail.this, EditEventActivity.class);
                intent.putExtra("eventModel", currentEvent);
                editEventLauncher.launch(intent);
            }
        });

        pbQuota.setOnClickListener(v -> showParticipantsDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga para asegurar que el estado de la DB se refleje (ej: si otro admin lo cambió)
        if (eventId != null) {
            loadEventFromFirebase(eventId);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        if (currentEvent != null) {
            updateMapLocation(currentEvent);
        }
    }

    private void checkUserRole() {
        if (auth.getCurrentUser() != null) {
            db.collection("users").document(auth.getCurrentUser().getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && "admin".equals(doc.getString("role"))) {
                            isAdmin = true;
                            invalidateOptionsMenu();
                            fabEditEvent.setVisibility(View.VISIBLE);
                        } else {
                            isAdmin = false;
                            fabEditEvent.setVisibility(View.GONE);
                        }
                    });
        }
    }

    // --- GESTIÓN DE MENÚ (ADMIN) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
        MenuItem itemSwitch = menu.findItem(R.id.action_visibility);
        MenuItem itemCancel = menu.findItem(R.id.action_cancel_event);

        itemSwitch.setVisible(isAdmin);
        itemCancel.setVisible(isAdmin);

        if (isAdmin && itemSwitch.getActionView() != null) {
            SwitchMaterial switchVisibility = itemSwitch.getActionView().findViewById(R.id.switchVisibility);
            if (currentEvent != null) {
                switchVisibility.setChecked(currentEvent.isVisible());
                updateSwitchText(switchVisibility);
            }
            // CORRECCIÓN APLICADA: Cambiar 'switchView' por 'switchVisibility'
            switchVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> updateEventVisibility(isChecked, switchVisibility));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_cancel_event);
        if (currentEvent != null && item != null) {
            if ("CANCELADO".equals(currentEvent.getStatus())) {
                item.setTitle("Reactivar Evento");
            } else {
                item.setTitle("Cancelar Evento");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_cancel_event) {
            toggleEventCancellationStatus();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSwitchText(SwitchMaterial switchMaterial) {
        switchMaterial.setText(switchMaterial.isChecked() ? "Visible " : "Oculto ");
    }

    private void updateEventVisibility(boolean isVisible, SwitchMaterial switchView) {
        if (currentEvent == null) return;
        switchView.setEnabled(false);
        db.collection("events").document(currentEvent.getId())
                .update("visible", isVisible)
                .addOnSuccessListener(aVoid -> {
                    currentEvent.setVisible(isVisible);
                    updateSwitchText(switchView);
                    switchView.setEnabled(true);
                    Toast.makeText(this, isVisible ? "Evento visible" : "Evento oculto", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    switchView.setChecked(!isVisible);
                    switchView.setEnabled(true);
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleEventCancellationStatus() {
        if (currentEvent == null) return;
        String newStatus;
        boolean isCancelling = !"CANCELADO".equals(currentEvent.getStatus());

        if (isCancelling) {
            newStatus = "CANCELADO";
            // Notificar cancelación (Asumimos que el NotificationHelper ya existe)
            // notificationHelper.notifyEventCancelled(currentEvent.getId(), currentEvent.getTitle(), "Cancelación manual por Administrador.");
        } else {
            // Lógica de Reactivación: CONFIRMADO si cumple aforo, PENDIENTE si falta
            if (currentEvent.getCurrentParticipants() >= currentEvent.getMinQuota()) {
                newStatus = "CONFIRMADO";
            } else {
                newStatus = "PENDIENTE";
            }
        }

        db.collection("events").document(currentEvent.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    loadEventFromFirebase(eventId);
                    invalidateOptionsMenu();
                    Toast.makeText(this, "Estatus cambiado a: " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        tvEventTitle = findViewById(R.id.tvEventTitle);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime); // ID CORREGIDO
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

        tvGalleryLabel = findViewById(R.id.tvGalleryLabel);
        rvEventGallery = findViewById(R.id.rvEventGallery);
        rvEventGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    // --- PARTICIPANTES ---
    private void showParticipantsDialog() {
        if (currentEvent == null) return;
        ParticipantsSheet sheet = new ParticipantsSheet(currentEvent.getId(), isAdmin);
        sheet.show(getSupportFragmentManager(), "ParticipantsSheet");
    }

    // --- LÓGICA DE INSCRIPCIÓN Y PAGOS ---
    private void checkUserStatus() {
        if (auth.getCurrentUser() == null || currentEvent == null) return;
        btnAction.setEnabled(false);
        btnAction.setText("Verificando...");
        eventRepository.checkUserRegistration(currentEvent.getId(), auth.getCurrentUser().getUid(), new EventRepository.OnCheckRegistrationListener() {
            @Override
            public void onResult(boolean registered) {
                isRegistered = registered;
                if (isRegistered) configureButtonAsRegistered();
                else configureButtonAsAvailable();
            }
            @Override public void onError(String error) {}
        });
    }

    private void handleInscription() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }
        // Fix 1: No permitir inscripción si está Cancelado
        if ("CANCELADO".equals(currentEvent.getStatus())) {
            Toast.makeText(this, "El evento ha sido CANCELADO. No se permiten inscripciones.", Toast.LENGTH_SHORT).show();
            configureButtonAsAvailable();
            return;
        }

        if (currentEvent.isPaid() && currentEvent.getCost() > 0) {
            showPaymentDialog(currentEvent.getCost());
        } else {
            processRegistration(0); // Gratis
        }
    }

    private void handleCancellation() {
        new AlertDialog.Builder(this)
                .setTitle("¿Cancelar Asistencia?")
                .setMessage(currentEvent.isPaid() ?
                        "Se te reembolsarán $" + currentEvent.getCost() + " a tu crédito de la app." :
                        "¿Seguro que deseas liberar tu lugar?")
                .setPositiveButton("Sí, Cancelar", (dialog, which) -> {
                    btnAction.setEnabled(false);
                    btnAction.setText("Procesando...");
                    double refund = currentEvent.isPaid() ? currentEvent.getCost() : 0.0;
                    String userId = auth.getCurrentUser().getUid();

                    eventRepository.cancelRegistration(currentEvent.getId(), userId, refund, new EventRepository.OnRegistrationResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ActivityEventDetail.this, "Asistencia Cancelada.", Toast.LENGTH_LONG).show();
                            loadEventFromFirebase(eventId); // Forzar la recarga
                        }
                        @Override public void onEventFull() {}
                        @Override public void onAlreadyRegistered() {}
                        @Override public void onError(String error) {
                            Toast.makeText(ActivityEventDetail.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            configureButtonAsRegistered();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showPaymentDialog(double amountRequired) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment_simulation, null);
        builder.setView(dialogView);

        TextView tvAmount = dialogView.findViewById(R.id.tvRequiredAmount);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etPaymentAmount);
        MaterialButton btnPay = dialogView.findViewById(R.id.btnConfirmPayment);

        String concept = currentEvent.getPaymentConcept() != null ? currentEvent.getPaymentConcept() : "Pago";
        tvAmount.setText(String.format("Total a pagar: $%.2f MXN", amountRequired));

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnPay.setOnClickListener(v -> {
            String input = etAmount.getText().toString().trim();
            if (input.isEmpty()) {
                etAmount.setError("Ingresa el monto");
                return;
            }
            try {
                double amountEntered = Double.parseDouble(input);
                if (amountEntered < amountRequired) {
                    Toast.makeText(this, "Monto insuficiente.", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();

                    // FIX DE PAGO EXCESIVO: Informar al usuario sobre el cargo exacto
                    if (amountEntered > amountRequired) {
                        Toast.makeText(this, "Solo se cargaron $"+ amountRequired +" MXN. El exceso fue ignorado.", Toast.LENGTH_LONG).show();
                    }
                    // Siempre pasamos el costo exacto del evento al registro.
                    processRegistration(amountRequired);
                }
            } catch (NumberFormatException e) {
                etAmount.setError("Monto inválido");
            }
        });
        dialog.show();
    }

    private void processRegistration(double cost) {
        btnAction.setEnabled(false);
        btnAction.setText("Procesando...");
        String userId = auth.getCurrentUser().getUid();

        EventRepository.OnRegistrationResultListener listener = new EventRepository.OnRegistrationResultListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ActivityEventDetail.this, "¡Inscripción Exitosa!", Toast.LENGTH_LONG).show();
                loadEventFromFirebase(eventId); // Forzar la recarga
            }
            @Override
            public void onEventFull() {
                Toast.makeText(ActivityEventDetail.this, "Evento lleno", Toast.LENGTH_LONG).show();
                btnAction.setText("CUPO LLENO");
            }
            @Override
            public void onAlreadyRegistered() {
                isRegistered = true;
                configureButtonAsRegistered();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(ActivityEventDetail.this, error, Toast.LENGTH_SHORT).show();
                configureButtonAsAvailable();
            }
        };

        // Llama al Repositorio sin el parámetro 'userName'
        if (cost > 0) {
            eventRepository.processPaymentAndRegister(currentEvent.getId(), userId, cost, listener);
        } else {
            eventRepository.registerUserToEvent(currentEvent.getId(), userId, listener);
        }
    }

    private void configureButtonAsRegistered() {
        btnAction.setText("CANCELAR ASISTENCIA");
        btnAction.setIconResource(R.drawable.ic_close);
        btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        btnAction.setEnabled(true);
    }

    private void configureButtonAsAvailable() {
        // Deshabilitar si está CANCELADO o FINALIZADO
        if (currentEvent.getStatus() != null &&
                (currentEvent.getStatus().equalsIgnoreCase("CANCELADO") || currentEvent.getTimeStatus().equalsIgnoreCase("FINALIZADO"))) {
            btnAction.setText(currentEvent.getStatus().toUpperCase());
            btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            btnAction.setEnabled(false);
            return;
        }

        if (currentEvent != null && currentEvent.isPaid()) {
            btnAction.setText("PAGAR $" + currentEvent.getCost() + " E INSCRIBIRME");
        } else {
            btnAction.setText("INSCRIBIRME");
        }
        btnAction.setIcon(null);
        btnAction.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_primary)));
        btnAction.setEnabled(true);
    }

    // --- CARGA DE DATOS Y MAPA ---
    private void loadEventFromFirebase(String id) {
        db.collection("events").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentEvent = doc.toObject(EventModel.class);
                if (currentEvent != null) {
                    currentEvent.setId(doc.getId());
                    updateUI(currentEvent);
                    checkUserStatus();
                    invalidateOptionsMenu();
                }
            }
        });
    }

    private void updateUI(EventModel event) {
        String dbStatus = event.getStatus();
        String timeStatus = event.getTimeStatus(); // Calculado por el modelo

        // -----------------------------------------------------------
        // 💥 FIX CLAVE: PERSISTENCIA DEL ESTADO EN VIVO EN LA DB
        // -----------------------------------------------------------
        if (timeStatus.equals("EN VIVO") && !dbStatus.equals("EN VIVO") && !dbStatus.equals("CANCELADO")) {
            // El evento está EN VIVO según el reloj, pero la DB lo marca como CONFIRMADO/PENDIENTE.
            Log.d(TAG, "Forzando actualización de STATUS a EN VIVO en DB.");

            db.collection("events").document(event.getId())
                    .update("status", "EN VIVO")
                    .addOnSuccessListener(aVoid -> {
                        // Si tiene éxito, actualizamos el modelo local para reflejar el cambio inmediatamente
                        event.setStatus("EN VIVO");
                        Log.d(TAG, "Estado de DB actualizado a EN VIVO.");
                        // Re-ejecutar updateUI para reflejar el cambio de estatus en los chips
                        updateUI(event);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al actualizar estado a EN VIVO en DB: " + e.getMessage());
                    });

            // Usamos el estado ACTUAL (aún no persistido) para no bloquear la UI.
            dbStatus = "EN VIVO";
        }

        // -----------------------------------------------------------
        // LÓGICA DE ACTUALIZACIÓN DE CHIPS
        // -----------------------------------------------------------

        // El estado a mostrar es el estado de la DB (que ahora se actualiza si es EN VIVO)
        String statusToDisplay = dbStatus;

        ColorStateList statusColor;

        if ("EN VIVO".equalsIgnoreCase(statusToDisplay)) {
            chipStatus.setText("EN VIVO");
            // Usamos un color fijo para EN VIVO (verde fuerte)
            statusColor = ColorStateList.valueOf(Color.parseColor("#4CAF50")); // Verde de Android
        } else if ("CONFIRMADO".equalsIgnoreCase(statusToDisplay)) {
            chipStatus.setText("CONFIRMADO");
            statusColor = ColorStateList.valueOf(Color.parseColor("#00E676"));
        } else if ("CANCELADO".equalsIgnoreCase(statusToDisplay)) {
            chipStatus.setText("CANCELADO");
            statusColor = ColorStateList.valueOf(Color.RED);
        } else if ("FINALIZADO".equalsIgnoreCase(timeStatus)) {
            // Si no fue marcado como EN VIVO, usamos el status de tiempo.
            chipStatus.setText("FINALIZADO");
            statusColor = ColorStateList.valueOf(Color.GRAY);
        } else {
            // PENDIENTE (ACTIVO ha sido eliminado)
            chipStatus.setText(statusToDisplay.toUpperCase());
            statusColor = ColorStateList.valueOf(Color.parseColor("#2196F3"));
        }

        chipStatus.setChipBackgroundColor(statusColor);


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

        if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
            Glide.with(this).load(event.getImageUrls().get(0)).centerCrop().into(ivEventBanner);

            if (event.getImageUrls().size() > 1) {
                tvGalleryLabel.setVisibility(View.VISIBLE);
                rvEventGallery.setVisibility(View.VISIBLE);
                List<String> galleryUrls = new ArrayList<>(event.getImageUrls().subList(1, event.getImageUrls().size()));
                rvEventGallery.setAdapter(new GalleryDetailAdapter(galleryUrls));
            } else {
                tvGalleryLabel.setVisibility(View.GONE);
                rvEventGallery.setVisibility(View.GONE);
            }
        } else {
            tvGalleryLabel.setVisibility(View.GONE);
            rvEventGallery.setVisibility(View.GONE);
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

    // Adaptador de Galería
    class GalleryDetailAdapter extends RecyclerView.Adapter<GalleryDetailAdapter.ViewHolder> {
        private List<String> urls;
        public GalleryDetailAdapter(List<String> urls) { this.urls = urls; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(8, 0, 8, 0);
            return new ViewHolder(iv);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(urls.get(position)).into((ImageView) holder.itemView);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityEventDetail.this, GalleryActivity.class);
                intent.putStringArrayListExtra("images", new ArrayList<>(urls));
                intent.putExtra("position", position);
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return urls.size(); }
        class ViewHolder extends RecyclerView.ViewHolder { public ViewHolder(View v) { super(v); } }
    }
}
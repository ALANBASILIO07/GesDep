package com.uaemex.gesdep;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log; // Agregado para logs de diagnóstico

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.models.VenueModel;
import com.uaemex.gesdep.utils.NotificationHelper;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EditEventActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etMacroEvent, etDescription, etMinQuota, etMaxQuota;
    private TextInputEditText etDate, etStartTime, etEndTime, etDeadlineDate;
    private SwitchMaterial switchPaidEvent;
    private LinearLayout containerPaymentDetails;
    private AutoCompleteTextView dropdownCoach, dropdownRegistrationType;
    private AutoCompleteTextView dropdownPaymentType, dropdownCategory, dropdownDiscipline, dropdownModality, dropdownVenue;
    private TextInputEditText etCost;
    private MaterialButton btnSaveChanges, btnAddPhotos;
    private MaterialCardView btnSelectImage;
    private ImageView ivEventImage;
    private LinearLayout layoutImagePlaceholder;
    private View loadingOverlay;
    private RecyclerView rvEventImages;

    private Calendar calendarEventDate = Calendar.getInstance();
    private Calendar calendarStartTime = Calendar.getInstance();
    private Calendar calendarEndTime = Calendar.getInstance();
    private Calendar calendarDeadline = Calendar.getInstance();

    private List<VenueModel> venueList = new ArrayList<>();
    private VenueModel selectedVenue = null;
    // Coach data
    private List<DocumentSnapshot> coachList = new ArrayList<>();
    private DocumentSnapshot selectedCoach = null;
    private EventModel currentEvent;
    private String originalVenueName;
    private Date originalStartTime;

    private Uri newMainImageUri = null;
    private List<String> currentImageUrls = new ArrayList<>();
    private List<Object> galleryItems = new ArrayList<>(); // Para mezcla de String (URL) y Uri (Local)
    private GalleryEditAdapter galleryAdapter;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private NotificationHelper notificationHelper;

    private final ActivityResultLauncher<Intent> bannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    newMainImageUri = result.getData().getData();
                    Glide.with(this).load(newMainImageUri).centerCrop().into(ivEventImage);
                    layoutImagePlaceholder.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<Intent> galleryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            if (galleryItems.size() < 5) galleryItems.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        if (galleryItems.size() < 5) galleryItems.add(result.getData().getData());
                    }
                    galleryAdapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        WindowUtils.setGreenStatusBar(this);

        db = FirebaseFirestore.getInstance("gesdep");
        storage = FirebaseStorage.getInstance();
        notificationHelper = new NotificationHelper();

        initViews();
        setupDropdowns();
        setupListeners();
        setupRecyclerView();

        if (getIntent().hasExtra("eventModel")) {
            currentEvent = (EventModel) getIntent().getSerializableExtra("eventModel");
            if (currentEvent != null) {
                originalVenueName = currentEvent.getPlaceName();
                originalStartTime = currentEvent.getStartTime();
                checkEmergencyLock();
                loadVenuesAndFillData();
            }
        } else {
            finish();
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etMacroEvent = findViewById(R.id.etMacroEvent);
        etDescription = findViewById(R.id.etDescription);
        etMinQuota = findViewById(R.id.etMinQuota);
        etMaxQuota = findViewById(R.id.etMaxQuota);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        etDeadlineDate = findViewById(R.id.etDeadlineDate);
        dropdownCategory = findViewById(R.id.dropdownCategory);
        dropdownDiscipline = findViewById(R.id.dropdownDiscipline);
        dropdownModality = findViewById(R.id.dropdownModality);
        dropdownVenue = findViewById(R.id.dropdownVenue);
        switchPaidEvent = findViewById(R.id.switchPaidEvent);
        containerPaymentDetails = findViewById(R.id.containerPaymentDetails);
        dropdownPaymentType = findViewById(R.id.dropdownPaymentType);
        etCost = findViewById(R.id.etCost);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivEventImage = findViewById(R.id.ivEventImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        rvEventImages = findViewById(R.id.rvEventImages);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        dropdownCoach = findViewById(R.id.dropdownCoach);
        dropdownRegistrationType = findViewById(R.id.dropdownRegistrationType);
    }

    private void setupRecyclerView() {
        galleryAdapter = new GalleryEditAdapter(galleryItems);
        rvEventImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvEventImages.setAdapter(galleryAdapter);
    }

    private void checkEmergencyLock() {
        long now = System.currentTimeMillis();
        long eventStart = currentEvent.getStartTime() != null ? currentEvent.getStartTime().getTime() : 0;
        if (eventStart > now && (eventStart - now) < 3600000) {
            disableFormInputs();
            Toast.makeText(this, "⚠️ A menos de 1h del evento, la edición está bloqueada.", Toast.LENGTH_LONG).show();
        }
    }

    private void disableFormInputs() {
        etTitle.setEnabled(false);
        etDescription.setEnabled(false);
        dropdownVenue.setEnabled(false);
        etDate.setEnabled(false);
        etStartTime.setEnabled(false);
        dropdownCategory.setEnabled(false);
    }

    private void setupDropdowns() {
        setAdapterFromResource(dropdownCategory, R.array.event_categories);
        setAdapterFromResource(dropdownDiscipline, R.array.event_disciplines);
        setAdapterFromResource(dropdownModality, R.array.event_modalities);
        setAdapterFromResource(dropdownPaymentType, R.array.event_payment_types);
        setAdapterFromResource(dropdownRegistrationType, R.array.event_registration_types);
    }

    private void setAdapterFromResource(AutoCompleteTextView dropdown, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayResId, android.R.layout.simple_dropdown_item_1line);
        dropdown.setAdapter(adapter);
    }

    private void loadVenuesAndFillData() {
        db.collection("venues").get().addOnSuccessListener(queryDocumentSnapshots -> {
            venueList.clear();
            List<String> venueNames = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                VenueModel venue = doc.toObject(VenueModel.class);
                if (venue != null) {
                    venue.setId(doc.getId());
                    venueList.add(venue);
                    venueNames.add(venue.getName());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, venueNames);
            dropdownVenue.setAdapter(adapter);
            fillFormWithEventData();
            loadCoaches();
        });
    }

    private void loadCoaches() {
        db.collection("users").whereEqualTo("role", "coach").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    coachList.clear();
                    List<String> coachNames = new ArrayList<>();
                    coachNames.add("-- Sin asignar --");
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        coachList.add(doc);
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            coachNames.add(name);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, coachNames);
                    dropdownCoach.setAdapter(adapter);
                    fillCoachData();
                });
    }

    private void fillCoachData() {
        if (currentEvent != null && currentEvent.getCoachId() != null && !currentEvent.getCoachId().isEmpty()) {
            dropdownCoach.setText(currentEvent.getCoachName(), false);
            for (int i = 0; i < coachList.size(); i++) {
                if (coachList.get(i).getId().equals(currentEvent.getCoachId())) {
                    selectedCoach = coachList.get(i);
                    break;
                }
            }
        } else {
            dropdownCoach.setText("-- Sin asignar --", false);
        }

        // Fill registration type
        String regType = currentEvent != null ? currentEvent.getRegistrationType() : "individual";
        if ("individual".equals(regType)) {
            dropdownRegistrationType.setText("Individual", false);
        } else if ("team".equals(regType)) {
            dropdownRegistrationType.setText("Por Equipo", false);
        } else if ("both".equals(regType)) {
            dropdownRegistrationType.setText("Ambos", false);
        } else {
            dropdownRegistrationType.setText("Individual", false);
        }
    }

    private void fillFormWithEventData() {
        if (currentEvent == null) return;

        etTitle.setText(currentEvent.getTitle());
        etMacroEvent.setText(currentEvent.getMacroEvent());
        etDescription.setText(currentEvent.getDescription());
        etMinQuota.setText(String.valueOf(currentEvent.getMinQuota()));
        etMaxQuota.setText(String.valueOf(currentEvent.getMaxQuota()));
        dropdownCategory.setText(currentEvent.getCategory(), false);
        dropdownDiscipline.setText(currentEvent.getDiscipline(), false);
        dropdownModality.setText(currentEvent.getModality(), false);
        dropdownVenue.setText(currentEvent.getPlaceName(), false);

        for (VenueModel v : venueList) {
            if (v.getName().equals(currentEvent.getPlaceName())) {
                selectedVenue = v;
                break;
            }
        }

        if (currentEvent.getStartTime() != null) {
            calendarEventDate.setTime(currentEvent.getStartTime());
            calendarStartTime.setTime(currentEvent.getStartTime());
            updateDateText(etDate, calendarEventDate);
            updateTimeText(etStartTime, calendarStartTime);
        }
        if (currentEvent.getEndTime() != null) {
            calendarEndTime.setTime(currentEvent.getEndTime());
            updateTimeText(etEndTime, calendarEndTime);
        }
        if (currentEvent.getRegistrationDeadline() != null) {
            calendarDeadline.setTime(currentEvent.getRegistrationDeadline());
            updateDateText(etDeadlineDate, calendarDeadline);
        }

        switchPaidEvent.setChecked(currentEvent.isPaid());
        if (currentEvent.isPaid()) {
            containerPaymentDetails.setVisibility(View.VISIBLE);
            etCost.setText(String.valueOf(currentEvent.getCost()));
            dropdownPaymentType.setText(currentEvent.getPaymentConcept(), false);
        } else {
            containerPaymentDetails.setVisibility(View.GONE);
        }

        currentImageUrls = currentEvent.getImageUrls();
        if (currentImageUrls != null && !currentImageUrls.isEmpty()) {
            Glide.with(this).load(currentImageUrls.get(0)).centerCrop().into(ivEventImage);
            layoutImagePlaceholder.setVisibility(View.GONE);

            // Llenar Galería (Saltando el index 0 que es el banner)
            if (currentImageUrls.size() > 1) {
                for (int i = 1; i < currentImageUrls.size(); i++) {
                    galleryItems.add(currentImageUrls.get(i));
                }
                galleryAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateDateText(TextInputEditText et, Calendar cal) {
        et.setText(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
    }

    private void updateTimeText(TextInputEditText et, Calendar cal) {
        et.setText(String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePicker(etDate, calendarEventDate));
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime, calendarStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime, calendarEndTime));
        etDeadlineDate.setOnClickListener(v -> showDatePicker(etDeadlineDate, calendarDeadline));

        dropdownVenue.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            for (VenueModel v : venueList) {
                if (v.getName().equals(selection)) {
                    selectedVenue = v;
                    break;
                }
            }
        });

        dropdownCoach.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            if (selection.equals("-- Sin asignar --") || position == 0) {
                selectedCoach = null;
            } else {
                selectedCoach = coachList.get(position - 1);
            }
        });

        switchPaidEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            containerPaymentDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            bannerLauncher.launch(intent);
        });

        btnAddPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryPickerLauncher.launch(intent);
        });

        btnSaveChanges.setOnClickListener(v -> validateAndUpdateEvent());
    }

    private void showDatePicker(TextInputEditText target, Calendar calendar) {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateDateText(target, calendar);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker(TextInputEditText target, Calendar calendar) {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            updateTimeText(target, calendar);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void validateAndUpdateEvent() {
        if (etTitle.getText().toString().isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        String newVenueName = dropdownVenue.getText().toString();
        copyDateToTime(calendarEventDate, calendarStartTime);
        Date newStartDate = calendarStartTime.getTime();

        if (originalVenueName != null && !newVenueName.equals(originalVenueName)) {
            long diff = Math.abs(newStartDate.getTime() - originalStartTime.getTime());
            if (diff < 1000) {
                Toast.makeText(this, "⚠️ REGLA: Si cambias la sede, DEBES cambiar el horario.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        long now = System.currentTimeMillis();
        if (originalStartTime != null && originalStartTime.getTime() > now && (originalStartTime.getTime() - now) < 3600000) {
            Toast.makeText(this, "No se pueden guardar cambios a menos de 1h del evento.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);

        // Preparar carga de imágenes
        List<Uri> newImagesToUpload = new ArrayList<>();
        List<String> existingUrls = new ArrayList<>();

        // 1. Banner
        if (newMainImageUri != null) newImagesToUpload.add(newMainImageUri);
        else if (!currentImageUrls.isEmpty()) existingUrls.add(currentImageUrls.get(0));

        // 2. Galería
        for (Object item : galleryItems) {
            if (item instanceof Uri) newImagesToUpload.add((Uri) item);
            else if (item instanceof String) existingUrls.add((String) item);
        }

        if (!newImagesToUpload.isEmpty()) {
            uploadImagesRecursive(newImagesToUpload, 0, existingUrls);
        } else {
            updateEventInFirestore(existingUrls);
        }
    }

    private void uploadImagesRecursive(List<Uri> uris, int index, List<String> finalUrls) {
        if (index >= uris.size()) {
            updateEventInFirestore(finalUrls);
            return;
        }
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("events/" + filename);

        ref.putFile(uris.get(index))
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    finalUrls.add(uri.toString());
                    uploadImagesRecursive(uris, index + 1, finalUrls);
                }))
                .addOnFailureListener(e -> uploadImagesRecursive(uris, index + 1, finalUrls));
    }

    private void updateEventInFirestore(List<String> imageUrls) {
        currentEvent.setTitle(etTitle.getText().toString().trim());
        currentEvent.setDescription(etDescription.getText().toString().trim());
        currentEvent.setMacroEvent(etMacroEvent.getText().toString().trim());
        currentEvent.setCategory(dropdownCategory.getText().toString());
        currentEvent.setDiscipline(dropdownDiscipline.getText().toString());
        currentEvent.setModality(dropdownModality.getText().toString());

        if (selectedVenue != null) {
            currentEvent.setPlaceName(selectedVenue.getName());
            currentEvent.setAddress(selectedVenue.getAddress());
            currentEvent.setLatitude(selectedVenue.getLatitude());
            currentEvent.setLongitude(selectedVenue.getLongitude());
        }

        copyDateToTime(calendarEventDate, calendarStartTime);
        copyDateToTime(calendarEventDate, calendarEndTime);

        Date newStart = calendarStartTime.getTime();
        Date now = new Date();

        // --- FIX DE INTEGRIDAD DE ESTADOS ---
        String statusFromDB = currentEvent.getStatus();

        // 1. Si el evento es futuro (y no está cancelado), re-establecer a PENDIENTE/CONFIRMADO
        if (newStart.after(now)) {
            // Si el estado actual de la DB no es CANCELADO, no debe ser FINALIZADO o EN VIVO.
            if (!"CANCELADO".equalsIgnoreCase(statusFromDB)) {
                // Si se cumplen los requisitos mínimos, se puede forzar a CONFIRMADO, sino PENDIENTE.
                if (currentEvent.getCurrentParticipants() >= currentEvent.getMinQuota()) {
                    currentEvent.setStatus("CONFIRMADO");
                } else {
                    currentEvent.setStatus("PENDIENTE");
                }
            }
            // Si estaba cancelado, mantener cancelado, excepto si la lógica de reactivación es manual (toggleCancellationStatus)
        }
        // 2. Si el evento ya pasó, marcar como FINALIZADO.
        else if (calendarEndTime.getTime().before(now)) {
            currentEvent.setStatus("FINALIZADO");
        }
        // 3. Si ya está en rango, ActivityEventDetail lo actualizará a EN VIVO al ser visto (persistencia).
        // Mantenemos el estado actual si es EN VIVO o CONFIRMADO (aunque CONFIRMADO es mejor si no ha pasado la hora de inicio).

        currentEvent.setStartTime(newStart);
        currentEvent.setEndTime(calendarEndTime.getTime());
        currentEvent.setEventDateTime(newStart);

        if (!etDeadlineDate.getText().toString().isEmpty()) {
            calendarDeadline.set(Calendar.HOUR_OF_DAY, 23);
            calendarDeadline.set(Calendar.MINUTE, 59);
            currentEvent.setRegistrationDeadline(calendarDeadline.getTime());
        }

        currentEvent.setPaid(switchPaidEvent.isChecked());
        if (currentEvent.isPaid()) {
            try {
                currentEvent.setCost(Double.parseDouble(etCost.getText().toString()));
                currentEvent.setPaymentConcept(dropdownPaymentType.getText().toString());
            } catch (Exception e) { currentEvent.setCost(0.0); }
        } else {
            currentEvent.setCost(0.0);
        }

        try {
            currentEvent.setMinQuota(Integer.parseInt(etMinQuota.getText().toString()));
            currentEvent.setMaxQuota(Integer.parseInt(etMaxQuota.getText().toString()));
        } catch (Exception e) {}

        currentEvent.setImageUrls(imageUrls);

        // Coach assignment
        if (selectedCoach != null) {
            currentEvent.setCoachId(selectedCoach.getId());
            currentEvent.setCoachName(selectedCoach.getString("name"));
            currentEvent.setCoachEmail(selectedCoach.getString("email"));
        } else {
            currentEvent.setCoachId(null);
            currentEvent.setCoachName(null);
            currentEvent.setCoachEmail(null);
        }

        // Registration type
        String regType = dropdownRegistrationType.getText().toString();
        if (regType.equals("Individual")) {
            currentEvent.setRegistrationType("individual");
        } else if (regType.equals("Por Equipo")) {
            currentEvent.setRegistrationType("team");
        } else if (regType.equals("Ambos")) {
            currentEvent.setRegistrationType("both");
        } else {
            currentEvent.setRegistrationType("individual");
        }

        db.collection("events").document(currentEvent.getId())
                .set(currentEvent)
                .addOnSuccessListener(aVoid -> {
                    loadingOverlay.setVisibility(View.GONE);
                    String msg = "El evento '" + currentEvent.getTitle() + "' ha sido actualizado.";
                    notificationHelper.notifyEventParticipants(currentEvent.getId(), "Evento Actualizado", msg);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedEvent", currentEvent);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                });
    }

    private void copyDateToTime(Calendar sourceDate, Calendar targetTime) {
        targetTime.set(Calendar.YEAR, sourceDate.get(Calendar.YEAR));
        targetTime.set(Calendar.MONTH, sourceDate.get(Calendar.MONTH));
        targetTime.set(Calendar.DAY_OF_MONTH, sourceDate.get(Calendar.DAY_OF_MONTH));
    }

    class GalleryEditAdapter extends RecyclerView.Adapter<GalleryEditAdapter.ViewHolder> {
        private List<Object> items;
        public GalleryEditAdapter(List<Object> items) { this.items = items; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(250, 250));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(8, 8, 8, 8);
            return new ViewHolder(iv);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Object item = items.get(position);
            Glide.with(holder.itemView.getContext()).load(item).into((ImageView) holder.itemView);
            holder.itemView.setOnLongClickListener(v -> {
                items.remove(position);
                notifyItemRemoved(position);
                return true;
            });
        }
        @Override public int getItemCount() { return items.size(); }
        class ViewHolder extends RecyclerView.ViewHolder { public ViewHolder(View v) { super(v); } }
    }
}
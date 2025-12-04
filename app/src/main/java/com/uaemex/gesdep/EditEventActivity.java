package com.uaemex.gesdep;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.models.VenueModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class EditEventActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etTitle, etMacroEvent, etDescription, etMinQuota, etMaxQuota;
    private TextInputEditText etDate, etStartTime, etEndTime, etDeadlineDate;
    private SwitchMaterial switchPaidEvent;
    private LinearLayout containerPaymentDetails;
    private AutoCompleteTextView dropdownPaymentType, dropdownCategory, dropdownDiscipline, dropdownModality, dropdownVenue;
    private TextInputEditText etCost;
    private MaterialButton btnSaveChanges;
    private MaterialCardView btnSelectImage;
    private ImageView ivEventImage;
    private LinearLayout layoutImagePlaceholder;
    private View loadingOverlay;

    // Dates Helpers
    private Calendar calendarEventDate = Calendar.getInstance();
    private Calendar calendarStartTime = Calendar.getInstance();
    private Calendar calendarEndTime = Calendar.getInstance();
    private Calendar calendarDeadline = Calendar.getInstance();

    // Data
    private List<VenueModel> venueList = new ArrayList<>();
    private VenueModel selectedVenue = null;
    private EventModel currentEvent;

    // Multimedia
    private Uri newMainImageUri = null;
    private List<String> currentImageUrls = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<Intent> bannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    newMainImageUri = result.getData().getData();
                    Glide.with(this).load(newMainImageUri).centerCrop().into(ivEventImage);
                    layoutImagePlaceholder.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        WindowUtils.setGreenStatusBar(this);

        db = FirebaseFirestore.getInstance("gesdep");
        storage = FirebaseStorage.getInstance();

        initViews();
        setupDropdowns();
        setupListeners();

        // 1. RECUPERAR EL EVENTO A EDITAR
        if (getIntent().hasExtra("eventModel")) {
            currentEvent = (EventModel) getIntent().getSerializableExtra("eventModel");
            if (currentEvent != null) {
                loadVenuesAndFillData();
            }
        } else {
            Toast.makeText(this, "Error: No se recibió el evento", Toast.LENGTH_SHORT).show();
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
        loadingOverlay = findViewById(R.id.loadingOverlay);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        setAdapterFromResource(dropdownCategory, R.array.event_categories);
        setAdapterFromResource(dropdownDiscipline, R.array.event_disciplines);
        setAdapterFromResource(dropdownModality, R.array.event_modalities);
        setAdapterFromResource(dropdownPaymentType, R.array.event_payment_types);
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
        });
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

        switchPaidEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            containerPaymentDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            bannerLauncher.launch(intent);
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

        loadingOverlay.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);

        if (newMainImageUri != null) {
            uploadNewImageAndUpdate();
        } else {
            updateEventInFirestore(currentImageUrls);
        }
    }

    private void uploadNewImageAndUpdate() {
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("events/" + filename);

        ref.putFile(newMainImageUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    List<String> newUrls = new ArrayList<>();
                    newUrls.add(uri.toString());
                    if (currentImageUrls.size() > 1) {
                        newUrls.addAll(currentImageUrls.subList(1, currentImageUrls.size()));
                    }
                    updateEventInFirestore(newUrls);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error subiendo imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingOverlay.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                });
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

        currentEvent.setStartTime(calendarStartTime.getTime());
        currentEvent.setEndTime(calendarEndTime.getTime());
        currentEvent.setEventDateTime(calendarStartTime.getTime());

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

        db.collection("events").document(currentEvent.getId())
                .set(currentEvent)
                .addOnSuccessListener(aVoid -> {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Evento Actualizado Correctamente", Toast.LENGTH_SHORT).show();

                    // --- CAMBIO CLAVE AQUÍ ---
                    // Devolvemos el evento actualizado a la actividad padre para refrescar la UI al instante
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedEvent", currentEvent);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    // --------------------------
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void copyDateToTime(Calendar sourceDate, Calendar targetTime) {
        targetTime.set(Calendar.YEAR, sourceDate.get(Calendar.YEAR));
        targetTime.set(Calendar.MONTH, sourceDate.get(Calendar.MONTH));
        targetTime.set(Calendar.DAY_OF_MONTH, sourceDate.get(Calendar.DAY_OF_MONTH));
    }
}
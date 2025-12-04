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
// Importamos Date de Java, no Timestamp de Firebase para los setters del modelo
import java.util.Date;
import com.google.firebase.auth.FirebaseAuth;
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

public class CreateEventActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etTitle, etMacroEvent, etDescription, etMinQuota, etMaxQuota;
    private TextInputEditText etDate, etStartTime, etEndTime, etDeadlineDate;

    // Pagos
    private SwitchMaterial switchPaidEvent;
    private LinearLayout containerPaymentDetails;
    private AutoCompleteTextView dropdownPaymentType;
    private TextInputEditText etCost;

    // Dropdowns
    private AutoCompleteTextView dropdownCategory, dropdownDiscipline, dropdownModality, dropdownVenue;

    // Botones y Multimedia
    private MaterialButton btnPublishEvent, btnAddPhotos;
    private MaterialCardView btnSelectImage;
    private ImageView ivEventImage;
    private LinearLayout layoutImagePlaceholder;
    private View loadingOverlay;
    private RecyclerView rvEventImages;

    // Datos Temporales
    private Calendar calendarEventDate = Calendar.getInstance();
    private Calendar calendarStartTime = Calendar.getInstance();
    private Calendar calendarEndTime = Calendar.getInstance();
    private Calendar calendarDeadline = Calendar.getInstance();

    private List<VenueModel> venueList = new ArrayList<>();
    private VenueModel selectedVenue = null;
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    // Multimedia Uris
    private Uri mainImageUri = null;
    private List<Uri> galleryUris = new ArrayList<>();
    private GalleryAdapter galleryAdapter;

    // Firebase
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // --- LAUNCHERS ---
    private final ActivityResultLauncher<Intent> bannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    mainImageUri = result.getData().getData();
                    Glide.with(this).load(mainImageUri).centerCrop().into(ivEventImage);
                    layoutImagePlaceholder.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<Intent> galleryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            if (galleryUris.size() < 5) galleryUris.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        if (galleryUris.size() < 5) galleryUris.add(result.getData().getData());
                    }
                    galleryAdapter.notifyDataSetChanged();
                    Toast.makeText(this, galleryUris.size() + " fotos añadidas", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");
        storage = FirebaseStorage.getInstance();

        initViews();
        setupDropdowns();
        setupRecyclerView();
        setupListeners();
        loadVenues();
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

        // Pagos
        switchPaidEvent = findViewById(R.id.switchPaidEvent);
        containerPaymentDetails = findViewById(R.id.containerPaymentDetails);
        dropdownPaymentType = findViewById(R.id.dropdownPaymentType);
        etCost = findViewById(R.id.etCost);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        ivEventImage = findViewById(R.id.ivEventImage);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        rvEventImages = findViewById(R.id.rvEventImages);
        btnPublishEvent = findViewById(R.id.btnPublishEvent);
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

    private void loadVenues() {
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
        });
    }

    private void setupRecyclerView() {
        galleryAdapter = new GalleryAdapter(galleryUris);
        rvEventImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvEventImages.setAdapter(galleryAdapter);
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
                    selectedLat = v.getLatitude();
                    selectedLng = v.getLongitude();
                    break;
                }
            }
        });

        switchPaidEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                containerPaymentDetails.setVisibility(View.VISIBLE);
            } else {
                containerPaymentDetails.setVisibility(View.GONE);
                etCost.setText("");
                dropdownPaymentType.setText("", false);
            }
        });

        btnSelectImage.setOnClickListener(v -> checkPermissionAndOpenGallery(false));
        btnAddPhotos.setOnClickListener(v -> checkPermissionAndOpenGallery(true));
        btnPublishEvent.setOnClickListener(v -> validateAndCreateEvent());
    }

    private void showDatePicker(TextInputEditText target, Calendar calendar) {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            target.setText(day + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker(TextInputEditText target, Calendar calendar) {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            target.setText(String.format("%02d:%02d", hour, minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void checkPermissionAndOpenGallery(boolean isMultiSelect) {
        if (android.os.Build.VERSION.SDK_INT >= 33 ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (isMultiSelect) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                galleryPickerLauncher.launch(intent);
            } else {
                bannerLauncher.launch(intent);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
    }

    private void validateAndCreateEvent() {
        String title = etTitle.getText().toString().trim();
        String category = dropdownCategory.getText().toString();

        if (title.isEmpty() || category.isEmpty() || selectedVenue == null || etDate.getText().toString().isEmpty() ||
                etStartTime.getText().toString().isEmpty() || etEndTime.getText().toString().isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios (Título, Categoría, Sede, Horarios)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (switchPaidEvent.isChecked()) {
            String costStr = etCost.getText().toString().trim();
            if (costStr.isEmpty() || dropdownPaymentType.getText().toString().isEmpty()) {
                Toast.makeText(this, "Indica el monto y concepto de pago", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Sincronizar fecha con horas
        copyDateToTime(calendarEventDate, calendarStartTime);
        copyDateToTime(calendarEventDate, calendarEndTime);

        if (!etDeadlineDate.getText().toString().isEmpty()) {
            // Si hay fecha límite, asignamos las 23:59 de ese día
            calendarDeadline.set(Calendar.HOUR_OF_DAY, 23);
            calendarDeadline.set(Calendar.MINUTE, 59);
        }

        // Validar consistencia de horas
        if (calendarEndTime.before(calendarStartTime)) {
            Toast.makeText(this, "La hora de fin no puede ser antes del inicio", Toast.LENGTH_LONG).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);
        btnPublishEvent.setEnabled(false);

        // Preparar lista de imágenes
        List<Uri> allImagesToUpload = new ArrayList<>();
        if (mainImageUri != null) allImagesToUpload.add(mainImageUri);
        allImagesToUpload.addAll(galleryUris);

        if (!allImagesToUpload.isEmpty()) {
            uploadImagesRecursive(allImagesToUpload, 0, new ArrayList<>());
        } else {
            saveEventToFirestore(new ArrayList<>());
        }
    }

    private void copyDateToTime(Calendar sourceDate, Calendar targetTime) {
        targetTime.set(Calendar.YEAR, sourceDate.get(Calendar.YEAR));
        targetTime.set(Calendar.MONTH, sourceDate.get(Calendar.MONTH));
        targetTime.set(Calendar.DAY_OF_MONTH, sourceDate.get(Calendar.DAY_OF_MONTH));
    }

    private void uploadImagesRecursive(List<Uri> uris, int index, List<String> uploadedUrls) {
        if (index >= uris.size()) {
            saveEventToFirestore(uploadedUrls);
            return;
        }
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("events/" + filename);

        ref.putFile(uris.get(index))
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    uploadedUrls.add(uri.toString());
                    uploadImagesRecursive(uris, index + 1, uploadedUrls);
                }))
                .addOnFailureListener(e -> {
                    // Si falla una, seguimos con la siguiente
                    Toast.makeText(this, "Error subiendo una imagen", Toast.LENGTH_SHORT).show();
                    uploadImagesRecursive(uris, index + 1, uploadedUrls);
                });
    }

    private void saveEventToFirestore(List<String> imageUrls) {
        EventModel event = new EventModel();
        event.setTitle(etTitle.getText().toString().trim());
        event.setDescription(etDescription.getText().toString().trim());
        event.setMacroEvent(etMacroEvent.getText().toString().trim());

        event.setCategory(dropdownCategory.getText().toString());
        event.setDiscipline(dropdownDiscipline.getText().toString());
        event.setModality(dropdownModality.getText().toString());

        // Sede
        event.setPlaceName(selectedVenue.getName());
        event.setAddress(selectedVenue.getAddress());
        event.setLatitude(selectedLat);
        event.setLongitude(selectedLng);

        // **CORRECCIÓN CLAVE:** Usar .getTime() (Date) en lugar de new Timestamp()
        event.setStartTime(calendarStartTime.getTime());
        event.setEndTime(calendarEndTime.getTime());

        if (!etDeadlineDate.getText().toString().isEmpty()) {
            event.setRegistrationDeadline(calendarDeadline.getTime());
        }

        // Campo legacy para compatibilidad
        event.setEventDateTime(calendarStartTime.getTime());

        // Pagos
        event.setPaid(switchPaidEvent.isChecked());
        if (event.isPaid()) {
            try {
                event.setCost(Double.parseDouble(etCost.getText().toString().trim()));
                event.setPaymentConcept(dropdownPaymentType.getText().toString());
            } catch (Exception e) {
                event.setCost(0.0);
            }
        } else {
            event.setCost(0.0);
            event.setPaymentConcept("Gratuito");
        }

        try {
            event.setMinQuota(Integer.parseInt(etMinQuota.getText().toString()));
            event.setMaxQuota(Integer.parseInt(etMaxQuota.getText().toString()));
        } catch (Exception e) {
            event.setMinQuota(2);
            event.setMaxQuota(50);
        }

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anon";
        event.setOrganizerId(uid);
        event.setOrganizerName("IMCUFIDE");
        event.setStatus("ACTIVO");
        event.setCreatedAt(new Date()); // Date actual
        event.setImageUrls(imageUrls);

        db.collection("events").add(event)
                .addOnSuccessListener(docRef -> {
                    docRef.update("id", docRef.getId());
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "¡Evento Publicado!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingOverlay.setVisibility(View.GONE);
                    btnPublishEvent.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private List<Uri> uris;
        public GalleryAdapter(List<Uri> uris) { this.uris = uris; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            int size = (int) (100 * parent.getContext().getResources().getDisplayMetrics().density);
            iv.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(8, 8, 8, 8);
            return new ViewHolder(iv);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(uris.get(position)).into((ImageView) holder.itemView);
        }
        @Override public int getItemCount() { return uris.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder { public ViewHolder(View v) { super(v); } }
    }
}
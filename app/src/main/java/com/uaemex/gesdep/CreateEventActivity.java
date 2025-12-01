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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {

    // UI
    private TextInputEditText etTitle, etMacroEvent, etDescription, etMinQuota, etMaxQuota;
    private TextInputEditText etDate, etStartTime, etEndTime, etDeadlineDate;
    private AutoCompleteTextView dropdownCategory, dropdownDiscipline, dropdownModality, dropdownVenue;
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

    // Multimedia
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
        // Pickers
        etDate.setOnClickListener(v -> showDatePicker(etDate, calendarEventDate));
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime, calendarStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime, calendarEndTime));
        etDeadlineDate.setOnClickListener(v -> showDatePicker(etDeadlineDate, calendarDeadline));

        // Sede
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
            if (isMultiSelect) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            else bannerLauncher.launch(intent);

            if (isMultiSelect) galleryPickerLauncher.launch(intent);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
    }

    private void validateAndCreateEvent() {
        String title = etTitle.getText().toString().trim();

        if (title.isEmpty() || selectedVenue == null || etDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sincronizar fechas con horas
        copyDateToTime(calendarEventDate, calendarStartTime);
        copyDateToTime(calendarEventDate, calendarEndTime);

        // Validar horas
        if (calendarEndTime.before(calendarStartTime)) {
            Toast.makeText(this, "La hora de fin es incorrecta", Toast.LENGTH_LONG).show();
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);
        btnPublishEvent.setEnabled(false);

        List<Uri> allImages = new ArrayList<>();
        if (mainImageUri != null) allImages.add(mainImageUri);
        allImages.addAll(galleryUris);

        if (!allImages.isEmpty()) uploadImagesRecursive(allImages, 0, new ArrayList<>());
        else saveEventToFirestore(new ArrayList<>());
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
                .addOnFailureListener(e -> uploadImagesRecursive(uris, index + 1, uploadedUrls));
    }

    private void saveEventToFirestore(List<String> imageUrls) {
        EventModel event = new EventModel();
        event.setTitle(etTitle.getText().toString().trim());
        event.setDescription(etDescription.getText().toString().trim());
        event.setMacroEvent(etMacroEvent.getText().toString().trim());

        event.setCategory(dropdownCategory.getText().toString());
        event.setDiscipline(dropdownDiscipline.getText().toString());
        event.setModality(dropdownModality.getText().toString());

        // Datos de Sede
        event.setPlaceName(selectedVenue.getName());
        event.setAddress(selectedVenue.getAddress());
        event.setLatitude(selectedLat);
        event.setLongitude(selectedLng);

        // Tiempos (NUEVOS)
        event.setStartTime(new Timestamp(calendarStartTime.getTime()));
        event.setEndTime(new Timestamp(calendarEndTime.getTime()));
        if (!etDeadlineDate.getText().toString().isEmpty()) {
            event.setRegistrationDeadline(new Timestamp(calendarDeadline.getTime()));
        }

        // Compatibilidad con campos viejos (para evitar crash en listas viejas)
        event.setEventDateTime(new Timestamp(calendarStartTime.getTime()));

        try {
            event.setMinQuota(Integer.parseInt(etMinQuota.getText().toString()));
            event.setMaxQuota(Integer.parseInt(etMaxQuota.getText().toString()));
        } catch (Exception e) {
            event.setMinQuota(2);
            event.setMaxQuota(50);
        }

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anon";
        event.setOrganizerId(uid);
        event.setOrganizerName("UAEMex GESDEP");
        event.setStatus("ACTIVO");
        event.setCreatedAt(Timestamp.now());
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
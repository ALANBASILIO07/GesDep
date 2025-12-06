package com.uaemex.gesdep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat; // Importante

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uaemex.gesdep.models.UserModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.HashMap;
import java.util.Map;

public class UserDetailActivity extends AppCompatActivity {

    private EditText etName, etEmail, etRole;
    private ImageView ivProfile;
    private CardView btnChangePhoto;
    private MaterialButton btnSave, btnDelete;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private UserModel user;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();

                    // AL SELECCIONAR FOTO: LIMPIEZA TOTAL
                    ivProfile.setColorFilter(null);
                    ivProfile.setImageTintList(null);
                    ivProfile.setPadding(0, 0, 0, 0);
                    ivProfile.setBackground(null);

                    Glide.with(this)
                            .load(selectedImageUri)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivProfile);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        WindowUtils.setGreenStatusBar(this);

        db = FirebaseFirestore.getInstance("gesdep");
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        user = (UserModel) getIntent().getSerializableExtra("user_data");

        initViews();
        setupToolbar();

        if (user != null) {
            populateData();
        } else {
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etDetailName);
        etEmail = findViewById(R.id.etDetailEmail);
        etRole = findViewById(R.id.etDetailRole);
        ivProfile = findViewById(R.id.ivDetailProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSaveChanges);
        btnDelete = findViewById(R.id.btnDeleteUser);
        progressBar = findViewById(R.id.progressBarDetail);

        btnSave.setOnClickListener(v -> saveChanges());
        btnDelete.setOnClickListener(v -> confirmDelete());

        View.OnClickListener pickPhotoListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        };
        btnChangePhoto.setOnClickListener(pickPhotoListener);
        ivProfile.setOnClickListener(pickPhotoListener);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void populateData() {
        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etRole.setText(user.getRole());

        // --- LÓGICA DE FOTO CORREGIDA PARA EFECTO TROFEO ---
        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
            // SI HAY FOTO: LIMPIEZA
            ivProfile.setColorFilter(null);
            ivProfile.setImageTintList(null);
            ivProfile.setPadding(0, 0, 0, 0);
            ivProfile.setBackground(null);

            Glide.with(this)
                    .load(user.getProfilePhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfile);
        } else {
            // NO HAY FOTO: ESTILO TROFEO VERDE
            // Padding grande para esta vista (25dp aprox)
            int padding = (int) (25 * getResources().getDisplayMetrics().density);
            ivProfile.setPadding(padding, padding, padding, padding);

            // Trofeo Blanco
            ivProfile.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            ivProfile.setImageResource(R.drawable.ic_trophy);

            // Fondo Verde
            ivProfile.setBackgroundResource(R.drawable.bg_circle_green);
        }
    }

    // ... (Métodos saveChanges, uploadImageAndThenUpdate, updateUserInFirestore, confirmDelete IGUALES) ...
    // Solo cópialos de tu versión anterior, la lógica no cambia.

    private void saveChanges() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        if (selectedImageUri != null) uploadImageAndThenUpdate();
        else updateUserInFirestore(null);
    }

    private void uploadImageAndThenUpdate() {
        StorageReference fileRef = storageRef.child(user.getUid() + ".jpg");
        fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> updateUserInFirestore(uri.toString()))
        ).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            Toast.makeText(this, "Error subiendo imagen", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserInFirestore(String newPhotoUrl) {
        String newName = etName.getText().toString().trim();
        String newRole = etRole.getText().toString().trim().toLowerCase();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("role", newRole);
        if (newPhotoUrl != null) updates.put("photoUrl", newPhotoUrl);

        db.collection("users").document(user.getUid()).update(updates).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("ELIMINAR", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    db.collection("users").document(user.getUid()).delete().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton("Cancelar", null).show();
    }
}
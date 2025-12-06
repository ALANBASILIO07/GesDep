package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// CAMBIO DE IMPORTACIÓN AQUÍ:
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
// import com.google.android.material.materialswitch.MaterialSwitch; // <-- ELIMINAR ESTE

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.UserModel;
import com.uaemex.gesdep.utils.ThemeManager;
import com.uaemex.gesdep.utils.WindowUtils;

public class SettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialCardView cardProfile;
    private ImageView ivUserProfile;
    private TextView tvUserName;

    // CAMBIO DE TIPO AQUÍ:
    private SwitchCompat switchDarkMode;

    private LinearLayout btnChangePassword, btnAbout;
    private MaterialButton btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UserModel currentUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupThemeSwitch();
        loadUserProfile();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        cardProfile = findViewById(R.id.cardProfile);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        tvUserName = findViewById(R.id.tvUserName);

        // CAMBIO DE CASTING (Aunque findViewById lo infiere, es bueno saberlo)
        switchDarkMode = findViewById(R.id.switchDarkMode);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupThemeSwitch() {
        int currentMode = ThemeManager.getStoredTheme(this);
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            ThemeManager.saveThemeSelection(this, newMode);
        });
    }

    private void loadUserProfile() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserModel = new UserModel();
                        currentUserModel.setUid(uid);
                        currentUserModel.setName(documentSnapshot.getString("name"));
                        currentUserModel.setEmail(documentSnapshot.getString("email"));
                        currentUserModel.setRole(documentSnapshot.getString("role"));

                        String photoUrl = documentSnapshot.getString("photoUrl");
                        if(photoUrl == null) photoUrl = documentSnapshot.getString("profilePhotoUrl");
                        currentUserModel.setProfilePhotoUrl(photoUrl);

                        tvUserName.setText(currentUserModel.getName());

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            ivUserProfile.setPadding(0,0,0,0);
                            ivUserProfile.setColorFilter(null);
                            ivUserProfile.setBackground(null);

                            Glide.with(this)
                                    .load(photoUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(ivUserProfile);
                        } else {
                            ivUserProfile.setImageResource(R.drawable.ic_trophy);
                            int padding = (int) (12 * getResources().getDisplayMetrics().density);
                            ivUserProfile.setPadding(padding, padding, padding, padding);
                            ivUserProfile.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
                            ivUserProfile.setBackgroundResource(R.drawable.bg_circle_green);
                        }
                    }
                });
    }

    private void setupListeners() {
        cardProfile.setOnClickListener(v -> {
            if (currentUserModel != null) {
                Intent intent = new Intent(this, UserDetailActivity.class);
                intent.putExtra("user_data", currentUserModel);
                startActivity(intent);
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            String email = auth.getCurrentUser().getEmail();
            new AlertDialog.Builder(this)
                    .setTitle("Cambiar Contraseña")
                    .setMessage("Se enviará un correo a " + email + " para restablecer tu contraseña.")
                    .setPositiveButton("Enviar", (dialog, which) -> {
                        auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Correo enviado", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Acerca de GESDEP")
                    .setMessage("Sistema de Gestión Deportiva UAEMex.\nVersión 1.0.0\nDesarrollado por: \nAlan Osvaldo Basilio Delgado\nGustavo Olmedo Alarcón\nJorge Manuel García Vera")
                    .setPositiveButton("Aceptar", null)
                    .show();
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
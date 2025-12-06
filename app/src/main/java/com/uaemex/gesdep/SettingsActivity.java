package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat; // Importación correcta para el Switch
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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

    // Usamos SwitchCompat para evitar crash de estilos con MaterialSwitch
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

        // Asegura que el Status Bar se vea verde
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
        // Recargar datos por si se editó el perfil y se regresó a esta pantalla
        loadUserProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        cardProfile = findViewById(R.id.cardProfile);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        tvUserName = findViewById(R.id.tvUserName);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupThemeSwitch() {
        // Obtener tema actual guardado (0=System, 1=Light, 2=Dark)
        int currentMode = ThemeManager.getStoredTheme(this);

        // Si el modo es NOCHE (2), activamos el switch
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            ThemeManager.saveThemeSelection(this, newMode);
            // El cambio de tema recreará la actividad automáticamente
        });
    }

    private void loadUserProfile() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Parseo manual seguro de datos básicos
                        currentUserModel = new UserModel();
                        currentUserModel.setUid(uid);
                        currentUserModel.setName(documentSnapshot.getString("name"));
                        currentUserModel.setEmail(documentSnapshot.getString("email"));
                        currentUserModel.setRole(documentSnapshot.getString("role"));

                        // Intentar obtener foto de ambos campos posibles
                        String photoUrl = documentSnapshot.getString("photoUrl");
                        if(photoUrl == null) photoUrl = documentSnapshot.getString("profilePhotoUrl");
                        currentUserModel.setProfilePhotoUrl(photoUrl);

                        // Actualizar UI
                        tvUserName.setText(currentUserModel.getName());

                        // --- LÓGICA DE FOTO INTELIGENTE ---
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            // CASO: FOTO REAL -> Limpieza TOTAL de estilos
                            ivUserProfile.setPadding(0,0,0,0);
                            ivUserProfile.setColorFilter(null);
                            ivUserProfile.setImageTintList(null); // CRÍTICO para evitar tinte blanco
                            ivUserProfile.setBackground(null);    // Quitar círculo verde

                            Glide.with(this)
                                    .load(photoUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(ivUserProfile);
                        } else {
                            // CASO: SIN FOTO -> Estilo Trofeo Verde
                            ivUserProfile.setImageResource(R.drawable.ic_trophy);

                            // Padding dinámico (12dp)
                            int padding = (int) (12 * getResources().getDisplayMetrics().density);
                            ivUserProfile.setPadding(padding, padding, padding, padding);

                            // Tinte BLANCO para el icono
                            ivUserProfile.setColorFilter(ContextCompat.getColor(this, android.R.color.white));

                            // Fondo VERDE circular
                            ivUserProfile.setBackgroundResource(R.drawable.bg_circle_green);
                        }
                    }
                });
    }

    private void setupListeners() {
        // Ir a editar perfil
        cardProfile.setOnClickListener(v -> {
            if (currentUserModel != null) {
                Intent intent = new Intent(this, UserDetailActivity.class);
                intent.putExtra("user_data", currentUserModel);
                startActivity(intent);
            }
        });

        // Cambiar contraseña (Email de Firebase)
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

        // Acerca de
        btnAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Acerca de GESDEP")
                    .setMessage("Sistema de Gestión Deportiva UAEMex.\nVersión 1.0.0\n\nDesarrollado por:\nAlan Osvaldo Basilio Delgado\nGustavo Olmedo Alarcón\nJorge Manuel García Vera")
                    .setPositiveButton("Aceptar", null)
                    .show();
        });

        // Cerrar Sesión
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
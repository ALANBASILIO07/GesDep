package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword, etOrganizationCode;
    private RadioGroup rgUserType;
    private Button btnRegister;
    private ProgressBar progressBar;
    private View organizationCodeLayout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Código de validación para organizadores (puedes cambiarlo)
    private static final String ORGANIZATION_CODE = "IMCUFIDE2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etOrganizationCode = findViewById(R.id.etOrganizationCode);
        rgUserType = findViewById(R.id.rgUserType);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        organizationCodeLayout = findViewById(R.id.organizationCodeLayout);
    }

    private void setupListeners() {
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOrganizer) {
                organizationCodeLayout.setVisibility(View.VISIBLE);
            } else {
                organizationCodeLayout.setVisibility(View.GONE);
                etOrganizationCode.setText("");
            }
        });

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String orgCode = etOrganizationCode.getText().toString().trim();

        // Validaciones
        if (fullName.isEmpty()) {
            etFullName.setError("Ingrese su nombre completo");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Ingrese su email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingrese una contraseña");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }

        // Determinar tipo de usuario
        int selectedRole = rgUserType.getCheckedRadioButtonId();
        String userRole;

        if (selectedRole == R.id.rbOrganizer) {
            // Validar código de organización
            if (!orgCode.equals(ORGANIZATION_CODE)) {
                etOrganizationCode.setError("Código de organización inválido");
                etOrganizationCode.requestFocus();
                Toast.makeText(this, "Código inválido. Contacte al administrador.", Toast.LENGTH_LONG).show();
                return;
            }
            userRole = "admin";
        } else {
            userRole = "user";
        }

        // Registrar usuario
        registerUser(fullName, email, password, userRole);
    }

    private void registerUser(String fullName, String email, String password, String role) {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        // Crear documento de usuario en Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("uid", firebaseUser.getUid());
                        user.put("name", fullName);
                        user.put("email", email);
                        user.put("role", role);
                        user.put("createdAt", System.currentTimeMillis());
                        user.put("eventsOrganized", 0);
                        user.put("eventsParticipated", 0);
                        user.put("active", true);

                        db.collection("users")
                                .document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, 
                                        "Cuenta creada exitosamente", 
                                        Toast.LENGTH_SHORT).show();
                                    
                                    // Redirigir según el rol
                                    redirectToHome(role);
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(this, 
                                        "Error al guardar datos: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    
                    String errorMessage = "Error al crear cuenta";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("already in use")) {
                            errorMessage = "Este email ya está registrado";
                        } else if (e.getMessage().contains("invalid email")) {
                            errorMessage = "Email inválido";
                        } else if (e.getMessage().contains("weak password")) {
                            errorMessage = "Contraseña muy débil";
                        }
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    private void redirectToHome(String role) {
        Intent intent;
        if (role.equals("admin")) {
            intent = new Intent(this, AdminHomeActivity.class);
        } else {
            intent = new Intent(this, UserHomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

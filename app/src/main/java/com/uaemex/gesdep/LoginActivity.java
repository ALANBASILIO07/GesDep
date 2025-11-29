package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Importante para la flecha
import android.widget.ProgressBar;
import android.widget.TextView; // Importante para el texto de registro
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.uaemex.gesdep.services.MyFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    // Componentes de la interfaz
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageButton btnBackLogin; // Nuevo: Flecha atrás
    private TextView tvRegister;      // Nuevo: Texto para ir a registro
    private TextView btnForgotPassword;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inicializar Firebase con la instancia CORRECTA
        auth = FirebaseAuth.getInstance();
        // IMPORTANTE: Tu base de datos se llama "gesdep", no es la default
        db = FirebaseFirestore.getInstance("gesdep");

        // 2. Inicializar Vistas
        initViews();

        // 3. Configurar Botones
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Nuevos controles agregados en el XML
        btnBackLogin = findViewById(R.id.btnBackLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
    }

    private void setupListeners() {
        // Botón Iniciar Sesión
        btnLogin.setOnClickListener(v -> login());

        // Botón Atrás (Flecha): Regresa a WelcomeActivity
        btnBackLogin.setOnClickListener(v -> {
            onBackPressed(); // O simplemente finish()
        });

        // Texto "Regístrate aquí": Va a RegisterActivity
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish(); // Cerramos Login para que no quede en el historial intermedio
        });

        // Olvidé contraseña (Placeholder)
        btnForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Función de recuperación próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Ingrese su email");
            etEmail.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            etPassword.setError("Ingrese su contraseña");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        loadUserAndRedirect(user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    String errorMessage = "Error al iniciar sesión";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("no user record")) {
                            errorMessage = "Usuario no encontrado";
                        } else if (e.getMessage().contains("password is invalid") || e.getMessage().contains("credential")) {
                            errorMessage = "Contraseña incorrecta";
                        } else if (e.getMessage().contains("network")) {
                            errorMessage = "Error de conexión";
                        }
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    private void loadUserAndRedirect(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role == null) role = "user";
                        redirectToHome(role);
                    } else {
                        // Caso raro: Usuario en Auth pero no en Firestore
                        Toast.makeText(this, "Perfil no encontrado, contacte soporte", Toast.LENGTH_SHORT).show();
                        // Opcional: Crear perfil básico o redirigir a user
                        redirectToHome("user");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true); // Reactivar botón si falla la carga
                    Log.e("LoginActivity", "Error cargando perfil", e);
                    Toast.makeText(this, "Error de conexión a la base de datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void redirectToHome(String role) {
        // Registrar token de notificaciones (si tienes el servicio configurado)
        try {
            MyFirebaseMessagingService.registerFCMToken(this);
        } catch (Exception e) {
            Log.e("LoginActivity", "Error FCM: " + e.getMessage());
        }

        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(this, AdminHomeActivity.class);
                break;
            case "coach":
                intent = new Intent(this, CoachHomeActivity.class);
                break;
            default: // "user" o cualquier otro
                intent = new Intent(this, UserHomeActivity.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.uaemex.gesdep.services.MyFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> login());
        
        // Link para volver a Welcome
        findViewById(R.id.btnForgotPassword).setOnClickListener(v -> {
            // Por ahora solo volver
            finish();
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
                        } else if (e.getMessage().contains("password is invalid")) {
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
                        // Usuario no tiene documento en Firestore (no debería pasar)
                        Toast.makeText(this, "Error: Perfil no encontrado", Toast.LENGTH_SHORT).show();
                        redirectToHome("user");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("LoginActivity", "Error cargando perfil", e);
                    Toast.makeText(this, "Error cargando perfil", Toast.LENGTH_SHORT).show();
                    redirectToHome("user");
                });
    }

    private void redirectToHome(String role) {
        // Registrar token FCM para notificaciones push
        MyFirebaseMessagingService.registerFCMToken(this);

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

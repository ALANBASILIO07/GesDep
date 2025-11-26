package com.example.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPass;
    private Button btnLogin;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // AQUÍ defines qué correos son admins
    private final List<String> ADMIN_EMAILS = Arrays.asList(
            "admin@gesdep.com",
            "tu_correo_admin@gmail.com"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Error de usuario", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cargarOcrearUsuario(user);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error de login: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void cargarOcrearUsuario(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        DocumentReference ref = db.collection("users").document(uid);

        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // Ya tiene perfil, leemos el rol
                String role = doc.getString("role");
                if (role == null) role = "user";
                irHome(role);
            } else {
                // No existe → lo creamos
                String email = firebaseUser.getEmail();
                String name = firebaseUser.getDisplayName();
                if (name == null || name.isEmpty()) name = "Usuario";

                String role = (email != null && ADMIN_EMAILS.contains(email)) ? "admin" : "user";

                UserModel user = new UserModel(uid, name, email, role);
                ref.set(user)
                        .addOnSuccessListener(aVoid -> irHome(role))
                        .addOnFailureListener(e -> {
                            Log.e("LoginActivity", "Error guardando usuario", e);
                            Toast.makeText(this, "Error guardando perfil", Toast.LENGTH_SHORT).show();
                            irHome(role); // aun así lo mandamos
                        });
            }
        }).addOnFailureListener(e -> {
            Log.e("LoginActivity", "Error leyendo usuario", e);
            Toast.makeText(this, "Error leyendo perfil", Toast.LENGTH_SHORT).show();
            irHome("user");
        });
    }

    private void irHome(String role) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }
}

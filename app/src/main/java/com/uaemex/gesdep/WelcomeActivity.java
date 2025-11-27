package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si ya hay sesión iniciada
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser != null) {
            // Usuario ya autenticado, redirigir según su rol
            checkUserRoleAndRedirect(currentUser.getUid());
            return;
        }

        setContentView(R.layout.activity_welcome);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void checkUserRoleAndRedirect(String userId) {
        // Aquí verificaremos el rol desde Firestore
        // Por ahora redirigimos a HomeActivity (lo mejoraremos)
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}

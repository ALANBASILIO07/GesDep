package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserHomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);

        loadUserInfo();
        setupClickListeners();
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvWelcome.setText("¡Hola, " + name + "!");
                            }
                        }
                    });
        }
    }

    private void setupClickListeners() {
        // Ver Eventos Disponibles
        findViewById(R.id.cardEvents).setOnClickListener(v ->
                startActivity(new Intent(this, EventsActivity.class)));

        // Mis Eventos Registrados
        findViewById(R.id.cardMyEvents).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Mis Eventos", Toast.LENGTH_SHORT).show());

        // Mi Perfil
        findViewById(R.id.cardProfile).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Mi Perfil", Toast.LENGTH_SHORT).show());

        // Cerrar Sesión
        findViewById(R.id.cardLogout).setOnClickListener(v -> logout());
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

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
        // Gestionar Eventos
        findViewById(R.id.cardEvents).setOnClickListener(v ->
                startActivity(new Intent(this, EventsActivity.class)));

        // Crear Nuevo Evento
        findViewById(R.id.cardCreateEvent).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Crear Evento", Toast.LENGTH_SHORT).show());

        // Mis Eventos Organizados
        findViewById(R.id.cardMyEvents).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Mis Eventos Organizados", Toast.LENGTH_SHORT).show());

        // Participantes
        findViewById(R.id.cardParticipants).setOnClickListener(v ->
                startActivity(new Intent(this, ParticipantsActivity.class)));

        // Entrenadores
        findViewById(R.id.cardCoaches).setOnClickListener(v ->
                startActivity(new Intent(this, CoachesActivity.class)));

        // Reportes
        findViewById(R.id.cardReports).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Reportes", Toast.LENGTH_SHORT).show());

        // Configuración
        findViewById(R.id.cardSettings).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Configuración", Toast.LENGTH_SHORT).show());

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

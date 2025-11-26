package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private String role = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getIntent() != null) {
            String r = getIntent().getStringExtra("role");
            if (r != null) role = r;
        }

        try {
            // Crear datos de ejemplo en Firestore
            DatabaseInitializer.init();
        } catch (Exception e) {
            Log.e("HomeActivity", "Error al inicializar la BD", e);
            Toast.makeText(this, "Error al inicializar datos", Toast.LENGTH_SHORT).show();
        }

        View cardActivities = findViewById(R.id.cardActivities);
        View cardCoaches = findViewById(R.id.cardCoaches);
        View cardParticipants = findViewById(R.id.cardParticipants);
        View cardMaintenance = findViewById(R.id.cardMaintenance);

        // Todos los usuarios pueden ver eventos (nueva funcionalidad Fase 4)
        cardActivities.setOnClickListener(v ->
                startActivity(new Intent(this, EventsActivity.class)));

        cardCoaches.setOnClickListener(v -> {
            if (role.equals("admin")) {
                startActivity(new Intent(this, CoachesActivity.class));
            } else {
                Toast.makeText(this, "Solo administrador puede gestionar entrenadores", Toast.LENGTH_SHORT).show();
            }
        });

        cardParticipants.setOnClickListener(v -> {
            if (role.equals("admin")) {
                startActivity(new Intent(this, ParticipantsActivity.class));
            } else {
                Toast.makeText(this, "Solo administrador puede gestionar participantes", Toast.LENGTH_SHORT).show();
            }
        });

        cardMaintenance.setOnClickListener(v -> {
            if (role.equals("admin")) {
                startActivity(new Intent(this, MaintenanceActivity.class));
            } else {
                Toast.makeText(this, "Solo administrador puede acceder a mantenimiento", Toast.LENGTH_SHORT).show();
            }
        });

    }
}

package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private String role = "admin"; // Por defecto admin si es esta vista

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CORRECCIÓN: Usar el layout de Admin (donde están estos botones)
        setContentView(R.layout.activity_admin_home);

        if (getIntent() != null) {
            String r = getIntent().getStringExtra("role");
            if (r != null) role = r;
        }

        try {
            DatabaseInitializer.init();
        } catch (Exception e) {
            Log.e("HomeActivity", "Error al inicializar la BD", e);
        }

        // 2. CORRECCIÓN DE IDs (Actualizados según el nuevo XML)

        // Antes cardActivities -> Ahora cardEvents
        View cardEvents = findViewById(R.id.cardEvents);

        View cardCoaches = findViewById(R.id.cardCoaches);
        View cardParticipants = findViewById(R.id.cardParticipants);

        // Antes cardMaintenance -> Ahora cardReports
        View cardReports = findViewById(R.id.cardReports);

        // Configurar Listeners

        // Gestionar Eventos
        cardEvents.setOnClickListener(v ->
                startActivity(new Intent(this, EventsActivity.class)));

        // Gestionar Entrenadores
        cardCoaches.setOnClickListener(v -> {
            if (role.equals("admin")) {
                startActivity(new Intent(this, CoachesActivity.class));
            } else {
                Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            }
        });

        // Gestionar Participantes
        cardParticipants.setOnClickListener(v -> {
            if (role.equals("admin")) {
                startActivity(new Intent(this, ParticipantsActivity.class));
            } else {
                Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            }
        });

        // Mantenimiento y Reportes
        cardReports.setOnClickListener(v -> {
            if (role.equals("admin")) {
                startActivity(new Intent(this, MaintenanceActivity.class));
            } else {
                Toast.makeText(this, "Acceso denegado", Toast.LENGTH_SHORT).show();
            }
        });

        // Opcional: Configurar el botón de Cerrar Sesión si lo necesitas aquí también
        View cardLogout = findViewById(R.id.cardLogout);
        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> {
                // Tu lógica de logout
                finish();
            });
        }
    }
}
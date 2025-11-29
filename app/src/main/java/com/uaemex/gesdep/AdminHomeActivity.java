package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView; // Para la foto
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide; // Para Glide
import com.bumptech.glide.request.RequestOptions; // Para Glide
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.utils.WindowUtils;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        tvWelcome = findViewById(R.id.tvWelcome);

        loadUserInfo();
        updateNavHeader(navigationView); // IMPORTANTE: Cargar Header

        setupDashboardCards();
    }

    // --- MÉTODO CORREGIDO PARA FOTO DE PERFIL ---
    private void updateNavHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView navName = headerView.findViewById(R.id.navHeaderName);
        TextView navEmail = headerView.findViewById(R.id.navHeaderEmail);
        ImageView navImage = headerView.findViewById(R.id.imgProfile);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            navEmail.setText(user.getEmail());
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            if (name != null) navName.setText(name);

                            // Lógica de Foto
                            String photoUrl = doc.getString("photoUrl");
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                // Si tiene foto: Cargarla y quitar tinte
                                Glide.with(this)
                                        .load(photoUrl)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(navImage);
                                navImage.setPadding(0,0,0,0);
                                navImage.setColorFilter(null);
                            } else {
                                // Si no tiene: Poner trofeo blanco
                                navImage.setImageResource(R.drawable.ic_trophy);
                                navImage.setPadding(30,30,30,30); // Ajustar si es necesario
                                navImage.setColorFilter(getResources().getColor(R.color.white));
                            }
                        }
                    });
        }
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvWelcome.setText("¡Hola, " + name + "!");
                            }
                        }
                    });
        }
    }

    private void setupDashboardCards() {
        // Mismos listeners...
        findViewById(R.id.cardEvents).setOnClickListener(v -> startActivity(new Intent(this, EventsActivity.class)));
        findViewById(R.id.cardCreateEvent).setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));
        findViewById(R.id.cardMyEvents).setOnClickListener(v -> startActivity(new Intent(this, EventsActivity.class)));
        findViewById(R.id.cardParticipants).setOnClickListener(v -> startActivity(new Intent(this, ParticipantsActivity.class)));
        findViewById(R.id.cardCoaches).setOnClickListener(v -> startActivity(new Intent(this, CoachesActivity.class)));
        findViewById(R.id.cardReports).setOnClickListener(v -> startActivity(new Intent(this, MaintenanceActivity.class)));
        findViewById(R.id.cardSettings).setOnClickListener(v -> Toast.makeText(this, "Configuración próximamente", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardLogout).setOnClickListener(v -> logout());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_events) startActivity(new Intent(this, EventsActivity.class));
        else if (id == R.id.nav_admin_create_event) startActivity(new Intent(this, CreateEventActivity.class));
        else if (id == R.id.nav_admin_participants) startActivity(new Intent(this, ParticipantsActivity.class));
        else if (id == R.id.nav_admin_coaches) startActivity(new Intent(this, CoachesActivity.class));
        else if (id == R.id.nav_admin_reports) startActivity(new Intent(this, MaintenanceActivity.class));
        else if (id == R.id.nav_admin_settings) Toast.makeText(this, "Configuración próximamente", Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_admin_logout) logout();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
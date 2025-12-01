package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.utils.WindowUtils;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // UI Dashboard
    private TextView tvWelcome, tvCountEvents, tvCountUsers, tvCountReports;
    private MaterialButton btnQuickCreate, btnQuickMessage;

    // Notificaciones
    private View btnNotification;
    private View badgeNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupNavigation();
        loadUserInfo();
        loadDashboardData();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);

        // KPIs
        tvCountEvents = findViewById(R.id.tvCountEvents);
        tvCountUsers = findViewById(R.id.tvCountUsers);
        tvCountReports = findViewById(R.id.tvCountReports);

        // Botones Rápidos
        btnQuickCreate = findViewById(R.id.btnQuickCreate);
        btnQuickMessage = findViewById(R.id.btnQuickMessage);

        // Notificaciones (Ahora son una vista normal, no menú)
        btnNotification = findViewById(R.id.btnNotification);
        badgeNotification = findViewById(R.id.badgeNotification);

        // Listeners
        btnQuickCreate.setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));
        btnQuickMessage.setOnClickListener(v -> Toast.makeText(this, "Función de Avisos próximamente", Toast.LENGTH_SHORT).show());

        // Clic en la campanita
        btnNotification.setOnClickListener(v -> {
            // Ocultar el punto rojo al abrir
            badgeNotification.setVisibility(View.GONE);
            startActivity(new Intent(this, NotificationsActivity.class));
        });
    }

    private void setupNavigation() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // Cambiar color del icono de hamburguesa a blanco explícitamente si el tema falla
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateNavHeader(navigationView);
    }

    // ... (El resto de métodos loadDashboardData, loadUserInfo, updateNavHeader, onNavigationItemSelected, logout siguen IGUAL) ...

    private void loadDashboardData() {
        db.collection("events").whereEqualTo("status", "ACTIVO").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountEvents.setText(String.valueOf(snapshot.getCount())));

        db.collection("users").whereEqualTo("role", "user").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountUsers.setText(String.valueOf(snapshot.getCount())));

        tvCountReports.setText("0");
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            if (name != null) tvWelcome.setText("Hola, " + name);
                        }
                    });
        }
    }

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

                            String photoUrl = doc.getString("photoUrl");
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Glide.with(this).load(photoUrl).apply(RequestOptions.circleCropTransform()).into(navImage);
                                navImage.setPadding(0,0,0,0);
                                navImage.setColorFilter(null);
                            } else {
                                navImage.setImageResource(R.drawable.ic_trophy);
                                navImage.setPadding(30,30,30,30);
                                navImage.setColorFilter(getResources().getColor(R.color.white));
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_admin_home) { }
        else if (id == R.id.nav_admin_events) startActivity(new Intent(this, EventsActivity.class));
        else if (id == R.id.nav_admin_venues) startActivity(new Intent(this, ManageVenuesActivity.class));
        else if (id == R.id.nav_admin_inbox) Toast.makeText(this, "Bandeja de entrada próximamente", Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_admin_map) Toast.makeText(this, "Mapa general próximamente", Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_admin_reports) startActivity(new Intent(this, MaintenanceActivity.class));
        else if (id == R.id.nav_admin_settings) Toast.makeText(this, "Ajustes próximamente", Toast.LENGTH_SHORT).show();
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
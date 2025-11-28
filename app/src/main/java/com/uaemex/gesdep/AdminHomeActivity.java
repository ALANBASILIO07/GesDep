package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView tvWelcome;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
                                tvWelcome.setText("Hola, " + name + "!");
                            }
                        }
                    });
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.cardEvents).setOnClickListener(v ->
                startActivity(new Intent(this, EventsActivity.class)));

        findViewById(R.id.cardCreateEvent).setOnClickListener(v ->
                Toast.makeText(this, "Proximamente: Crear Evento", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardMyEvents).setOnClickListener(v ->
                Toast.makeText(this, "Proximamente: Mis Eventos", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardParticipants).setOnClickListener(v ->
                startActivity(new Intent(this, ParticipantsActivity.class)));

        findViewById(R.id.cardCoaches).setOnClickListener(v ->
                startActivity(new Intent(this, CoachesActivity.class)));

        findViewById(R.id.cardReports).setOnClickListener(v ->
                Toast.makeText(this, "Proximamente: Reportes", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardSettings).setOnClickListener(v ->
                Toast.makeText(this, "Proximamente: Configuracion", Toast.LENGTH_SHORT).show());

        findViewById(R.id.cardLogout).setOnClickListener(v -> logout());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == 0x7f0a01e1) {
        } else if (id == 0x7f0a01e2) {
            startActivity(new Intent(this, EventsActivity.class));
        } else if (id == 0x7f0a01e3) {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show();
        } else if (id == 0x7f0a01e4) {
            startActivity(new Intent(this, ParticipantsActivity.class));
        } else if (id == 0x7f0a01e5) {
            startActivity(new Intent(this, CoachesActivity.class));
        } else if (id == 0x7f0a01e6) {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show();
        } else if (id == 0x7f0a01e7) {
            logout();
        }

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

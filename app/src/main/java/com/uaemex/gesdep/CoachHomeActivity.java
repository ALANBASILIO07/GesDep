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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.utils.WindowUtils;

public class CoachHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView tvWelcome;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_home);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        MaterialToolbar toolbar = findViewById(R.id.toolbarCoach);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout_coach);
        NavigationView navigationView = findViewById(R.id.nav_view_coach);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        tvWelcome = findViewById(R.id.tvWelcomeCoach);

        loadUserInfo();
        updateNavHeader(navigationView);
        setupClickListeners();
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
                                Glide.with(this)
                                        .load(photoUrl)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(navImage);
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
        findViewById(R.id.cardEvents).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Mis Grupos", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardMyEvents).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Mi Horario", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardProfile).setOnClickListener(v ->
                Toast.makeText(this, "Próximamente: Mi Perfil", Toast.LENGTH_SHORT).show());
        findViewById(R.id.cardLogout).setOnClickListener(v -> logout());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_coach_home) { }
        else if (id == R.id.nav_coach_groups) Toast.makeText(this, "Próximamente", Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_coach_schedule) Toast.makeText(this, "Próximamente", Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_coach_profile) Toast.makeText(this, "Próximamente", Toast.LENGTH_SHORT).show();
        else if (id == R.id.nav_coach_logout) logout();

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
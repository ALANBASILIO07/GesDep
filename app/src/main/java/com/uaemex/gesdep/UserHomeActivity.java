package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uaemex.gesdep.adapters.LiveEventAdapter;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvWelcome;
    private TextView tvCurrentCredit, tvCountEnrollments, tvCountAttendances, tvCountTeams, tvCountReports;
    private MaterialButton btnQuickExploreEvents, btnQuickRecharge, btnQuickMessage;

    private RecyclerView rvLiveEvents;
    private TextView tvNoLiveEvents;
    private LiveEventAdapter liveAdapter;
    private List<EventModel> liveEventsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        loadLiveEvents();
        loadUserInfo();
        updateNavHeader();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvCurrentCredit = findViewById(R.id.tvCurrentCredit);
        tvCountEnrollments = findViewById(R.id.tvCountEnrollments);
        tvCountAttendances = findViewById(R.id.tvCountAttendances);
        tvCountTeams = findViewById(R.id.tvCountTeams);
        tvCountReports = findViewById(R.id.tvCountReports);

        btnQuickExploreEvents = findViewById(R.id.btnQuickExploreEvents);
        btnQuickRecharge = findViewById(R.id.btnQuickRecharge);
        btnQuickMessage = findViewById(R.id.btnQuickMessage);

        rvLiveEvents = findViewById(R.id.rvLiveEvents);
        tvNoLiveEvents = findViewById(R.id.tvNoLiveEvents);

        liveAdapter = new LiveEventAdapter(liveEventsList);
        rvLiveEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLiveEvents.setAdapter(liveAdapter);

        btnQuickExploreEvents.setOnClickListener(v -> startActivity(new Intent(this, EventsActivity.class)));
        btnQuickRecharge.setOnClickListener(v -> startActivity(new Intent(this, RechargeActivity.class)));
        btnQuickMessage.setOnClickListener(v -> startActivity(new Intent(this, CreateReportActivity.class)));
    }

    private void setupNavigation() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void updateNavHeader() {
        if (navigationView == null) return;

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
                                navImage.setPadding(0, 0, 0, 0);
                                navImage.setColorFilter(null);
                            } else {
                                navImage.setImageResource(R.drawable.ic_trophy);
                                navImage.setPadding(30, 30, 30, 30);
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

    private void loadDashboardData() {
        String userId = auth.getCurrentUser().getUid();

        // Cargar inscripciones
        db.collection("eventParticipants")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    tvCountEnrollments.setText(String.valueOf(count));
                });

        // Cargar asistencias (si existe la colección)
        db.collection("attendances")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    tvCountAttendances.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    // Si no existe la colección, dejar en 0
                    tvCountAttendances.setText("0");
                });

        // Cargar equipos a los que pertenece
        db.collection("teams")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> memberIds = (List<String>) doc.get("memberIds");
                        if (memberIds != null && memberIds.contains(userId)) {
                            count++;
                        }
                    }
                    tvCountTeams.setText(String.valueOf(count));
                });
    }

    private void loadLiveEvents() {
        Date now = new Date();

        db.collection("events")
                .whereEqualTo("status", "ACTIVO")
                .orderBy("startTime", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    liveEventsList.clear();
                    querySnapshot.forEach(doc -> {
                        EventModel event = doc.toObject(EventModel.class);
                        String timeStatus = event.getTimeStatus();
                        if ("EN VIVO".equals(timeStatus) || "PENDIENTE".equals(timeStatus)) {
                            liveEventsList.add(event);
                        }
                    });

                    if (liveEventsList.isEmpty()) {
                        tvNoLiveEvents.setVisibility(View.VISIBLE);
                        rvLiveEvents.setVisibility(View.GONE);
                    } else {
                        tvNoLiveEvents.setVisibility(View.GONE);
                        rvLiveEvents.setVisibility(View.VISIBLE);
                        liveAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_user_home) { }
        else if (id == R.id.nav_user_enrollments) startActivity(new Intent(this, EventsActivity.class)); // TODO: Create MyEnrollmentsActivity
        else if (id == R.id.nav_user_events) startActivity(new Intent(this, EventsActivity.class));
        else if (id == R.id.nav_user_map) startActivity(new Intent(this, MapEventsActivity.class));
        else if (id == R.id.nav_user_reports) startActivity(new Intent(this, ReportsListActivity.class));
        else if (id == R.id.nav_user_settings) startActivity(new Intent(this, SettingsActivity.class));
        else if (id == R.id.nav_user_logout) logout();

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

package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // UI Dashboard
    private TextView tvWelcome;
    private TextView tvCountEvents, tvCountUsers, tvCountVenues, tvCountReports;
    private MaterialButton btnQuickCreate, btnQuickMessage;
    private View btnNotification, badgeNotification;

    // Eventos en Vivo
    private RecyclerView rvLiveEvents;
    private TextView tvNoLiveEvents;
    private LiveEventAdapter liveAdapter;
    private List<EventModel> liveEventsList = new ArrayList<>();

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
    }

    // IMPORTANTE: Actualizar datos cada vez que la pantalla se muestra
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        loadLiveEvents();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);

        // KPIs
        tvCountEvents = findViewById(R.id.tvCountEvents);
        tvCountUsers = findViewById(R.id.tvCountUsers);
        tvCountVenues = findViewById(R.id.tvCountVenues);
        tvCountReports = findViewById(R.id.tvCountReports);

        btnQuickCreate = findViewById(R.id.btnQuickCreate);
        btnQuickMessage = findViewById(R.id.btnQuickMessage);
        btnNotification = findViewById(R.id.btnNotification);
        badgeNotification = findViewById(R.id.badgeNotification);

        // Live Events
        rvLiveEvents = findViewById(R.id.rvLiveEvents);
        tvNoLiveEvents = findViewById(R.id.tvNoLiveEvents);

        // Configurar Recycler Horizontal
        liveAdapter = new LiveEventAdapter(liveEventsList);
        rvLiveEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLiveEvents.setAdapter(liveAdapter);

        // Listeners
        btnQuickCreate.setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));
        btnQuickMessage.setOnClickListener(v -> Toast.makeText(this, "Función de Avisos próximamente", Toast.LENGTH_SHORT).show());

        btnNotification.setOnClickListener(v -> {
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
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateNavHeader(navigationView);
    }

    private void loadDashboardData() {
        // 1. Eventos Activos
        db.collection("events").whereEqualTo("status", "ACTIVO").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountEvents.setText(String.valueOf(snapshot.getCount())));

        // 2. Usuarios
        db.collection("users").whereEqualTo("role", "user").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountUsers.setText(String.valueOf(snapshot.getCount())));

        // 3. Sedes
        db.collection("venues").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountVenues.setText(String.valueOf(snapshot.getCount())));

        // 4. Reportes (Placeholder)
        tvCountReports.setText("0");
    }

    private void loadLiveEvents() {
        // Traemos todos los activos y filtramos por hora en el cliente
        db.collection("events").whereEqualTo("status", "ACTIVO").get()
                .addOnSuccessListener(querySnapshot -> {
                    liveEventsList.clear();
                    long now = new Date().getTime();

                    for (DocumentSnapshot doc : querySnapshot) {
                        EventModel event = doc.toObject(EventModel.class);
                        // CORRECCIÓN: Verificar nulos y NO usar toDate() porque ya es Date
                        if (event != null && event.getStartTime() != null && event.getEndTime() != null) {
                            long start = event.getStartTime().getTime(); // <--- CAMBIO AQUÍ
                            long end = event.getEndTime().getTime();     // <--- CAMBIO AQUÍ

                            // Si el evento está ocurriendo AHORA
                            if (now >= start && now <= end) {
                                event.setId(doc.getId());
                                liveEventsList.add(event);
                            }
                        }
                    }

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

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            if (name != null) tvWelcome.setText("Hola, " + name + "!");
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
        else if (id == R.id.nav_admin_users) startActivity(new Intent(this, ParticipantsActivity.class));
        else if (id == R.id.nav_admin_venues) startActivity(new Intent(this, ManageVenuesActivity.class));
        else if (id == R.id.nav_admin_inbox) startActivity(new Intent(this, NotificationsActivity.class));
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

    // --- ADAPTADOR INTERNO EVENTOS EN VIVO ---
    class LiveEventAdapter extends RecyclerView.Adapter<LiveEventAdapter.ViewHolder> {
        private List<EventModel> list;
        public LiveEventAdapter(List<EventModel> list) { this.list = list; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
            int width = (int) (300 * parent.getContext().getResources().getDisplayMetrics().density);
            v.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EventModel event = list.get(position);
            holder.tvTitle.setText(event.getTitle());

            holder.tvCategory.setText("🔴 EN VIVO • " + event.getDiscipline());
            holder.tvCategory.setTextColor(getColor(R.color.red_error));

            // Tiempo transcurrido (CORREGIDO: Sin toDate)
            long now = new Date().getTime();
            long start = event.getStartTime().getTime(); // <--- CAMBIO AQUÍ
            long diff = now - start;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            holder.tvDate.setText("Iniciado hace " + minutes + " min");

            holder.tvLocation.setText(event.getPlaceName());

            holder.cardStatus.setCardBackgroundColor(getColor(R.color.red_light));
            holder.tvStatus.setText("LIVE");
            holder.tvStatus.setTextColor(getColor(R.color.red_error));

            holder.tvParticipants.setText(event.getCurrentParticipants() + "/" + event.getMaxQuota());

            // Cargar imagen si existe
            if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
                Glide.with(AdminHomeActivity.this)
                        .load(event.getImageUrls().get(0))
                        .centerCrop()
                        .placeholder(R.drawable.ic_calendar)
                        .into(holder.ivThumbnail); // Asegúrate que el ViewHolder tenga este campo
            }
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCategory, tvDate, tvLocation, tvStatus, tvParticipants;
            MaterialCardView cardStatus;
            ImageView ivThumbnail; // Nuevo

            public ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvEventName);
                tvCategory = v.findViewById(R.id.tvCategory);
                tvDate = v.findViewById(R.id.tvDate);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvStatus = v.findViewById(R.id.tvStatus);
                tvParticipants = v.findViewById(R.id.tvParticipants);
                cardStatus = v.findViewById(R.id.cardStatus);
                ivThumbnail = v.findViewById(R.id.ivEventThumbnail); // Nuevo ID del XML item_event

                v.setOnClickListener(view -> {
                    Intent i = new Intent(AdminHomeActivity.this, EventDetailActivity.class);
                    i.putExtra("event", list.get(getAdapterPosition()));
                    startActivity(i);
                });
            }
        }
    }
}
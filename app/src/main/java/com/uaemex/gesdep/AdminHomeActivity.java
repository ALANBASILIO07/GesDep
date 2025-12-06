package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private final String TAG = "LiveEvents";

    private TextView tvWelcome;
    private TextView tvCountEvents, tvCountUsers, tvCountVenues, tvCountReports;
    private TextView tvCurrentCredit;
    private MaterialButton btnQuickCreate, btnQuickMessage, btnQuickRecharge;
    private View btnNotification, badgeNotification;

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
        tvCountEvents = findViewById(R.id.tvCountEvents);
        tvCountUsers = findViewById(R.id.tvCountUsers);
        tvCountVenues = findViewById(R.id.tvCountVenues);
        tvCountReports = findViewById(R.id.tvCountReports);
        tvCurrentCredit = findViewById(R.id.tvCurrentCredit);

        btnQuickCreate = findViewById(R.id.btnQuickCreate);
        btnQuickMessage = findViewById(R.id.btnQuickMessage);
        btnQuickRecharge = findViewById(R.id.btnQuickRecharge);
        btnNotification = findViewById(R.id.btnNotification);
        badgeNotification = findViewById(R.id.badgeNotification);

        rvLiveEvents = findViewById(R.id.rvLiveEvents);
        tvNoLiveEvents = findViewById(R.id.tvNoLiveEvents);

        liveAdapter = new LiveEventAdapter(liveEventsList);
        rvLiveEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLiveEvents.setAdapter(liveAdapter);

        btnQuickCreate.setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));
        btnQuickMessage.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateReportActivity.class);
            intent.putExtra("eventId", "GENERAL");
            intent.putExtra("eventName", "Aviso General");
            startActivity(intent);
        });

        btnQuickRecharge.setOnClickListener(v -> startActivity(new Intent(this, CreditActivity.class)));

        btnNotification.setOnClickListener(v -> {
            badgeNotification.setVisibility(View.GONE);
            startActivity(new Intent(this, NotificationsActivity.class));
        });
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
                            String photoUrl = doc.getString("photoUrl");
                            if (photoUrl == null) photoUrl = doc.getString("profilePhotoUrl");

                            if (name != null) navName.setText(name);

                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                navImage.setColorFilter(null);
                                navImage.setPadding(0,0,0,0);
                                Glide.with(this).load(photoUrl).apply(RequestOptions.circleCropTransform()).into(navImage);
                            } else {
                                navImage.setImageResource(R.drawable.ic_trophy);
                                navImage.setPadding(30,30,30,30);
                                navImage.setColorFilter(getResources().getColor(android.R.color.white));
                            }
                        }
                    });
        }
    }

    private void loadDashboardData() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        db.collection("events")
                .whereIn("status", Arrays.asList("ACTIVO", "PENDIENTE", "CONFIRMADO", "EN VIVO"))
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountEvents.setText(String.valueOf(snapshot.getCount())));

        db.collection("users").whereEqualTo("role", "user").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountUsers.setText(String.valueOf(snapshot.getCount())));

        db.collection("venues").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> tvCountVenues.setText(String.valueOf(snapshot.getCount())));

        tvCountReports.setText("0");

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        Double credit = doc.getDouble("appCredit");
                        if (credit == null) credit = 0.0;
                        tvCurrentCredit.setText(format.format(credit));
                    })
                    .addOnFailureListener(e -> tvCurrentCredit.setText(format.format(0)));
        } else {
            tvCurrentCredit.setText(format.format(0));
        }
    }

    private void loadLiveEvents() {
        final long GRACE_PERIOD_MINUTES = 10;
        final long GRACE_PERIOD_MILLIS = TimeUnit.MINUTES.toMillis(GRACE_PERIOD_MINUTES);
        long nowLocal = System.currentTimeMillis();
        TimeZone tz = TimeZone.getDefault();
        long tzOffset = tz.getOffset(nowLocal);

        db.collection("events")
                .whereIn("status", Arrays.asList("ACTIVO", "PENDIENTE", "CONFIRMADO", "EN VIVO"))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    liveEventsList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event != null && event.getStartTime() != null && event.getEndTime() != null) {
                            long startUtc = event.getStartTime().getTime();
                            long endUtc = event.getEndTime().getTime();
                            long startLocalAdjusted = startUtc + tzOffset;
                            long endLocalAdjusted = endUtc + tzOffset;

                            if (nowLocal >= startLocalAdjusted && nowLocal <= (endLocalAdjusted + GRACE_PERIOD_MILLIS)) {
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_home) { }
        else if (id == R.id.nav_admin_events) startActivity(new Intent(this, EventsActivity.class));
        else if (id == R.id.nav_admin_users) startActivity(new Intent(this, ParticipantsActivity.class));
        else if (id == R.id.nav_admin_venues) startActivity(new Intent(this, ManageVenuesActivity.class));
        else if (id == R.id.nav_admin_inbox) startActivity(new Intent(this, NotificationsActivity.class));
        else if (id == R.id.nav_admin_map) startActivity(new Intent(this, MapEventsActivity.class));
        else if (id == R.id.nav_admin_reports) startActivity(new Intent(this, ReportsListActivity.class));
            // ✅ CONECTADO: Ahora abre SettingsActivity
        else if (id == R.id.nav_admin_settings) startActivity(new Intent(this, SettingsActivity.class));
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
            holder.tvCategory.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));

            long now = new Date().getTime();
            long start = event.getStartTime().getTime();
            TimeZone tz = TimeZone.getDefault();
            long tzOffset = tz.getOffset(now);
            long startLocalAdjusted = start + tzOffset;
            long diff = now - startLocalAdjusted;

            String timeText;
            if (diff < 0) {
                long remaining = Math.abs(diff);
                long hours = TimeUnit.MILLISECONDS.toHours(remaining);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
                timeText = "Inicia en " + (hours > 0 ? hours + " hr " : "") + minutes + " min";
            } else {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                timeText = "Iniciado hace " + (minutes < 60 ? minutes + " min" : TimeUnit.MILLISECONDS.toHours(diff) + " hr(s)");
            }
            holder.tvDate.setText(timeText);
            holder.tvLocation.setText(event.getPlaceName());
            holder.tvParticipants.setText(event.getCurrentParticipants() + "/" + event.getMaxQuota());

            try {
                holder.cardStatus.setCardBackgroundColor(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.red_error));
            } catch (Exception e) {
                holder.cardStatus.setCardBackgroundColor(ContextCompat.getColorStateList(holder.itemView.getContext(), android.R.color.holo_red_light));
            }
            holder.tvStatus.setText("LIVE");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));

            if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(event.getImageUrls().get(0))
                        .centerCrop()
                        .placeholder(R.drawable.ic_calendar)
                        .into(holder.ivThumbnail);
            }
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCategory, tvDate, tvLocation, tvStatus, tvParticipants;
            MaterialCardView cardStatus;
            ImageView ivThumbnail;

            public ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvEventName);
                tvCategory = v.findViewById(R.id.tvCategory);
                tvDate = v.findViewById(R.id.tvDate);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvStatus = v.findViewById(R.id.tvStatus);
                tvParticipants = v.findViewById(R.id.tvParticipants);
                cardStatus = v.findViewById(R.id.cardStatus);
                ivThumbnail = v.findViewById(R.id.ivEventThumbnail);

                v.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Intent i = new Intent(AdminHomeActivity.this, ActivityEventDetail.class);
                        i.putExtra("eventModel", list.get(position));
                        startActivity(i);
                    }
                });
            }
        }
    }
}
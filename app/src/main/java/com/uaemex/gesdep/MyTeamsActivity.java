package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uaemex.gesdep.adapters.TeamsAdapter;
import com.uaemex.gesdep.models.TeamModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

public class MyTeamsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvTeams;
    private TextView tvNoTeams;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreateTeam;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TeamsAdapter adapter;
    private List<TeamModel> teamsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_teams);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupRecyclerView();
        loadTeams();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeams(); // Reload when returning from create/detail
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvTeams = findViewById(R.id.rvTeams);
        tvNoTeams = findViewById(R.id.tvNoTeams);
        progressBar = findViewById(R.id.progressBar);
        fabCreateTeam = findViewById(R.id.fabCreateTeam);

        fabCreateTeam.setOnClickListener(v ->
                startActivity(new Intent(this, CreateTeamActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new TeamsAdapter(teamsList, this::openTeamDetail);
        rvTeams.setLayoutManager(new LinearLayoutManager(this));
        rvTeams.setAdapter(adapter);
    }

    private void loadTeams() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoTeams.setVisibility(View.GONE);

        String coachId = auth.getCurrentUser().getUid();

        db.collection("teams")
                .whereEqualTo("coachId", coachId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    teamsList.clear();
                    querySnapshot.forEach(doc -> {
                        TeamModel team = doc.toObject(TeamModel.class);
                        teamsList.add(team);
                    });

                    progressBar.setVisibility(View.GONE);

                    if (teamsList.isEmpty()) {
                        tvNoTeams.setVisibility(View.VISIBLE);
                        rvTeams.setVisibility(View.GONE);
                    } else {
                        tvNoTeams.setVisibility(View.GONE);
                        rvTeams.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoTeams.setVisibility(View.VISIBLE);
                    tvNoTeams.setText("Error al cargar equipos");
                });
    }

    private void openTeamDetail(TeamModel team) {
        Intent intent = new Intent(this, TeamDetailActivity.class);
        intent.putExtra("team", team);
        startActivity(intent);
    }
}

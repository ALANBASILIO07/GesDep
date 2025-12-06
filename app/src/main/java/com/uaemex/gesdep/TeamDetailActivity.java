package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.uaemex.gesdep.models.TeamModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.List;

public class TeamDetailActivity extends AppCompatActivity {

    private TeamModel team;
    private RecyclerView rvMembers;
    private TextView tvNoMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);
        WindowUtils.setGreenStatusBar(this);

        // Recuperar el objeto serializable
        team = (TeamModel) getIntent().getSerializableExtra("teamModel");

        if (team == null) {
            Toast.makeText(this, "Error al cargar el equipo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
    }

    // Método para recargar datos si volvemos de agregar miembros
    @Override
    protected void onResume() {
        super.onResume();
        // Aquí idealmente volveríamos a cargar el equipo de Firebase para ver los cambios reflejados
        // Por simplicidad, si la actividad anterior cerró, el usuario tendrá que salir y entrar para ver cambios
        // O implementamos loadTeamFromFirebase() aquí.
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvTeamName = findViewById(R.id.tvTeamName);
        TextView tvInstitution = findViewById(R.id.tvInstitution);
        Chip chipDiscipline = findViewById(R.id.chipDiscipline);
        Chip chipCategory = findViewById(R.id.chipCategory);
        TextView tvMemberCount = findViewById(R.id.tvMemberCount);
        TextView tvUniformColor = findViewById(R.id.tvUniformColor);
        rvMembers = findViewById(R.id.rvMembers);
        tvNoMembers = findViewById(R.id.tvNoMembers);
        ExtendedFloatingActionButton fabAddMember = findViewById(R.id.fabAddMember);

        // Set Data
        tvTeamName.setText(team.teamName);
        tvInstitution.setText(team.institution != null ? team.institution : "N/A");
        chipDiscipline.setText(team.discipline);
        chipCategory.setText(team.category);
        tvMemberCount.setText(team.currentMembers + "/" + team.maxMembers);
        tvUniformColor.setText(team.uniformColor != null ? team.uniformColor : "N/A");

        setupMembersList();

        // --- CONEXIÓN DE LA NUEVA VISTA ---
        fabAddMember.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectParticipantsActivity.class);
            intent.putExtra("teamModel", team); // Pasamos el equipo para saber a cuál agregar
            startActivity(intent);
        });
    }

    private void setupMembersList() {
        if (team.members == null || team.members.isEmpty()) {
            rvMembers.setVisibility(View.GONE);
            tvNoMembers.setVisibility(View.VISIBLE);
        } else {
            rvMembers.setVisibility(View.VISIBLE);
            tvNoMembers.setVisibility(View.GONE);

            MembersAdapter adapter = new MembersAdapter(team.members);
            rvMembers.setLayoutManager(new LinearLayoutManager(this));
            rvMembers.setAdapter(adapter);
        }
    }

    // Adaptador interno simple para los miembros
    private class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
        private List<TeamModel.TeamMember> members;

        public MembersAdapter(List<TeamModel.TeamMember> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TeamModel.TeamMember member = members.get(position);
            holder.tvName.setText(member.name);
            holder.tvRole.setText(member.position + " (" + member.age + " años)");
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvParticipantName);
                tvRole = itemView.findViewById(R.id.tvParticipantRole);
            }
        }
    }
}
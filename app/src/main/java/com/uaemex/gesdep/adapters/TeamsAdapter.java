package com.uaemex.gesdep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.TeamModel;

import java.util.List;

public class TeamsAdapter extends RecyclerView.Adapter<TeamsAdapter.TeamViewHolder> {

    private List<TeamModel> teams;
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(TeamModel team);
    }

    public TeamsAdapter(List<TeamModel> teams, OnTeamClickListener listener) {
        this.teams = teams;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamModel team = teams.get(position);
        holder.bind(team, listener);
    }

    @Override
    public int getItemCount() {
        return teams != null ? teams.size() : 0;
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName, tvDiscipline, tvStatus, tvMembersCount, tvEventsCount;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvDiscipline = itemView.findViewById(R.id.tvDiscipline);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMembersCount = itemView.findViewById(R.id.tvMembersCount);
            tvEventsCount = itemView.findViewById(R.id.tvEventsCount);
        }

        public void bind(TeamModel team, OnTeamClickListener listener) {
            tvTeamName.setText(team.teamName);
            tvDiscipline.setText(team.discipline + " · " + team.category);

            // Estado
            if ("active".equals(team.status)) {
                tvStatus.setText("ACTIVO");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.green_primary, null));
            } else {
                tvStatus.setText("INACTIVO");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary_color, null));
            }

            // Contadores
            tvMembersCount.setText(String.valueOf(team.currentMembers));
            int eventsCount = team.enrolledEventIds != null ? team.enrolledEventIds.size() : 0;
            tvEventsCount.setText(String.valueOf(eventsCount));

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamClick(team);
                }
            });
        }
    }
}

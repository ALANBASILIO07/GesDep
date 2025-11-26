package com.example.gesdep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {

    private List<ParticipantModel> participants;

    public ParticipantAdapter(List<ParticipantModel> participants) {
        this.participants = participants;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        ParticipantModel p = participants.get(position);
        holder.tvName.setText(p.getName());
        holder.tvActivity.setText("Actividad: " + p.getActivity());
        holder.tvAge.setText("Edad: " + p.getAge());
        holder.tvPhone.setText("Tel: " + p.getPhone());
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public void updateList(List<ParticipantModel> newList) {
        this.participants = newList;
        notifyDataSetChanged();
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvActivity, tvAge, tvPhone;

        ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvParticipantName);
            tvActivity = itemView.findViewById(R.id.tvParticipantActivity);
            tvAge = itemView.findViewById(R.id.tvParticipantAge);
            tvPhone = itemView.findViewById(R.id.tvParticipantPhone);
        }
    }
}


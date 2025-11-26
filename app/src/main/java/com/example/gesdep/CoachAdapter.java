package com.example.gesdep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CoachAdapter extends RecyclerView.Adapter<CoachAdapter.CoachViewHolder> {

    public interface OnCoachLongClickListener {
        void onCoachLongClick(CoachModel coach);
    }

    private List<CoachModel> coachList;
    private OnCoachLongClickListener longClickListener;

    public CoachAdapter(List<CoachModel> coachList, OnCoachLongClickListener listener) {
        this.coachList = coachList;
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public CoachViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coach, parent, false);
        return new CoachViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CoachViewHolder holder, int position) {
        CoachModel coach = coachList.get(position);
        holder.tvName.setText(coach.getName());
        holder.tvSport.setText("Deporte: " + coach.getSport());
        holder.tvPhone.setText("Tel: " + coach.getPhone());
        holder.tvEmail.setText(coach.getEmail());

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onCoachLongClick(coach);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return coachList.size();
    }

    static class CoachViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvPhone, tvEmail;

        CoachViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCoachName);
            tvSport = itemView.findViewById(R.id.tvCoachSport);
            tvPhone = itemView.findViewById(R.id.tvCoachPhone);
            tvEmail = itemView.findViewById(R.id.tvCoachEmail);
        }
    }
}

package com.example.gesdep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(ActivityModel model);
    }

    private final List<ActivityModel> list;
    private final OnItemClick listener;

    public ActivitiesAdapter(List<ActivityModel> list, OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ActivityModel m = list.get(position);
        h.tvName.setText(m.name);
        h.tvCoach.setText("Entrenador: " + m.coachName);
        h.tvSchedule.setText(m.schedule);

        h.itemView.setOnClickListener(v -> listener.onClick(m));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCoach, tvSchedule;

        ViewHolder(View item) {
            super(item);
            tvName = item.findViewById(R.id.tvName);
            tvCoach = item.findViewById(R.id.tvCoach);
            tvSchedule = item.findViewById(R.id.tvSchedule);
        }
    }
}

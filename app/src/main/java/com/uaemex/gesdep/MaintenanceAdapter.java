package com.uaemex.gesdep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ReportViewHolder> {

    private List<MaintenanceReport> reports;

    public MaintenanceAdapter(List<MaintenanceReport> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        MaintenanceReport r = reports.get(position);
        holder.tvSubject.setText(r.getTitle());
        holder.tvStatus.setText(r.getStatus());
        holder.tvEventName.setText(r.getLocation());
        holder.tvCategory.setText("Mantenimiento");
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void updateList(List<MaintenanceReport> newList) {
        this.reports = newList;
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvStatus, tvEventName, tvCategory;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}

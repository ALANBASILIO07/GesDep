package com.example.gesdep;

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
        holder.tvTitle.setText(r.getTitle());
        holder.tvStatus.setText("Estado: " + r.getStatus());
        holder.tvLocation.setText("Ubicación: " + r.getLocation());
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
        TextView tvTitle, tvStatus, tvLocation;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportTitle);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
            tvLocation = itemView.findViewById(R.id.tvReportLocation);
        }
    }
}

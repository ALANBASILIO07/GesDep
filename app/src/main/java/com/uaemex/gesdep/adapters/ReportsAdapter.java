package com.uaemex.gesdep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.ReportModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<ReportModel> reports;
    private OnReportClickListener clickListener;

    public interface OnReportClickListener {
        void onReportClick(ReportModel report);
    }

    public ReportsAdapter(List<ReportModel> reports, OnReportClickListener clickListener) {
        this.reports = reports;
        this.clickListener = clickListener;
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
        ReportModel report = reports.get(position);

        holder.tvEventName.setText(report.getEventName());
        holder.tvSubject.setText(report.getSubject());
        holder.tvCategory.setText(report.getCategory());
        holder.tvPriority.setText(report.getPriority());
        holder.tvStatus.setText(report.getStatus());
        holder.tvCreatedBy.setText("Creado por: " + report.getCreatedByName());

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(report.getCreatedAt()));
        holder.tvDate.setText(dateStr);

        // Set priority color
        int priorityColor;
        switch (report.getPriority()) {
            case ReportModel.PRIORITY_HIGH:
                priorityColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.holo_red_dark, holder.itemView.getContext().getTheme());
                break;
            case ReportModel.PRIORITY_MEDIUM:
                priorityColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.holo_orange_dark, holder.itemView.getContext().getTheme());
                break;
            default:
                priorityColor = holder.itemView.getContext().getResources()
                        .getColor(android.R.color.holo_green_dark, holder.itemView.getContext().getTheme());
                break;
        }
        holder.tvPriority.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(priorityColor));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void updateReports(List<ReportModel> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvSubject, tvCategory, tvPriority, tvStatus, tvCreatedBy, tvDate;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedBy = itemView.findViewById(R.id.tvCreatedBy);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}

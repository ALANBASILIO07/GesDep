package com.uaemex.gesdep.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.uaemex.gesdep.ActivityEventDetail;
import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.EventModel;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class LiveEventAdapter extends RecyclerView.Adapter<LiveEventAdapter.ViewHolder> {
    private List<EventModel> list;
    private Context context;

    public LiveEventAdapter(List<EventModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        int width = (int) (300 * context.getResources().getDisplayMetrics().density);
        v.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

    @Override
    public int getItemCount() {
        return list.size();
    }

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
                    Intent i = new Intent(context, ActivityEventDetail.class);
                    i.putExtra("eventModel", list.get(position));
                    context.startActivity(i);
                }
            });
        }
    }
}

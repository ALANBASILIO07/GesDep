package com.uaemex.gesdep.adapters;

import android.content.Context;
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
import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.EventModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<EventModel> events;
    private OnEventClickListener listener;
    private SimpleDateFormat dateFormat;
    private Context context;

    public interface OnEventClickListener {
        void onEventClick(EventModel event);
    }

    public EventsAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM, HH:mm", new Locale("es", "MX"));
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<EventModel> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvCategory, tvDate, tvLocation, tvParticipants, tvStatus;
        private MaterialCardView cardStatus;
        private ImageView ivThumbnail;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardStatus = itemView.findViewById(R.id.cardStatus);
            ivThumbnail = itemView.findViewById(R.id.ivEventThumbnail);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });
        }

        public void bind(EventModel event) {
            tvTitle.setText(event.getTitle());

            // Categoría
            String discipline = event.getDiscipline() != null ? event.getDiscipline() : "General";
            String modality = event.getModality() != null ? event.getModality() : "";
            tvCategory.setText(discipline + (modality.isEmpty() ? "" : " • " + modality));

            // Fecha (Lógica BLINDADA)
            String dateText = "Por definir";
            Date dateToFormat = null;

            // Intentar obtener la fecha de inicio (puede ser Timestamp o Date)
            Object startObj = event.getStartTime();
            if (startObj == null) startObj = event.getEventDateTime(); // Fallback

            if (startObj != null) {
                if (startObj instanceof com.google.firebase.Timestamp) {
                    dateToFormat = ((com.google.firebase.Timestamp) startObj).toDate();
                } else if (startObj instanceof Date) {
                    dateToFormat = (Date) startObj;
                }
            }

            if (dateToFormat != null) {
                dateText = dateFormat.format(dateToFormat);
            }
            tvDate.setText(dateText);

            // Ubicación y Participantes
            tvLocation.setText(event.getPlaceName());
            int current = event.getCurrentParticipants();
            int max = event.getMaxQuota();
            tvParticipants.setText(current + "/" + max);

            // Estado Visual
            // Nota: Podrías usar event.getTimeStatus() aquí, pero la lógica visual actual funciona
            setStatusVisual("DISPONIBLE", 0xFFE8F5E9, R.color.green_primary); // Default

            // Imagen
            if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrls().get(0))
                        .centerCrop()
                        .placeholder(R.drawable.ic_calendar)
                        .into(ivThumbnail);
                ivThumbnail.setPadding(0,0,0,0);
                ivThumbnail.setColorFilter(null);
            } else {
                ivThumbnail.setImageResource(R.drawable.ic_trophy); // Asegúrate de tener ic_trophy o cambia a ic_calendar
                ivThumbnail.setColorFilter(ContextCompat.getColor(context, R.color.green_primary));
                ivThumbnail.setPadding(25,25,25,25);
                ivThumbnail.setBackgroundColor(ContextCompat.getColor(context, R.color.card_surface_color));
            }
        }

        private void setStatusVisual(String text, int bgColor, int textColorRes) {
            tvStatus.setText(text);
            if (bgColor == 0xFFE8F5E9) cardStatus.setCardBackgroundColor(bgColor);
            else cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, bgColor));
            tvStatus.setTextColor(ContextCompat.getColor(context, textColorRes));
        }
    }
}
package com.uaemex.gesdep.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
        this.dateFormat = new SimpleDateFormat("EEE, d MMM, hh:mm a", new Locale("es", "MX"));
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

        // Variables de la vista
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
            Context context = itemView.getContext();

            tvTitle.setText(event.getTitle());

            // Categoría
            String discipline = event.getDiscipline() != null ? event.getDiscipline() : "General";
            String modality = event.getModality() != null ? event.getModality() : "";
            tvCategory.setText(discipline.toUpperCase() + (modality.isEmpty() ? "" : " • " + modality.toUpperCase()));

            // Fecha
            String dateText = "Por definir";
            Date dateToFormat = event.getEventDateTime() != null ? event.getEventDateTime() : event.getStartTime();

            if (dateToFormat != null) {
                dateText = dateFormat.format(dateToFormat);
            }
            tvDate.setText(dateText);

            // Ubicación y Participantes
            tvLocation.setText(event.getPlaceName());
            int current = event.getCurrentParticipants();
            int max = event.getMaxQuota();
            tvParticipants.setText(current + "/" + max);

            // --- CÓDIGOS DE COLOR ---
            int redText = Color.parseColor("#F44336");
            int redBg = Color.parseColor("#33F44336");
            int strongGreenText = Color.parseColor("#00E676");
            int strongGreenBg = Color.parseColor("#3300E676");
            int blueText = Color.parseColor("#2196F3");
            int blueBg = Color.parseColor("#332196F3");
            int grayText = Color.parseColor("#9E9E9E");
            int grayBg = Color.parseColor("#339E9E9E");
            int defaultBlueBg = Color.parseColor("#332196F3");

            // --- LÓGICA DE PRIORIDAD DE ESTATUS LIMPIA ---

            String dbStatus = event.getStatus();
            String timeStatus = event.getTimeStatus(); // EN VIVO, FINALIZADO, PENDIENTE
            String statusToDisplay;

            int finalTextColor;
            int finalBgColor;
            boolean isFull = max > 0 && current >= max; // Check capacity once

            // 1. Prioridad Máxima: Estados Críticos o de Tiempo
            if ("CANCELADO".equalsIgnoreCase(dbStatus)) {
                statusToDisplay = "CANCELADO";
                finalBgColor = redBg;
                finalTextColor = redText;
            }
            else if ("EN VIVO".equals(timeStatus)) {
                statusToDisplay = "EN VIVO";
                finalBgColor = redBg;
                finalTextColor = redText;
            }
            else if ("FINALIZADO".equals(timeStatus)) {
                statusToDisplay = "FINALIZADO";
                finalBgColor = grayBg;
                finalTextColor = grayText;
            }

            // 2. Prioridad Media: ESTADO OPERATIVO (CONFIRMADO, PENDIENTE)
            else if ("CONFIRMADO".equalsIgnoreCase(dbStatus)) {
                statusToDisplay = "CONFIRMADO";
                finalBgColor = strongGreenBg;
                finalTextColor = strongGreenText;
            }
            else {
                // PENDIENTE (o el estado inicial)
                statusToDisplay = "PENDIENTE";
                finalBgColor = blueBg;
                finalTextColor = blueText;
            }

            // --- APLICAR VISUALIZACIÓN ---
            // CORRECCIÓN: Acceso directo a tvStatus y cardStatus
            tvStatus.setText(statusToDisplay);
            cardStatus.setCardBackgroundColor(ColorStateList.valueOf(finalBgColor));
            tvStatus.setTextColor(finalTextColor);

            // Advertencia de LLENO: Si está lleno y el badge principal no es CANCELADO/FINALIZADO/EN VIVO,
            // aplicamos la advertencia al texto de participantes y categoría.
            if (isFull && !("EN VIVO".equalsIgnoreCase(timeStatus) || "CANCELADO".equalsIgnoreCase(dbStatus) || "FINALIZADO".equalsIgnoreCase(timeStatus))) {
                tvParticipants.setTextColor(redText); // Advertir en el contador
                tvCategory.setTextColor(grayText);
            } else {
                // Usar color del estado operativo
                tvCategory.setTextColor(finalTextColor);
                tvParticipants.setTextColor(ContextCompat.getColor(context, R.color.purple_participants)); // Color original del contador
            }

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
                ivThumbnail.setImageResource(R.drawable.ic_trophy);
                ivThumbnail.setColorFilter(ContextCompat.getColor(context, R.color.green_primary));
                ivThumbnail.setPadding(25,25,25,25);
                ivThumbnail.setBackgroundColor(ContextCompat.getColor(context, R.color.card_surface_color));
            }
        }
    }
}
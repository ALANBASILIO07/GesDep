package com.uaemex.gesdep.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.EventModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar eventos en RecyclerView
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<EventModel> events;
    private OnEventClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnEventClickListener {
        void onEventClick(EventModel event);
    }

    public EventsAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("es", "MX"));
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        private TextView tvEventName;
        private TextView tvCategory;
        private TextView tvDate;
        private TextView tvLocation;
        private TextView tvParticipants;
        private Chip chipStatus;
        private Chip chipType;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            chipType = itemView.findViewById(R.id.chipType);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });
        }

        public void bind(EventModel event) {
            // Nombre del evento
            tvEventName.setText(event.name);

            // Categoría
            String categoryText = getCategoryDisplayName(event.category);
            tvCategory.setText(categoryText);

            // Fecha y hora
            if (event.eventDateTime != null) {
                String formattedDate = dateFormat.format(event.eventDateTime.toDate());
                tvDate.setText(formattedDate);
            }

            // Ubicación
            tvLocation.setText(event.placeName);

            // Participantes
            String participantsText = event.currentParticipants + "/" + event.maxParticipants;
            tvParticipants.setText(participantsText);

            // Chip de tipo (deportivo/cultural)
            chipType.setText(event.type.equals("deportivo") ? "Deportivo" : "Cultural");
            if (event.type.equals("deportivo")) {
                chipType.setChipBackgroundColorResource(android.R.color.holo_blue_light);
            } else {
                chipType.setChipBackgroundColorResource(android.R.color.holo_purple);
            }

            // Chip de estado
            if (event.isConfirmed) {
                chipStatus.setText("Confirmado");
                chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light);
                chipStatus.setTextColor(Color.WHITE);
            } else {
                chipStatus.setText("Pendiente");
                chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                chipStatus.setTextColor(Color.WHITE);
            }

            // Mostrar si está lleno
            if (event.isFull()) {
                chipStatus.setText("Lleno");
                chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light);
            }
        }

        private String getCategoryDisplayName(String category) {
            if (category == null) return "";

            switch (category) {
                case "futbol": return "Fútbol";
                case "basquetbol": return "Basquetbol";
                case "voleibol": return "Voleibol";
                case "atletismo": return "Atletismo";
                case "salto_longitud": return "Salto de Longitud";
                case "ajedrez": return "Ajedrez";
                case "natacion": return "Natación";
                case "ciclismo": return "Ciclismo";
                case "danza_individual": return "Danza Individual";
                case "danza_grupo": return "Danza Grupal";
                case "teatro_individual": return "Teatro Individual";
                case "teatro_grupo": return "Teatro Grupal";
                case "musica_solista": return "Música Solista";
                case "musica_banda": return "Banda Musical";
                case "arte": return "Arte y Pintura";
                default: return category;
            }
        }
    }
}

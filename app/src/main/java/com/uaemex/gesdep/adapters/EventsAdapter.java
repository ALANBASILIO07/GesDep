package com.uaemex.gesdep.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.EventModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        // Formato: 27 Nov, 10:00 AM
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
        // Elementos actualizados según el nuevo item_event.xml
        private TextView tvEventName;
        private TextView tvCategory;    // Antes chipType
        private TextView tvDate;
        private TextView tvLocation;
        private TextView tvParticipants;
        private TextView tvStatus;      // Antes chipStatus (texto)
        private MaterialCardView cardStatus; // Contenedor para el color de fondo del estado

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vincular con los nuevos IDs
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardStatus = itemView.findViewById(R.id.cardStatus); // Asegúrate de agregar este ID en el XML

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });
        }

        public void bind(EventModel event) {
            // 1. Nombre
            tvEventName.setText(event.name);

            // 2. Categoría y Tipo (Combinados en un solo texto estilizado)
            String typeDisplay = event.type.substring(0, 1).toUpperCase() + event.type.substring(1);
            String categoryDisplay = getCategoryDisplayName(event.category);
            tvCategory.setText(typeDisplay + " • " + categoryDisplay);

            // 3. Fecha
            if (event.eventDateTime != null) {
                tvDate.setText(dateFormat.format(event.eventDateTime.toDate()));
            }

            // 4. Ubicación
            tvLocation.setText(event.placeName);

            // 5. Participantes
            tvParticipants.setText(event.currentParticipants + "/" + event.maxParticipants);

            // 6. Lógica de Estado (Colores y Texto)
            if (event.isFull()) {
                tvStatus.setText("LLENO");
                // Rojo suave para lleno
                if(cardStatus != null) cardStatus.setCardBackgroundColor(Color.parseColor("#FFCDD2"));
                tvStatus.setTextColor(Color.parseColor("#C62828"));
            }
            else if (event.isConfirmed) {
                tvStatus.setText("CONFIRMADO");
                // Usar colores de tu paleta si es posible, aquí uso hex directos o recursos
                if(cardStatus != null) cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green_primary));
                tvStatus.setTextColor(Color.WHITE);
            }
            else {
                tvStatus.setText("PENDIENTE");
                // Naranja suave
                if(cardStatus != null) cardStatus.setCardBackgroundColor(Color.parseColor("#FFE0B2"));
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.orange_coach));
            }
        }

        private String getCategoryDisplayName(String category) {
            if (category == null) return "";
            switch (category) {
                case "futbol": return "Fútbol";
                case "basquetbol": return "Basquetbol";
                case "voleibol": return "Voleibol";
                case "atletismo": return "Atletismo";
                // ... resto de casos iguales ...
                default: return category.substring(0, 1).toUpperCase() + category.substring(1);
            }
        }
    }
}
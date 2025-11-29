package com.uaemex.gesdep.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Importación necesaria
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

        private TextView tvTitle;
        private TextView tvCategory;
        private TextView tvDate;
        private TextView tvLocation;
        private TextView tvParticipants;
        private TextView tvStatus;
        private MaterialCardView cardStatus;
        // private ImageView imgEvent; // Comentado hasta que el XML lo tenga

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardStatus = itemView.findViewById(R.id.cardStatus);
            // imgEvent = itemView.findViewById(R.id.imgEvent);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });
        }

        public void bind(EventModel event) {
            // 1. Título (Usando el Getter correcto)
            tvTitle.setText(event.getTitle());

            // 2. Categoría Detallada (Disciplina • Modalidad)
            String discipline = event.getDiscipline() != null ? event.getDiscipline() : "General";
            String modality = event.getModality() != null ? event.getModality() : "";

            // Si hay modalidad, la agregamos con un punto separador
            String categoryText = discipline + (modality.isEmpty() ? "" : " • " + modality);
            tvCategory.setText(categoryText);

            // 3. Fecha
            if (event.getEventDateTime() != null) {
                tvDate.setText(dateFormat.format(event.getEventDateTime().toDate()));
            } else {
                tvDate.setText("Por definir");
            }

            // 4. Ubicación
            tvLocation.setText(event.getPlaceName());

            // 5. Participantes
            // Nota: Usamos los métodos Exclude que creamos en el modelo para lógica,
            // o los getters directos para mostrar datos crudos.
            int current = event.getCurrentParticipants();
            int max = event.getMaxQuota();
            tvParticipants.setText(current + "/" + max);

            // 6. Estado (Colores)
            String status = event.getStatus();

            if ("LLENO".equals(status) || (event.getMaxQuota() > 0 && current >= max)) {
                tvStatus.setText("LLENO");
                if(cardStatus != null) cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red_light));
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_error));
            }
            else if ("CANCELADO".equals(status)) {
                tvStatus.setText("CANCELADO");
                if(cardStatus != null) cardStatus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray_light));
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.gray_dark));
            }
            else {
                tvStatus.setText("DISPONIBLE");
                // Usamos un verde claro o el color por defecto si no hay green_light
                // Aquí asumo que podrías no tener green_light, así que uso un hex seguro o white
                if(cardStatus != null) cardStatus.setCardBackgroundColor(0xFFE8F5E9); // Verde muy claro hex
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green_primary));
            }
        }
    }
}
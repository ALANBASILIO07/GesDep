package com.uaemex.gesdep;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.repositories.EventRepository;
import com.uaemex.gesdep.repositories.EventRepository.ParticipantData; // Usamos el modelo del Repository

import java.util.List;

public class ParticipantsSheet extends BottomSheetDialogFragment {

    private String eventId;
    private boolean isAdmin;
    private RecyclerView rvParticipants;
    private TextView tvEmptyState;
    private ProgressBar loading;
    private FirebaseFirestore db;
    private EventRepository repository;

    public ParticipantsSheet() {}

    public ParticipantsSheet(String eventId, boolean isAdmin) {
        this.eventId = eventId;
        this.isAdmin = isAdmin;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_participants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance("gesdep");
        repository = new EventRepository();

        rvParticipants = view.findViewById(R.id.rvParticipants);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        loading = view.findViewById(R.id.loading);

        rvParticipants.setLayoutManager(new LinearLayoutManager(getContext()));

        loadParticipants();
    }

    private void loadParticipants() {
        loading.setVisibility(View.VISIBLE);

        repository.getParticipants(eventId, new EventRepository.OnParticipantsLoadedListener() {
            @Override
            public void onLoaded(List<ParticipantData> participants) {
                loading.setVisibility(View.GONE);

                if (participants.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvParticipants.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvParticipants.setVisibility(View.VISIBLE);
                    rvParticipants.setAdapter(new ParticipantsAdapter(participants));
                }
            }

            @Override
            public void onError(String error) {
                loading.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error cargando lista: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ADAPTADOR INTERNO ---
    class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {
        List<ParticipantData> items;

        public ParticipantsAdapter(List<ParticipantData> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ParticipantData item = items.get(position);

            // Muestra el nombre real (o el ID si no hay nombre)
            holder.tvName.setText(item.userName);

            // Visibilidad y lógica del botón de remover
            if (isAdmin) {
                holder.btnRemove.setVisibility(View.VISIBLE);
                holder.btnRemove.setOnClickListener(v -> confirmDeletion(item, position));
            } else {
                holder.btnRemove.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void confirmDeletion(ParticipantData item, int position) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Eliminar Participante")
                    .setMessage("¿Estás seguro de eliminar a " + item.userName + " del evento? Se realizará el reembolso si aplica.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        repository.cancelRegistration(eventId, item.userId, 0, new EventRepository.OnRegistrationResultListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();
                                items.remove(position);
                                notifyItemRemoved(position);
                            }
                            @Override public void onEventFull() {}
                            @Override public void onAlreadyRegistered() {}
                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageButton btnRemove;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Vincula el nombre principal y el botón de remover (IDs del item_participant.xml)
                tvName = itemView.findViewById(R.id.tvParticipantName);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }
        }
    }
}
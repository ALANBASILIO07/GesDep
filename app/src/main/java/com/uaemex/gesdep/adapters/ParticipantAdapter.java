package com.uaemex.gesdep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton; // Importación necesaria
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.UserModel; // Usamos UserModel

import java.util.ArrayList;
import java.util.List;

// El nombre de la clase es ParticipantsAdapter (Plural)
public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    // CORRECCIÓN CLAVE: El constructor debe llamarse igual que la clase (ParticipantsAdapter)
    public ParticipantAdapter(OnUserClickListener listener) {
        this.userList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<UserModel> newUserList) {
        this.userList = newUserList;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvParticipantName, tvParticipantActivity, tvParticipantAge, tvParticipantPhone;
        private ImageButton btnRemove;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
            tvParticipantActivity = itemView.findViewById(R.id.tvParticipantActivity);
            tvParticipantAge = itemView.findViewById(R.id.tvParticipantAge);
            tvParticipantPhone = itemView.findViewById(R.id.tvParticipantPhone);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(userList.get(position));
                }
            });

            btnRemove.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "Remover: " + userList.get(getAdapterPosition()).getName(), Toast.LENGTH_SHORT).show();
            });
        }

        public void bind(UserModel user) {
            tvParticipantName.setText(user.getName() != null ? user.getName() : "Usuario Desconocido");

            // Mapeo: Rol/Tipo de usuario e Institución
            String userRole = user.getRole() != null ? user.getRole().toUpperCase() : "N/A";
            String userInstitution = user.getInstitution() != null && !user.getInstitution().isEmpty() ? user.getInstitution() : "N/A";

            tvParticipantActivity.setText("Tipo: " + userRole + " | Inst.: " + userInstitution);

            // Mapeo: Edad (Usamos Rol/Tipo ya que 'age' no existe en UserModel)
            tvParticipantAge.setText("Rol: " + userRole);

            // Mapeo: Teléfono
            tvParticipantPhone.setText("Tel: " + (user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "N/A"));

            // Lógica simple de visibilidad del botón
            btnRemove.setVisibility("admin".equalsIgnoreCase(userRole) ? View.GONE : View.VISIBLE);
        }
    }
}
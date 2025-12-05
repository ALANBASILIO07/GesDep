package com.uaemex.gesdep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.uaemex.gesdep.R;
import com.uaemex.gesdep.models.UserModel;

import java.util.ArrayList;
import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    public ParticipantAdapter(OnUserClickListener listener) {
        this.userList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() { return userList.size(); }

    public void updateUsers(List<UserModel> newUserList) {
        this.userList = newUserList;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvEmail, tvRole;
        private ImageView ivProfile;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvParticipantName);
            tvEmail = itemView.findViewById(R.id.tvParticipantEmail);
            tvRole = itemView.findViewById(R.id.tvParticipantRole);
            ivProfile = itemView.findViewById(R.id.ivProfile);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(userList.get(pos));
                }
            });
        }

        public void bind(UserModel user) {
            tvName.setText(user.getName() != null ? user.getName() : "Sin Nombre");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Sin Email");

            // --- FORMATEO DE ROLES ---
            String rawRole = user.getRole() != null ? user.getRole() : "user";
            String displayRole;
            switch (rawRole.toLowerCase()) {
                case "admin": displayRole = "ADMINISTRADOR"; break;
                case "coach": displayRole = "ENTRENADOR"; break;
                case "user":
                default: displayRole = "PARTICIPANTE"; break;
            }
            tvRole.setText(displayRole);

            // --- LÓGICA DE FOTO CORREGIDA (V4: Limpieza Total) ---
            if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                // CASO: HAY FOTO REAL
                // 1. Quitar padding para que la foto ocupe todo el espacio
                ivProfile.setPadding(0, 0, 0, 0);

                // 2. Quitar filtros de color (Tinte)
                ivProfile.setColorFilter(null);
                ivProfile.setImageTintList(null);

                // 3. CRÍTICO: Quitar el fondo (círculo verde) para evitar bordes o superposición
                ivProfile.setBackground(null);

                Glide.with(itemView.getContext())
                        .load(user.getProfilePhotoUrl())
                        .circleCrop()
                        // No usamos placeholder aquí para evitar parpadeos visuales con el fondo verde
                        .into(ivProfile);
            } else {
                // CASO: NO HAY FOTO (Usar icono Trofeo)
                // 1. Restaurar padding (10dp)
                int padding = (int) (10 * itemView.getContext().getResources().getDisplayMetrics().density);
                ivProfile.setPadding(padding, padding, padding, padding);

                // 2. Aplicar filtro BLANCO al icono (trofeo)
                ivProfile.setColorFilter(ContextCompat.getColor(itemView.getContext(), android.R.color.white));

                // 3. Restaurar fondo VERDE circular
                ivProfile.setBackgroundResource(R.drawable.bg_circle_green);

                // 4. Establecer el recurso de imagen
                ivProfile.setImageResource(R.drawable.ic_trophy);
            }
        }
    }
}
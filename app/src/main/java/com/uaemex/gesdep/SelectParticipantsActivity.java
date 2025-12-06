package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Importante para debug
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.TeamModel;
import com.uaemex.gesdep.models.UserModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectParticipantsActivity extends AppCompatActivity {

    private static final String TAG = "SelectParticipants"; // Tag para logs

    private RecyclerView rvUsers;
    private ProgressBar progressBar;
    private EditText etSearch;
    private MaterialButton btnAddSelected;
    // Agrega un TextView en tu XML para mostrar "No se encontraron usuarios" si quieres ser más explícito
    // private TextView tvEmptyState;

    private FirebaseFirestore db;
    private UserAdapter adapter;
    private List<UserModel> allUsers = new ArrayList<>();
    private List<UserModel> filteredUsers = new ArrayList<>();
    private List<UserModel> selectedUsers = new ArrayList<>();

    private TeamModel team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_participants);
        WindowUtils.setGreenStatusBar(this);

        team = (TeamModel) getIntent().getSerializableExtra("teamModel");
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        loadUsers();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        etSearch = findViewById(R.id.etSearch);
        btnAddSelected = findViewById(R.id.btnAddSelected);

        adapter = new UserAdapter(filteredUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddSelected.setOnClickListener(v -> addSelectedMembers());
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Iniciando carga de usuarios con rol 'user'");

        db.collection("users")
                .whereEqualTo("role", "user") // Asegúrate que en Firebase el campo sea "role" y el valor "user"
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allUsers.clear();
                    Log.d(TAG, "Usuarios encontrados en BD: " + querySnapshot.size());

                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) {
                            // Asignar UID si no viene en el objeto
                            user.setUid(doc.getId());

                            // Excluir si ya es miembro
                            if (!isMember(user.getUid())) {
                                allUsers.add(user);
                            } else {
                                Log.d(TAG, "Usuario " + user.getName() + " ya es miembro, omitiendo.");
                            }
                        }
                    }

                    Log.d(TAG, "Usuarios disponibles para agregar: " + allUsers.size());
                    filterUsers(""); // Mostrar todos al inicio
                    progressBar.setVisibility(View.GONE);

                    if (allUsers.isEmpty()) {
                        Toast.makeText(this, "No hay participantes disponibles para agregar.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error cargando usuarios", e);
                    Toast.makeText(this, "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isMember(String userId) {
        if (team == null || team.members == null) return false;
        for (TeamModel.TeamMember m : team.members) {
            if (m.id != null && m.id.equals(userId)) return true;
        }
        return false;
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (UserModel user : allUsers) {
                boolean nameMatch = user.getName() != null && user.getName().toLowerCase().contains(lowerQuery);
                boolean emailMatch = user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery);

                if (nameMatch || emailMatch) {
                    filteredUsers.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void addSelectedMembers() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        for (UserModel user : selectedUsers) {
            TeamModel.TeamMember member = new TeamModel.TeamMember(
                    user.getUid(),
                    user.getName(),
                    user.getEmail(),
                    "",
                    0,
                    "Miembro"
            );
            team.addMember(member);
        }

        // Guardar cambios en Firestore
        db.collection("teams").document(team.id)
                .set(team)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Miembros agregados exitosamente", Toast.LENGTH_SHORT).show();
                    // IMPORTANTE: Devolver resultado OK para que la vista anterior se actualice si implementas onActivityResult
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Adaptador
    class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<UserModel> list;

        public UserAdapter(List<UserModel> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_selection, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserModel user = list.get(position);
            holder.tvName.setText(user.getName());
            holder.tvEmail.setText(user.getEmail());

            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(selectedUsers.contains(user));
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedUsers.add(user);
                else selectedUsers.remove(user);
            });

            // Permitir clic en todo el item para marcar el checkbox
            holder.itemView.setOnClickListener(v -> holder.checkBox.setChecked(!holder.checkBox.isChecked()));
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail;
            CheckBox checkBox;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                checkBox = itemView.findViewById(R.id.checkBox);
            }
        }
    }
}
package com.uaemex.gesdep;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.TeamModel;
import com.uaemex.gesdep.utils.WindowUtils;

public class CreateTeamActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etTeamName, etMinMembers, etMaxMembers, etUniformColor, etInstitution, etNotes;
    private AutoCompleteTextView actvDiscipline, actvCategory;
    private MaterialButton btnCreateTeam;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String currentUserName, currentUserEmail, currentUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        WindowUtils.setGreenStatusBar(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        setupDropdowns();
        loadCoachInfo();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTeamName = findViewById(R.id.etTeamName);
        actvDiscipline = findViewById(R.id.actvDiscipline);
        actvCategory = findViewById(R.id.actvCategory);
        etMinMembers = findViewById(R.id.etMinMembers);
        etMaxMembers = findViewById(R.id.etMaxMembers);
        etUniformColor = findViewById(R.id.etUniformColor);
        etInstitution = findViewById(R.id.etInstitution);
        etNotes = findViewById(R.id.etNotes);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDropdowns() {
        // Disciplinas deportivas
        String[] disciplines = {
                "Fútbol", "Básquetbol", "Voleibol", "Béisbol", "Atletismo",
                "Natación", "Tenis", "Bádminton", "Gimnasia", "Danza", "Ajedrez", "Otro"
        };
        ArrayAdapter<String> disciplineAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, disciplines);
        actvDiscipline.setAdapter(disciplineAdapter);

        // Categorías
        String[] categories = {"Varonil", "Femenil", "Mixto"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(categoryAdapter);
    }

    private void loadCoachInfo() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentUserName = doc.getString("name");
                        currentUserEmail = doc.getString("email");
                        currentUserPhone = doc.getString("phone");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar información", Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        btnCreateTeam.setOnClickListener(v -> validateAndCreate());
    }

    private void validateAndCreate() {
        String teamName = etTeamName.getText().toString().trim();
        String discipline = actvDiscipline.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String minMembersStr = etMinMembers.getText().toString().trim();
        String maxMembersStr = etMaxMembers.getText().toString().trim();
        String uniformColor = etUniformColor.getText().toString().trim();
        String institution = etInstitution.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Validaciones
        if (teamName.isEmpty()) {
            etTeamName.setError("Requerido");
            return;
        }
        if (discipline.isEmpty()) {
            Toast.makeText(this, "Selecciona una disciplina", Toast.LENGTH_SHORT).show();
            return;
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }
        if (minMembersStr.isEmpty()) {
            etMinMembers.setError("Requerido");
            return;
        }
        if (maxMembersStr.isEmpty()) {
            etMaxMembers.setError("Requerido");
            return;
        }

        int minMembers = Integer.parseInt(minMembersStr);
        int maxMembers = Integer.parseInt(maxMembersStr);

        if (minMembers < 1) {
            etMinMembers.setError("Debe ser al menos 1");
            return;
        }
        if (maxMembers < minMembers) {
            etMaxMembers.setError("Debe ser mayor o igual al mínimo");
            return;
        }

        createTeam(teamName, discipline, category, minMembers, maxMembers, uniformColor, institution, notes);
    }

    private void createTeam(String teamName, String discipline, String category,
                            int minMembers, int maxMembers, String uniformColor,
                            String institution, String notes) {
        progressBar.setVisibility(View.VISIBLE);
        btnCreateTeam.setEnabled(false);

        String coachId = auth.getCurrentUser().getUid();

        // Crear referencia en Firestore
        DocumentReference teamRef = db.collection("teams").document();
        String teamId = teamRef.getId();

        // Crear modelo de equipo
        TeamModel team = new TeamModel(
                teamId, teamName, discipline, category,
                coachId, currentUserName, currentUserEmail, currentUserPhone,
                minMembers, maxMembers
        );
        team.uniformColor = uniformColor;
        team.institution = institution;
        team.notes = notes;

        // Guardar en Firestore
        teamRef.set(team)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Equipo creado exitosamente", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateTeam.setEnabled(true);
                    Toast.makeText(this, "Error al crear equipo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

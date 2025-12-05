package com.uaemex.gesdep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword, etOrgCode;
    private RadioGroup rgUserType;
    private Button btnRegister;
    private ProgressBar progressBar;
    private View tilOrgCode;
    private ImageButton btnBack;
    private TextView tvGoToLogin;

    private ImageView ivRegisterProfile;
    private CardView btnPickPhoto;
    private ScrollView registerScrollView;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri selectedImageUri = null;

    private static final String CODE_ORGANIZER = "ADMIN2025";
    private static final String CODE_COACH = "ENTRENADOR2025";

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();

                    // --- LIMPIEZA DE TINTES AQUÍ ---
                    ivRegisterProfile.setColorFilter(null);
                    ivRegisterProfile.setImageTintList(null);
                    ivRegisterProfile.setPadding(0, 0, 0, 0);

                    Glide.with(this)
                            .load(selectedImageUri)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivRegisterProfile);
                }
            }
    );

    // ... (El resto de tu código onCreate, initViews, listeners, validaciones,
    // firebase auth, etc. SE MANTIENE EXACTAMENTE IGUAL) ...
    // Solo asegurate de usar la versión completa que tenías antes, con el cambio en galleryLauncher.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance("gesdep");
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        initViews();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etOrgCode = findViewById(R.id.etOrgCode);
        rgUserType = findViewById(R.id.rgUserType);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tilOrgCode = findViewById(R.id.tilOrgCode);
        btnBack = findViewById(R.id.btnBack);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        ivRegisterProfile = findViewById(R.id.ivRegisterProfile);
        btnPickPhoto = findViewById(R.id.btnPickPhoto);
        registerScrollView = findViewById(R.id.registerScrollView);

        tilOrgCode.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        View.OnClickListener pickPhotoListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        };
        btnPickPhoto.setOnClickListener(pickPhotoListener);
        ivRegisterProfile.setOnClickListener(pickPhotoListener);

        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOrganizer || checkedId == R.id.rbCoach) {
                tilOrgCode.setVisibility(View.VISIBLE);
                etOrgCode.setHint(checkedId == R.id.rbOrganizer ? "Código de Organizador" : "Código de Entrenador");
                registerScrollView.post(() -> registerScrollView.smoothScrollTo(0, tilOrgCode.getBottom()));
            } else {
                tilOrgCode.setVisibility(View.GONE);
                etOrgCode.setText("");
            }
        });

        View.OnFocusChangeListener focusListener = (view, hasFocus) -> {
            if (hasFocus) {
                View parent = (View) view.getParent().getParent();
                scrollToView(parent);
            }
        };

        etName.setOnFocusChangeListener(focusListener);
        etEmail.setOnFocusChangeListener(focusListener);
        etPassword.setOnFocusChangeListener(focusListener);
        etConfirmPassword.setOnFocusChangeListener(focusListener);
        etOrgCode.setOnFocusChangeListener(focusListener);

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void scrollToView(final View view) {
        registerScrollView.postDelayed(() -> {
            int y = view.getTop();
            registerScrollView.smoothScrollTo(0, y);
        }, 200);
    }

    private void validateAndRegister() {
        String fullName = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String orgCode = etOrgCode.getText().toString().trim();

        if (fullName.isEmpty()) { etName.setError("Requerido"); return; }
        if (email.isEmpty()) { etEmail.setError("Requerido"); return; }
        if (password.isEmpty() || password.length() < 6) { etPassword.setError("Mínimo 6 caracteres"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("No coinciden"); return; }

        int selectedRole = rgUserType.getCheckedRadioButtonId();
        String userRole = "user";

        if (selectedRole == R.id.rbOrganizer) {
            if (!orgCode.equals(CODE_ORGANIZER)) {
                etOrgCode.setError("Código incorrecto"); return;
            }
            userRole = "admin";
        } else if (selectedRole == R.id.rbCoach) {
            if (!orgCode.equals(CODE_COACH)) {
                etOrgCode.setError("Código incorrecto"); return;
            }
            userRole = "coach";
        }

        registerInAuth(fullName, email, password, userRole);
    }

    private void registerInAuth(String name, String email, String password, String role) {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        if (selectedImageUri != null) {
                            uploadImageAndSaveData(user.getUid(), name, email, role);
                        } else {
                            saveUserToFirestore(user.getUid(), name, email, role, null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void uploadImageAndSaveData(String uid, String name, String email, String role) {
        StorageReference fileRef = storageRef.child(uid + ".jpg");

        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveUserToFirestore(uid, name, email, role, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    saveUserToFirestore(uid, name, email, role, null);
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String role, String photoUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        user.put("createdAt", System.currentTimeMillis());
        user.put("active", true);

        if (photoUrl != null) {
            user.put("photoUrl", photoUrl);
        }

        if (role.equals("admin")) {
            user.put("eventsOrganized", 0);
        } else {
            user.put("eventsParticipated", 0);
        }

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_LONG).show();
                    auth.signOut();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Error guardando datos", Toast.LENGTH_SHORT).show();
                });
    }
}
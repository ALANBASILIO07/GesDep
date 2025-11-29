package com.uaemex.gesdep;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FullScreenVideoView videoView;
    private int currentVideoIndex = 0;
    private final int[] videoResources = {
        R.raw.video1,
        R.raw.video2,
        R.raw.video3,
        R.raw.video4,
        R.raw.video5,
        R.raw.video6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "onCreate iniciado");

            // Inicializar Firebase
            Log.d(TAG, "Inicializando Firebase...");
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance("gesdep");
            Log.d(TAG, "Firebase inicializado correctamente");

            // Verificar si el usuario ya esta autenticado
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "Usuario autenticado encontrado: " + currentUser.getEmail());
                // Usuario ya autenticado, verificar su rol y redirigir
                checkUserRoleAndRedirect(currentUser.getUid());
                return;
            }

            Log.d(TAG, "No hay usuario autenticado, mostrando pantalla de bienvenida");
            // No hay usuario autenticado, mostrar pantalla de bienvenida
            setupWelcomeScreen();

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error al iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBackgroundVideo() {
        try {
            Log.d(TAG, "Configurando video de fondo...");
            playCurrentVideo();

            // Configurar listener para cuando termine cada video
            videoView.setOnCompletionListener(mp -> {
                Log.d(TAG, "Video completado, pasando al siguiente");
                currentVideoIndex = (currentVideoIndex + 1) % videoResources.length;
                playCurrentVideo();
            });

            // Manejo de errores
            videoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error al reproducir video - what: " + what + ", extra: " + extra);
                Toast.makeText(this, "No se pudo cargar el video de fondo", Toast.LENGTH_SHORT).show();
                // Intentar con el siguiente video
                currentVideoIndex = (currentVideoIndex + 1) % videoResources.length;
                playCurrentVideo();
                return true;
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar video: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void playCurrentVideo() {
        try {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResources[currentVideoIndex]);
            videoView.setVideoURI(uri);
            Log.d(TAG, "Reproduciendo video " + (currentVideoIndex + 1) + " de " + videoResources.length + ": " + uri);

            videoView.setOnPreparedListener(mp -> {
                Log.d(TAG, "Video " + (currentVideoIndex + 1) + " preparado, iniciando reproduccion");
                mp.setLooping(false); // No loop individual, manejamos la secuencia manualmente
                mp.setVolume(0f, 0f); // Sin sonido
                videoView.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());
                videoView.start();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al reproducir video " + (currentVideoIndex + 1) + ": " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume llamado");
        if (videoView != null && !videoView.isPlaying()) {
            Log.d(TAG, "Reanudando video");
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause llamado");
        if (videoView != null && videoView.isPlaying()) {
            Log.d(TAG, "Pausando video");
            videoView.pause();
        }
    }

    private void checkUserRoleAndRedirect(String uid) {
        Log.d(TAG, "Verificando rol del usuario: " + uid);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        if (role == null) role = "user";
                        Log.d(TAG, "Rol encontrado: " + role);
                        redirectToHome(role);
                    } else {
                        // No existe documento de usuario, cerrar sesion y mostrar pantalla
                        Log.w(TAG, "No existe documento de usuario en Firestore, cerrando sesion");
                        Toast.makeText(this, "No se encontró información del usuario", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        setupWelcomeScreen();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al leer usuario, cerrar sesion y mostrar pantalla
                    Log.e(TAG, "Error al leer documento de usuario: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al verificar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    auth.signOut();
                    setupWelcomeScreen();
                });
    }

    private void setupWelcomeScreen() {
        try {
            Log.d(TAG, "Configurando pantalla de bienvenida...");
            setContentView(R.layout.activity_welcome);
            Log.d(TAG, "Layout activity_welcome cargado");

            // Configurar video de fondo
            videoView = findViewById(R.id.videoViewBackground);
            if (videoView == null) {
                Log.e(TAG, "videoViewBackground no encontrado en el layout");
                Toast.makeText(this, "Error: No se encontró el componente de video", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "videoViewBackground encontrado, configurando video...");
                setupBackgroundVideo();
            }

            Button btnLogin = findViewById(R.id.btnLogin);
            Button btnRegister = findViewById(R.id.btnRegister);

            if (btnLogin == null || btnRegister == null) {
                Log.e(TAG, "Botones no encontrados en el layout");
                Toast.makeText(this, "Error: Botones no encontrados", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Botones encontrados, configurando listeners");

            btnLogin.setOnClickListener(v -> {
                Log.d(TAG, "Boton Login presionado");
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            });

            btnRegister.setOnClickListener(v -> {
                Log.d(TAG, "Boton Register presionado");
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            });

            Log.d(TAG, "Pantalla de bienvenida configurada exitosamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar pantalla de bienvenida: " + e.getMessage(), e);
            Toast.makeText(this, "Error al cargar pantalla: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void redirectToHome(String role) {
        try {
            Log.d(TAG, "Redirigiendo a pantalla principal para rol: " + role);
            Intent intent;
            if (role.equals("admin")) {
                Log.d(TAG, "Iniciando AdminHomeActivity");
                intent = new Intent(this, AdminHomeActivity.class);
            } else if (role.equals("coach")) {
                Log.d(TAG, "Iniciando CoachHomeActivity");
                intent = new Intent(this, CoachHomeActivity.class);
            } else {
                Log.d(TAG, "Iniciando UserHomeActivity");
                intent = new Intent(this, UserHomeActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Log.d(TAG, "Activity iniciada exitosamente, finalizando WelcomeActivity");
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error al redirigir a pantalla principal: " + e.getMessage(), e);
            Toast.makeText(this, "Error al abrir pantalla: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

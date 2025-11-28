package com.uaemex.gesdep;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
// Importante: Ya no usamos el VideoView estándar, sino el nuestro
// import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister;
    private FirebaseAuth auth;

    // CAMBIO 1: Usar tu componente personalizado
    private FullScreenVideoView videoView;

    // Lista de videos en la carpeta raw
    private int[] videos = {
            R.raw.video1,
            R.raw.video2,
            R.raw.video3,
            R.raw.video4,
            R.raw.video5,
            R.raw.video6
    };
    private int currentVideoIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Lógica de Autenticación
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            checkUserRoleAndRedirect(currentUser.getUid());
            return;
        }

        // 2. Cargar Vista
        setContentView(R.layout.activity_welcome);

        // 3. Inicializar Componentes de UI
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // CAMBIO 2: Vincular con el ID del layout (asegúrate de que en el XML sea <com.uaemex.gesdep.FullScreenVideoView>)
        videoView = findViewById(R.id.videoViewBackground);

        // 4. Configurar Botones
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // 5. Iniciar Video de Fondo
        setupVideoBackground();
    }

    private void setupVideoBackground() {
        // CAMBIO 3: Simplificación. Ya no necesitamos la matemática compleja
        // porque FullScreenVideoView fuerza el tamaño en su método onMeasure.

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            // Iniciamos directamente, el componente se estira solo.
            videoView.start();
        });

        // Listener para cambiar de video al terminar (Loop de videos)
        videoView.setOnCompletionListener(mp -> {
            currentVideoIndex++;
            if (currentVideoIndex >= videos.length) {
                currentVideoIndex = 0;
            }
            playNextVideo();
        });

        // Iniciar el primero
        playNextVideo();
    }

    private void playNextVideo() {
        String videoPath = "android.resource://" + getPackageName() + "/" + videos[currentVideoIndex];
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        // No es necesario llamar a start() aquí, el onPrepared lo hará
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            videoView.pause();
        }
    }

    private void checkUserRoleAndRedirect(String userId) {
        // Lógica temporal de redirección
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
package com.uaemex.gesdep.utils; // Ajusta esto si no usas subcarpetas

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.content.ContextCompat;
import com.uaemex.gesdep.R; // Importante para acceder a R.color

public class WindowUtils {

    public static void setGreenStatusBar(Activity activity) {
        Window window = activity.getWindow();
        // Esto permite pintar la barra de estado
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // Pone el color verde definido en colors.xml
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.green_primary));
    }
}
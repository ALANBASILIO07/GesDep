package com.uaemex.gesdep;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.uaemex.gesdep.utils.WindowUtils;

public class RechargeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Implement recharge view
        WindowUtils.setGreenStatusBar(this);
        Toast.makeText(this, "Función de recarga próximamente", Toast.LENGTH_LONG).show();
        finish(); // Temporal: return immediately
    }
}

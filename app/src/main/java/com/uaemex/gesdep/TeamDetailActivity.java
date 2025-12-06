package com.uaemex.gesdep;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.uaemex.gesdep.utils.WindowUtils;

public class TeamDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Implement team detail view
        WindowUtils.setGreenStatusBar(this);
        finish(); // Temporal: return immediately
    }
}

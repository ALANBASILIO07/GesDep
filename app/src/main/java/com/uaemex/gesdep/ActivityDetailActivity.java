package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityDetailActivity extends AppCompatActivity {

    private TextView tvName, tvCoach, tvSchedule, tvLocation;
    private Button btnAttendance;
    private FirebaseFirestore db;
    private String activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvName = findViewById(R.id.tvName);
        tvCoach = findViewById(R.id.tvCoach);
        tvSchedule = findViewById(R.id.tvSchedule);
        tvLocation = findViewById(R.id.tvLocation);
        btnAttendance = findViewById(R.id.btnAttendance);

        db = FirebaseFirestore.getInstance();
        activityId = getIntent().getStringExtra("activityId");

        loadDetail();

        btnAttendance.setOnClickListener(v -> {
            Intent i = new Intent(this, AttendanceActivity.class);
            i.putExtra("activityId", activityId);
            startActivity(i);
        });
    }

    private void loadDetail() {
        db.collection("activities").document(activityId)
                .get()
                .addOnSuccessListener(d -> {
                    ActivityModel m = d.toObject(ActivityModel.class);
                    if (m != null) {
                        tvName.setText(m.name);
                        tvCoach.setText(m.coachName);
                        tvSchedule.setText(m.schedule);
                        tvLocation.setText(m.place);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

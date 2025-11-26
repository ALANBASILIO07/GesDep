package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityDetailActivity extends AppCompatActivity {

    private EditText etName, etCoach, etSchedule, etLocation;
    private Button btnAttendance;
    private FirebaseFirestore db;
    private String activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        etName = findViewById(R.id.etName);
        etCoach = findViewById(R.id.etCoach);
        etSchedule = findViewById(R.id.etSchedule);
        etLocation = findViewById(R.id.etLocation);
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
                        etName.setText(m.name);
                        etCoach.setText(m.coachName);
                        etSchedule.setText(m.schedule);
                        etLocation.setText(m.place);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

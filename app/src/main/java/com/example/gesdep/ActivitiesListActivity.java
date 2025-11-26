package com.example.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private final List<ActivityModel> list = new ArrayList<>();
    private ActivitiesAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_list);

        rv = findViewById(R.id.rvActivities);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivitiesAdapter(list, this::openDetail);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadActivities();
    }

    private void loadActivities() {
        db.collection("activities")
                .get()
                .addOnSuccessListener(snapshot -> {
                    list.clear();
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                        ActivityModel m = d.toObject(ActivityModel.class);
                        if (m != null) {
                            m.id = d.getId();
                            list.add(m);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openDetail(ActivityModel m) {
        Intent i = new Intent(this, ActivityDetailActivity.class);
        i.putExtra("activityId", m.id);
        startActivity(i);
    }
}

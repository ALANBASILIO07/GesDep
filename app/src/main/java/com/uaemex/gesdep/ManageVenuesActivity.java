package com.uaemex.gesdep;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.VenueModel;
import com.uaemex.gesdep.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

public class ManageVenuesActivity extends AppCompatActivity {

    private RecyclerView rvVenues;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddVenue;
    private FirebaseFirestore db;
    private VenuesAdapter adapter;
    private List<VenueModel> venuesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_venues);

        WindowUtils.setGreenStatusBar(this);
        db = FirebaseFirestore.getInstance("gesdep");

        initViews();
        loadVenues();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVenues(); // Recargar al volver de crear una sede
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvVenues = findViewById(R.id.rvVenues);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddVenue = findViewById(R.id.fabAddVenue);

        venuesList = new ArrayList<>();
        adapter = new VenuesAdapter(venuesList);
        rvVenues.setLayoutManager(new LinearLayoutManager(this));
        rvVenues.setAdapter(adapter);

        fabAddVenue.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateVenueActivity.class));
        });
    }

    private void loadVenues() {
        db.collection("venues").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    venuesList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            VenueModel venue = doc.toObject(VenueModel.class);
                            if (venue != null) {
                                venue.setId(doc.getId());
                                venuesList.add(venue);
                            }
                        }
                        tvEmptyState.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar sedes", Toast.LENGTH_SHORT).show());
    }

    // --- ADAPTADOR INTERNO ---
    class VenuesAdapter extends RecyclerView.Adapter<VenuesAdapter.VenueViewHolder> {
        private List<VenueModel> list;

        public VenuesAdapter(List<VenueModel> list) { this.list = list; }

        @NonNull
        @Override
        public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venue, parent, false);
            return new VenueViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
            VenueModel venue = list.get(position);
            holder.tvName.setText(venue.getName());
            holder.tvAddress.setText(venue.getAddress());

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageVenuesActivity.this)
                        .setTitle("Eliminar Sede")
                        .setMessage("¿Estás seguro de eliminar " + venue.getName() + "?")
                        .setPositiveButton("Eliminar", (dialog, which) -> deleteVenue(venue.getId()))
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VenueViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAddress;
            View btnDelete;
            public VenueViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvVenueName);
                tvAddress = itemView.findViewById(R.id.tvVenueAddress);
                btnDelete = itemView.findViewById(R.id.btnDeleteVenue);
            }
        }
    }

    private void deleteVenue(String id) {
        db.collection("venues").document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Sede eliminada", Toast.LENGTH_SHORT).show();
                    loadVenues();
                });
    }
}
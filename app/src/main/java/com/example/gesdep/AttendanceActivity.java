package com.example.gesdep;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 100;

    private GoogleMap mMap;
    private FusedLocationProviderClient fused;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String activityId;
    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        activityId = getIntent().getStringExtra("activityId");

        fused = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MaterialButton btn = findViewById(R.id.btnRegisterAttendance);
        btn.setOnClickListener(v -> saveAttendance());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestLocation();
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fused.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
                LatLng here = new LatLng(lat, lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 16f));
                mMap.addMarker(new MarkerOptions().position(here).title("Tu ubicación"));
            }
        });
    }

    private void saveAttendance() {
        if (lat == 0 && lng == 0) {
            Toast.makeText(this, "Ubicación no disponible aún", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", activityId);
        data.put("userId", auth.getUid());
        data.put("timestamp", FieldValue.serverTimestamp());
        data.put("lat", lat);
        data.put("lng", lng);

        db.collection("attendance").add(data)
                .addOnSuccessListener(d ->
                        Toast.makeText(this, "Asistencia registrada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }
}

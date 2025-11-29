package com.uaemex.gesdep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.uaemex.gesdep.utils.WindowUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvAddress;
    private Button btnConfirm;
    private FloatingActionButton fabMyLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLatLng;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        WindowUtils.setGreenStatusBar(this);

        tvAddress = findViewById(R.id.tvAddress);
        btnConfirm = findViewById(R.id.btnConfirmLocation);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirm.setOnClickListener(v -> confirmSelection());
        fabMyLocation.setOnClickListener(v -> getCurrentLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Mover a una posición inicial (Toluca por defecto si no hay GPS)
        LatLng toluca = new LatLng(19.2826, -99.6557);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toluca, 13f));

        // Listener al mover el mapa
        mMap.setOnCameraIdleListener(() -> {
            selectedLatLng = mMap.getCameraPosition().target;
            getAddressFromLocation(selectedLatLng.latitude, selectedLatLng.longitude);
        });

        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 16f));
            }
        });
    }

    private void getAddressFromLocation(double lat, double lng) {
        tvAddress.setText("Cargando dirección...");
        btnConfirm.setEnabled(false);

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(MapPickerActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addressObj = addresses.get(0);
                    String addressLine = addressObj.getAddressLine(0);
                    selectedAddress = addressLine;

                    runOnUiThread(() -> {
                        tvAddress.setText(selectedAddress);
                        btnConfirm.setEnabled(true);
                    });
                } else {
                    runOnUiThread(() -> {
                        tvAddress.setText("Ubicación sin nombre (" + String.format("%.4f", lat) + ", " + String.format("%.4f", lng) + ")");
                        selectedAddress = "Coordenadas: " + lat + ", " + lng;
                        btnConfirm.setEnabled(true);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvAddress.setText("Sin dirección (Solo Coordenadas)");
                    selectedAddress = "Lat: " + lat + ", Lng: " + lng;
                    btnConfirm.setEnabled(true);
                });
            }
        }).start();
    }

    private void confirmSelection() {
        if (selectedLatLng == null) return;
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", selectedLatLng.latitude);
        resultIntent.putExtra("longitude", selectedLatLng.longitude);
        resultIntent.putExtra("address", selectedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
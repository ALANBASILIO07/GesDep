package com.uaemex.gesdep.models;

import java.io.Serializable;

public class VenueModel implements Serializable {
    private String id;
    private String name;        // Ej: "Cancha de Futbol 1"
    private String address;     // Dirección legible
    private double latitude;
    private double longitude;
    private String imageUrl;    // Foto de la cancha (Opcional)

    // Constructor vacío para Firebase
    public VenueModel() {}

    // Constructor completo
    public VenueModel(String id, String name, String address, double latitude, double longitude, String imageUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Para mostrar el nombre en el Spinner (Dropdown) automáticamente
    @Override
    public String toString() {
        return name;
    }
}
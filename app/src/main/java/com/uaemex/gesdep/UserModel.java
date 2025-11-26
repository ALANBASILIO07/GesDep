package com.uaemex.gesdep;

import com.google.firebase.Timestamp;

/**
 * Modelo de Usuario actualizado
 * Incluye campos para el sistema completo de gestión de eventos
 */
public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String role; // "admin" o "user"
    private String userType; // "admin", "user", "team_leader" (responsable de equipo)
    private String phone;
    private String profilePhotoUrl;
    private String institution; // Institución a la que pertenece (escuela, empresa, etc.)
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // Token para notificaciones push
    private String fcmToken;

    // Estadísticas
    private int eventsRegistered; // Eventos a los que se ha registrado
    private int eventsCompleted; // Eventos completados
    private int teamsLeading; // Equipos que lidera (si es team_leader)

    public UserModel() { }

    public UserModel(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.userType = role; // Por defecto userType = role
        this.createdAt = Timestamp.now();
        this.lastLogin = Timestamp.now();
        this.eventsRegistered = 0;
        this.eventsCompleted = 0;
        this.teamsLeading = 0;
    }

    // Constructor completo
    public UserModel(String uid, String name, String email, String role, String userType,
                     String phone, String profilePhotoUrl, String institution) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.userType = userType;
        this.phone = phone;
        this.profilePhotoUrl = profilePhotoUrl;
        this.institution = institution;
        this.createdAt = Timestamp.now();
        this.lastLogin = Timestamp.now();
        this.eventsRegistered = 0;
        this.eventsCompleted = 0;
        this.teamsLeading = 0;
    }

    // Getters
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getUserType() { return userType; }
    public String getPhone() { return phone; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public String getInstitution() { return institution; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getLastLogin() { return lastLogin; }
    public String getFcmToken() { return fcmToken; }
    public int getEventsRegistered() { return eventsRegistered; }
    public int getEventsCompleted() { return eventsCompleted; }
    public int getTeamsLeading() { return teamsLeading; }

    // Setters
    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public void setInstitution(String institution) { this.institution = institution; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setEventsRegistered(int eventsRegistered) { this.eventsRegistered = eventsRegistered; }
    public void setEventsCompleted(int eventsCompleted) { this.eventsCompleted = eventsCompleted; }
    public void setTeamsLeading(int teamsLeading) { this.teamsLeading = teamsLeading; }

    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    /**
     * Verifica si el usuario es líder de equipo
     */
    public boolean isTeamLeader() {
        return "team_leader".equals(userType);
    }

    /**
     * Incrementa el contador de eventos registrados
     */
    public void incrementEventsRegistered() {
        this.eventsRegistered++;
    }

    /**
     * Incrementa el contador de eventos completados
     */
    public void incrementEventsCompleted() {
        this.eventsCompleted++;
    }

    /**
     * Incrementa el contador de equipos que lidera
     */
    public void incrementTeamsLeading() {
        this.teamsLeading++;
    }
}

package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.Date;
import com.google.firebase.firestore.Exclude;

/**
 * Modelo de Usuario con campos de crédito y correcciones de mapeo de Firestore.
 */
public class UserModel implements Serializable {

    private String uid;
    private String name;
    private String email;
    private String role;
    private String userType;
    private String phone;

    // Si el campo en DB se llama 'photoUrl' pero en Java es 'profilePhotoUrl',
    // usamos @PropertyName para mapear.
    private String profilePhotoUrl;

    private String institution;

    // --- CRÍTICO: Usamos Date ---
    private Date createdAt;
    private Date lastLogin;

    // Token y Crédito
    private String fcmToken;
    private double appCredit = 0.0; // Saldo disponible

    // Estadísticas
    private int eventsRegistered;
    private int eventsCompleted;
    private int teamsLeading;

    public UserModel() {
        this.appCredit = 0.0;
        this.eventsRegistered = 0;
        this.eventsCompleted = 0;
        this.teamsLeading = 0;
    }

    // --- Constructor usado para Firebase Auth / Default ---
    public UserModel(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.userType = role;
        this.createdAt = new Date();
        this.lastLogin = new Date();
        this.appCredit = 0.0;
        this.eventsRegistered = 0;
        this.eventsCompleted = 0;
        this.teamsLeading = 0;
    }

    // --- Constructor completo ---
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
        this.createdAt = new Date();
        this.lastLogin = new Date();
        this.appCredit = 0.0;
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

    @PropertyName("photoUrl")
    public String getProfilePhotoUrl() { return profilePhotoUrl; }

    public String getInstitution() { return institution; }

    // Getters CRÍTICOS (Usan Date)
    public Date getCreatedAt() { return createdAt; }
    public Date getLastLogin() { return lastLogin; }
    public double getAppCredit() { return appCredit; }

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

    @PropertyName("photoUrl")
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public void setInstitution(String institution) { this.institution = institution; }

    // Setters CRÍTICOS (Usan Date)
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }
    public void setAppCredit(double appCredit) { this.appCredit = appCredit; }

    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setEventsRegistered(int eventsRegistered) { this.eventsRegistered = eventsRegistered; }
    public void setEventsCompleted(int eventsCompleted) { this.eventsCompleted = eventsCompleted; }
    public void setTeamsLeading(int teamsLeading) { this.teamsLeading = teamsLeading; }

    public boolean isAdmin() { return "admin".equals(role); }
    public boolean isTeamLeader() { return "team_leader".equals(userType); }
    public void incrementEventsRegistered() { this.eventsRegistered++; }
    public void incrementEventsCompleted() { this.eventsCompleted++; }
    public void incrementTeamsLeading() { this.teamsLeading++; }
}
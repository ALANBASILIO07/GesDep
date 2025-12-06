package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.Date;
import com.google.firebase.firestore.Exclude;

/**
 * Modelo de Usuario ROBUSTO
 * Soluciona el error: java.lang.RuntimeException: Failed to convert value of type java.lang.Long to Date
 */
public class UserModel implements Serializable {

    private String uid;
    private String name;
    private String email;
    private String role;
    private String userType;
    private String phone;

    // Mapeo especial para foto de perfil
    @PropertyName("photoUrl")
    private String profilePhotoUrl;

    private String institution;

    // --- CAMPOS DE FECHA PRIVADOS (Para forzar el uso de setters inteligentes) ---
    private Date createdAt;
    private Date lastLogin;
    // ----------------------------------------------------------------------------

    // Token y Crédito
    private String fcmToken;
    private double appCredit = 0.0;

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

    // Getters y Setters normales
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Helper para compatibilidad si alguna vista llama a getPhoneNumber()
    @Exclude
    public String getPhoneNumber() { return phone; }

    @PropertyName("photoUrl")
    public String getProfilePhotoUrl() { return profilePhotoUrl; }

    @PropertyName("photoUrl")
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }


    // --- SECCIÓN CRÍTICA: MANEJO DE FECHAS ROBUSTO ---

    @PropertyName("createdAt")
    public Date getCreatedAt() { return createdAt; }

    @PropertyName("createdAt")
    public void setCreatedAt(Object timestamp) {
        this.createdAt = convertToDate(timestamp);
    }

    @PropertyName("lastLogin")
    public Date getLastLogin() { return lastLogin; }

    @PropertyName("lastLogin")
    public void setLastLogin(Object timestamp) {
        this.lastLogin = convertToDate(timestamp);
    }

    // Método mágico que convierte Long o Timestamp a Date
    private Date convertToDate(Object timestamp) {
        if (timestamp == null) return null;
        if (timestamp instanceof Date) {
            return (Date) timestamp;
        } else if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).toDate();
        } else if (timestamp instanceof Long) {
            return new Date((Long) timestamp);
        }
        return null;
    }
    // ---------------------------------------------------

    public double getAppCredit() { return appCredit; }
    public void setAppCredit(double appCredit) { this.appCredit = appCredit; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public int getEventsRegistered() { return eventsRegistered; }
    public void setEventsRegistered(int eventsRegistered) { this.eventsRegistered = eventsRegistered; }

    public int getEventsCompleted() { return eventsCompleted; }
    public void setEventsCompleted(int eventsCompleted) { this.eventsCompleted = eventsCompleted; }

    public int getTeamsLeading() { return teamsLeading; }
    public void setTeamsLeading(int teamsLeading) { this.teamsLeading = teamsLeading; }

    @Exclude
    public boolean isAdmin() { return "admin".equals(role); }
    @Exclude
    public boolean isTeamLeader() { return "team_leader".equals(userType); }
}
package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName; // Importar esto
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeamModel implements Serializable {

    public String id;
    public String teamName;
    public String discipline;
    public String category;
    public String coachId;
    public String coachName;
    public String coachEmail;
    public String coachPhone;

    public List<TeamMember> members;
    public int minMembers;
    public int maxMembers;
    public int currentMembers;
    public String status;

    // --- CAMBIO CRÍTICO: Campos privados para obligar el uso de setters ---
    @ServerTimestamp
    private Date createdAt;
    private Date lastModified;
    // ---------------------------------------------------------------------

    public String uniformColor;
    public String institution;
    public String notes;
    public List<String> enrolledEventIds;

    // Constructor vacío requerido por Firestore
    public TeamModel() {
        this.members = new ArrayList<>();
        this.enrolledEventIds = new ArrayList<>();
        this.status = "active";
        this.currentMembers = 0;
    }

    // Constructor completo
    public TeamModel(String id, String teamName, String discipline, String category,
                     String coachId, String coachName, String coachEmail, String coachPhone,
                     int minMembers, int maxMembers) {
        this.id = id;
        this.teamName = teamName;
        this.discipline = discipline;
        this.category = category;
        this.coachId = coachId;
        this.coachName = coachName;
        this.coachEmail = coachEmail;
        this.coachPhone = coachPhone;
        this.minMembers = minMembers;
        this.maxMembers = maxMembers;
        this.members = new ArrayList<>();
        this.enrolledEventIds = new ArrayList<>();
        this.currentMembers = 0;
        this.status = "active";
        this.createdAt = new Date();
        this.lastModified = new Date();
    }

    // --- GETTERS Y SETTERS INTELIGENTES CON @PropertyName ---

    @PropertyName("createdAt")
    public Date getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Object timestamp) {
        this.createdAt = convertToDate(timestamp);
    }

    @PropertyName("lastModified")
    public Date getLastModified() {
        return lastModified;
    }

    @PropertyName("lastModified")
    public void setLastModified(Object timestamp) {
        this.lastModified = convertToDate(timestamp);
    }

    // Método auxiliar de conversión robusta
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

    // --- MÉTODOS @Exclude ---

    @Exclude
    public boolean hasMinimumMembers() { return currentMembers >= minMembers; }

    @Exclude
    public boolean isFull() { return currentMembers >= maxMembers; }

    @Exclude
    public int getAvailableSpots() { return maxMembers - currentMembers; }

    // --- MÉTODOS LÓGICOS ---

    public boolean addMember(TeamMember member) {
        if (currentMembers >= maxMembers) return false;
        members.add(member);
        currentMembers++;
        this.lastModified = new Date();
        return true;
    }

    public boolean removeMember(String memberId) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).id.equals(memberId)) {
                members.remove(i);
                currentMembers--;
                this.lastModified = new Date();
                return true;
            }
        }
        return false;
    }

    public boolean enrollInEvent(String eventId) {
        if (!enrolledEventIds.contains(eventId)) {
            enrolledEventIds.add(eventId);
            this.lastModified = new Date();
            return true;
        }
        return false;
    }

    public boolean unenrollFromEvent(String eventId) {
        if (enrolledEventIds.remove(eventId)) {
            this.lastModified = new Date();
            return true;
        }
        return false;
    }

    public boolean isEnrolledInEvent(String eventId) {
        return enrolledEventIds != null && enrolledEventIds.contains(eventId);
    }

    /**
     * Clase interna para miembros
     */
    public static class TeamMember implements Serializable {
        public String id;
        public String name;
        public String email;
        public String phone;
        public int age;
        public String position;
        public String documentId;
        public boolean isCaptain;

        // Privado para forzar el uso del setter
        private Date joinedAt;

        public TeamMember() {}

        public TeamMember(String id, String name, String email, String phone, int age, String position) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.age = age;
            this.position = position;
            this.isCaptain = false;
            this.joinedAt = new Date();
        }

        @PropertyName("joinedAt")
        public Date getJoinedAt() { return joinedAt; }

        @PropertyName("joinedAt")
        public void setJoinedAt(Object timestamp) {
            if (timestamp == null) return;
            if (timestamp instanceof Date) {
                this.joinedAt = (Date) timestamp;
            } else if (timestamp instanceof Timestamp) {
                this.joinedAt = ((Timestamp) timestamp).toDate();
            } else if (timestamp instanceof Long) {
                this.joinedAt = new Date((Long) timestamp);
            }
        }
    }
}
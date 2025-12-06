package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de Equipo PERMANENTE para eventos deportivos/culturales
 * Representa un equipo creado por un coach que puede inscribirse a múltiples eventos
 */
public class TeamModel implements Serializable {

    // Identificación
    public String id;
    public String teamName;
    public String discipline; // Fútbol, Básquetbol, Voleibol, etc.
    public String category; // Varonil, Femenil, Mixto

    // Responsable del equipo (Coach/Entrenador)
    public String coachId;
    public String coachName;
    public String coachEmail;
    public String coachPhone;

    // Miembros del equipo
    public List<TeamMember> members;
    public int minMembers; // Mínimo de miembros requeridos (ej: 5 para fútbol 5)
    public int maxMembers; // Máximo de miembros permitidos (ej: 11 para fútbol 11)
    public int currentMembers; // Número actual de miembros

    // Estado del equipo
    public String status; // "active", "inactive"
    public Timestamp createdAt;
    public Timestamp lastModified;

    // Información adicional
    public String uniformColor;
    public String institution; // Escuela, empresa, comunidad que representa
    public String notes;

    // Lista de eventos a los que está inscrito (IDs)
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
        this.createdAt = Timestamp.now();
        this.lastModified = Timestamp.now();
    }

    /**
     * Agrega un miembro al equipo
     */
    public boolean addMember(TeamMember member) {
        if (currentMembers >= maxMembers) {
            return false; // Equipo lleno
        }
        members.add(member);
        currentMembers++;
        lastModified = Timestamp.now();
        return true;
    }

    /**
     * Elimina un miembro del equipo
     */
    public boolean removeMember(String memberId) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).id.equals(memberId)) {
                members.remove(i);
                currentMembers--;
                lastModified = Timestamp.now();
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si el equipo tiene el mínimo de miembros requeridos
     */
    public boolean hasMinimumMembers() {
        return currentMembers >= minMembers;
    }

    /**
     * Verifica si el equipo está lleno
     */
    public boolean isFull() {
        return currentMembers >= maxMembers;
    }

    /**
     * Obtiene espacios disponibles
     */
    public int getAvailableSpots() {
        return maxMembers - currentMembers;
    }

    /**
     * Inscribe el equipo a un evento
     */
    public boolean enrollInEvent(String eventId) {
        if (!enrolledEventIds.contains(eventId)) {
            enrolledEventIds.add(eventId);
            lastModified = Timestamp.now();
            return true;
        }
        return false; // Ya está inscrito
    }

    /**
     * Desinscribe el equipo de un evento
     */
    public boolean unenrollFromEvent(String eventId) {
        if (enrolledEventIds.remove(eventId)) {
            lastModified = Timestamp.now();
            return true;
        }
        return false; // No estaba inscrito
    }

    /**
     * Verifica si el equipo está inscrito en un evento
     */
    public boolean isEnrolledInEvent(String eventId) {
        return enrolledEventIds != null && enrolledEventIds.contains(eventId);
    }

    /**
     * Clase interna para representar un miembro del equipo
     */
    public static class TeamMember implements Serializable {
        public String id; // UID del usuario
        public String name;
        public String email;
        public String phone;
        public int age;
        public String position; // Posición en el equipo (delantero, defensa, etc.)
        public String documentId; // INE, CURP, etc.
        public boolean isCaptain;
        public Timestamp joinedAt;

        public TeamMember() {}

        public TeamMember(String id, String name, String email, String phone, int age, String position) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.age = age;
            this.position = position;
            this.isCaptain = false;
            this.joinedAt = Timestamp.now();
        }
    }
}

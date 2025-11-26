package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de Equipo para eventos deportivos/culturales
 * Representa un equipo registrado en un evento (ej: equipo de fútbol, grupo de danza)
 */
public class TeamModel {

    // Identificación
    public String id;
    public String teamName;
    public String eventId;
    public String eventName;

    // Responsable del equipo (Team Leader)
    public String leaderId;
    public String leaderName;
    public String leaderEmail;
    public String leaderPhone;

    // Miembros del equipo
    public List<TeamMember> members;
    public int minMembers; // Mínimo de miembros requeridos (ej: 5 para fútbol 5)
    public int maxMembers; // Máximo de miembros permitidos (ej: 11 para fútbol 11)
    public int currentMembers; // Número actual de miembros

    // Estado del registro
    public String status; // "confirmed", "pending", "cancelled"
    public Timestamp registeredAt;
    public Timestamp lastModified;

    // Información adicional
    public String uniformColor;
    public String institution; // Escuela, empresa, comunidad que representa
    public String notes;

    // Constructor vacío requerido por Firestore
    public TeamModel() {
        this.members = new ArrayList<>();
        this.status = "confirmed";
        this.currentMembers = 0;
    }

    // Constructor completo
    public TeamModel(String id, String teamName, String eventId, String eventName,
                     String leaderId, String leaderName, String leaderEmail, String leaderPhone,
                     int minMembers, int maxMembers) {
        this.id = id;
        this.teamName = teamName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.leaderEmail = leaderEmail;
        this.leaderPhone = leaderPhone;
        this.minMembers = minMembers;
        this.maxMembers = maxMembers;
        this.members = new ArrayList<>();
        this.currentMembers = 0;
        this.status = "confirmed";
        this.registeredAt = Timestamp.now();
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
     * Clase interna para representar un miembro del equipo
     */
    public static class TeamMember {
        public String id;
        public String name;
        public String email;
        public String phone;
        public int age;
        public String position; // Posición en el equipo (delantero, defensa, etc.)
        public String documentId; // INE, CURP, etc.
        public boolean isCaptain;

        public TeamMember() {}

        public TeamMember(String id, String name, String email, String phone, int age, String position) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.age = age;
            this.position = position;
            this.isCaptain = false;
        }
    }
}

package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * Modelo para representar la inscripción de un equipo a un evento específico
 * Esto permite que un equipo permanente se inscriba a múltiples eventos
 */
public class TeamEnrollmentModel implements Serializable {

    public String id; // ID único de la inscripción
    public String teamId;
    public String teamName;
    public String eventId;
    public String eventName;
    public String coachId;
    public String coachName;

    // Estado de la inscripción
    public String status; // "pending", "confirmed", "cancelled"
    public Timestamp enrolledAt;
    public Timestamp lastModified;

    // Pago (si el evento tiene costo)
    public boolean paid;
    public double amountPaid;
    public String paymentMethod;
    public String transactionId;
    public Timestamp paidAt;

    // Número de integrantes al momento de inscripción
    public int teamMembersCount;

    // Notas adicionales
    public String notes;

    // Constructor vacío para Firestore
    public TeamEnrollmentModel() {
        this.status = "pending";
        this.paid = false;
        this.amountPaid = 0.0;
    }

    // Constructor completo
    public TeamEnrollmentModel(String teamId, String teamName, String eventId, String eventName,
                               String coachId, String coachName, int teamMembersCount) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.coachId = coachId;
        this.coachName = coachName;
        this.teamMembersCount = teamMembersCount;
        this.status = "pending";
        this.paid = false;
        this.amountPaid = 0.0;
        this.enrolledAt = Timestamp.now();
        this.lastModified = Timestamp.now();
    }

    /**
     * Marca el pago como completado
     */
    public void markAsPaid(double amount, String method, String transactionId) {
        this.paid = true;
        this.amountPaid = amount;
        this.paymentMethod = method;
        this.transactionId = transactionId;
        this.paidAt = Timestamp.now();
        this.lastModified = Timestamp.now();
    }

    /**
     * Confirma la inscripción
     */
    public void confirm() {
        this.status = "confirmed";
        this.lastModified = Timestamp.now();
    }

    /**
     * Cancela la inscripción
     */
    public void cancel() {
        this.status = "cancelled";
        this.lastModified = Timestamp.now();
    }
}

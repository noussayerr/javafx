package org.example.entity;

import java.time.LocalDateTime;

public class Favori {

    private int id;
    private int userId;
    private int eventId;
    private LocalDateTime dateAjout;

    // Constructeurs
    public Favori() {
    }

    public Favori(int userId, int eventId) {
        this.userId = userId;
        this.eventId = eventId;
        this.dateAjout = LocalDateTime.now();
    }

    public Favori(int id, int userId, int eventId, LocalDateTime dateAjout) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.dateAjout = dateAjout;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    @Override
    public String toString() {
        return "Favori{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventId=" + eventId +
                ", dateAjout=" + dateAjout +
                '}';
    }
}

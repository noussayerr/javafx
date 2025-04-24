package org.example.entity;

public class Comment {
    private int id;
    private int eventId;
    private int userId;
    private String nomUtilisateur;

    private String content;

    // ✅ Constructeur personnalisé
    public Comment(int eventId, int userId, String content, String nomUtilisateur) {
        this.eventId = eventId;
        this.userId = userId;
        this.content = content;
        this.nomUtilisateur = nomUtilisateur;
    }

    // ✅ Getters & setters nécessaires
    public int getEventId() {
        return eventId;
    }

    public int getUserId() {
        return userId;
    }
    public int getId() {
        return id;
    }


    public String getContent() {
        return content;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }
}

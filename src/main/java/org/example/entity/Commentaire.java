package org.example.entity;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private String contenu;
    private LocalDateTime date;
    private Matiere matiere; // nullable
    private User user;       // nullable
    private String sujet;

    // Constructeurs
    public Commentaire() {}

    public Commentaire(int id, String contenu, LocalDateTime date, String sujet) {
        this.id = id;
        this.contenu = contenu;
        this.date = date;
        this.sujet = sujet;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        // Gestion de la relation bidirectionnelle
        if (this.matiere != null) {
            this.matiere.getCommentaires().remove(this);
        }
        this.matiere = matiere;
        if (matiere != null && !matiere.getCommentaires().contains(this)) {
            matiere.getCommentaires().add(this);
        }
    }

    public User getUser() {
        return user;
    }



    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }
}
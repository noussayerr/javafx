package org.example.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Commentaire {
    private int id;

    public void setUser(User user) {
        this.user = user;
    }

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
        // Handle null case for current matiere
        if (this.matiere != null && this.matiere.getCommentaires() != null) {
            this.matiere.getCommentaires().remove(this);
        }

        this.matiere = matiere;

        // Handle null case for new matiere
        if (matiere != null) {
            if (matiere.getCommentaires() == null) {
                matiere.setCommentaires(new ArrayList<>());
            }
            if (!matiere.getCommentaires().contains(this)) {
                matiere.getCommentaires().add(this);
            }
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
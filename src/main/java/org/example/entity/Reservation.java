package org.example.entity;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private LocalDateTime dateres;
    private boolean valide;
    private Evenement evenement;
    private Apprenant user;
    private String codeqr;

    // Constructeur
    public Reservation(int id, LocalDateTime dateres, boolean valide, Evenement evenement, Apprenant user, String codeqr) {
        this.id = id;
        this.dateres = dateres;
        this.valide = valide;
        this.evenement = evenement;
        this.user = user;
        this.codeqr = codeqr;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateres() {
        return dateres;
    }

    public void setDateres(LocalDateTime dateres) {
        this.dateres = dateres;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    public Apprenant getUser() {
        return user;
    }

    public void setUser(Apprenant user) {
        this.user = user;
    }

    public String getCodeqr() {
        return codeqr;
    }

    public void setCodeqr(String codeqr) {
        this.codeqr = codeqr;
    }
}

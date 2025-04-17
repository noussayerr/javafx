package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Abonnement {
    private int id;
    private String titreAbonnement;
    private int prix;
    private String description;
    private Promotion promotion;
    private Duration duration;
    private List<Apprenant> apprenants = new ArrayList<>();

    // Constructeurs
    public Abonnement() {}

    public Abonnement(int id, String titreAbonnement, int prix, String description, Duration duration) {
        this.id = id;
        this.titreAbonnement = titreAbonnement;
        this.prix = prix;
        this.description = description;
        this.duration = duration;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitreAbonnement() {
        return titreAbonnement;
    }

    public void setTitreAbonnement(String titreAbonnement) {
        this.titreAbonnement = titreAbonnement;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public List<Apprenant> getApprenants() {
        return apprenants;
    }

    public void setApprenants(List<Apprenant> apprenants) {
        this.apprenants = apprenants;
    }
}

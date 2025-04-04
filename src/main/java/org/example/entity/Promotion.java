package org.example.entity;

import java.time.LocalDate;

public class Promotion {
    private int id;
    private String titre;
    private String description;
    private int reduction;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // Constructeurs
    public Promotion() {}

    public Promotion(int id, String titre, String description, int reduction,
                     LocalDate dateDebut, LocalDate dateFin) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.reduction = reduction;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReduction() {
        return reduction;
    }

    public void setReduction(int reduction) {
        this.reduction = reduction;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        // Validation optionnelle: dateFin doit être après dateDebut
        if (dateDebut != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
        this.dateFin = dateFin;
    }

    // Méthode utilitaire pour vérifier si la promotion est active
    public boolean estActive() {
        LocalDate aujourdhui = LocalDate.now();
        return !aujourdhui.isBefore(dateDebut) && !aujourdhui.isAfter(dateFin);
    }
}
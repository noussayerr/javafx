package org.example.entity;

import java.time.LocalDate;

public class Jeux {
    private int id;
    private String nom;
    private String description;
    private String type;
    private LocalDate DoC; // Date of Creation (Date de Création)

    // Constructeurs
    public Jeux() {}

    public Jeux(int id, String nom, String description, String type, LocalDate DoC) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.DoC = DoC;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDoC() {
        return DoC;
    }

    public void setDoC(LocalDate DoC) {
        this.DoC = DoC;
    }
}

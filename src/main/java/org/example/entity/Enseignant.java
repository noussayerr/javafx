package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Enseignant extends User {
    private String specialite;
    private String experience;
    private List<Cours> cours = new ArrayList<>();

    // Constructeurs
    public Enseignant() {}

    public Enseignant(int id, String email, String password, String nom, String prenom, String specialite, String experience) {
        super(id, email, password, nom, prenom);
        this.specialite = specialite;
        this.experience = experience;
    }

    // Getters et setters
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public List<Cours> getCours() { return cours; }
    public void setCours(List<Cours> cours) { this.cours = cours; }
}
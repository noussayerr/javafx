package org.example.entity;

public class Fichier {
    private int id;
    private String nomF;
    private String urlF;
    private Cours cours; // nullable (comme dans Symfony)
    private String type;

    // Constructeurs
    public Fichier() {}

    public Fichier(int id, String nomF, String urlF, String type) {
        this.id = id;
        this.nomF = nomF;
        this.urlF = urlF;
        this.type = type;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomF() {
        return nomF;
    }

    public void setNomF(String nomF) {
        this.nomF = nomF;
    }

    public String getUrlF() {
        return urlF;
    }

    public void setUrlF(String urlF) {
        this.urlF = urlF;
    }

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        // Gestion de la relation bidirectionnelle
        if (this.cours != null) {
            this.cours.getFichiers().remove(this);
        }
        this.cours = cours;
        if (cours != null && !cours.getFichiers().contains(this)) {
            cours.getFichiers().add(this);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

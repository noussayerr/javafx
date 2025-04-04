package org.example.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cours {
    private int id;
    private String nomC;
    private String objC;
    private LocalDateTime dateC;
    private String nivC; // Débutant, Intermediaire, Avance
    private String type; // free, premium
    private Matiere matiere; // non-null
    private List<Fichier> fichiers = new ArrayList<>();
    private List<Quiz> quizzes = new ArrayList<>();
    private User user; // non-null

    // Constructeurs
    public Cours() {}

    public Cours(int id, String nomC, String objC, LocalDateTime dateC, String nivC, String type) {
        this.id = id;
        this.nomC = nomC;
        this.objC = objC;
        this.dateC = dateC;
        this.nivC = nivC;
        this.type = type;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomC() {
        return nomC;
    }

    public void setNomC(String nomC) {
        this.nomC = nomC;
    }

    public String getObjC() {
        return objC;
    }

    public void setObjC(String objC) {
        this.objC = objC;
    }

    public LocalDateTime getDateC() {
        return dateC;
    }

    public void setDateC(LocalDateTime dateC) {
        this.dateC = dateC;
    }

    public String getNivC() {
        return nivC;
    }

    public void setNivC(String nivC) {
        this.nivC = nivC;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Matiere getMatiere() {
        return matiere;
    }



    public List<Fichier> getFichiers() {
        return fichiers;
    }

    public void setFichiers(List<Fichier> fichiers) {
        this.fichiers = fichiers;
    }

    public List<Quiz> getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
    }

    public User getUser() {
        return user;
    }



    // Méthodes utilitaires pour gérer les relations
    public void addFichier(Fichier fichier) {
        if (!fichiers.contains(fichier)) {
            fichiers.add(fichier);
            fichier.setCours(this);
        }
    }

    public void removeFichier(Fichier fichier) {
        if (fichiers.contains(fichier)) {
            fichiers.remove(fichier);
            fichier.setCours(null);
        }
    }


}
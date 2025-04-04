package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Matiere {
    private int id;
    private String nomM;
    private String titreM;
    private String descM;
    private String objM;
    private String imgM;

    private List<Cours> cours;
    private List<Commentaire> commentaires;
    private List<Evalu> evalus;

    // Constructeur par défaut
    public Matiere() {
        this.id = 0;
        this.nomM = "";
        this.titreM = "";
        this.descM = "";
        this.objM = "";
        this.imgM = null;
        this.cours = new ArrayList<>();
        this.commentaires = new ArrayList<>();
        this.evalus = new ArrayList<>();
    }

    // Constructeur avec paramètres principaux
    public Matiere(String nomM, String titreM, String descM, String objM) {
        this.id = 0;
        this.nomM = nomM;
        this.titreM = titreM;
        this.descM = descM;
        this.objM = objM;
        this.imgM = null;
        this.cours = new ArrayList<>();
        this.commentaires = new ArrayList<>();
        this.evalus = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomM() {
        return nomM;
    }

    public void setNomM(String nomM) {
        this.nomM = nomM;
    }

    public String getTitreM() {
        return titreM;
    }

    public void setTitreM(String titreM) {
        this.titreM = titreM;
    }

    public String getDescM() {
        return descM;
    }

    public void setDescM(String descM) {
        this.descM = descM;
    }

    public String getObjM() {
        return objM;
    }

    public void setObjM(String objM) {
        this.objM = objM;
    }

    public String getImgM() {
        return imgM;
    }

    public void setImgM(String imgM) {
        this.imgM = imgM;
    }

    public List<Cours> getCours() {
        return cours;
    }

    public void setCours(List<Cours> cours) {
        this.cours = cours;
    }

    public List<Commentaire> getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(List<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    public List<Evalu> getEvalus() {
        return evalus;
    }

    public void setEvalus(List<Evalu> evalus) {
        this.evalus = evalus;
    }

    // Méthodes pour ajouter des éléments aux listes
    public void addCours(Cours cours) {
        this.cours.add(cours);
    }

    public void addCommentaire(Commentaire commentaire) {
        this.commentaires.add(commentaire);
    }

    public void addEvalu(Evalu evalu) {
        this.evalus.add(evalu);
    }
}
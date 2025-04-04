package org.example.entity;

public class Evalu {
    private int id;
    private int note;
    private Matiere matiere; // nullable
    private User user;       // nullable

    // Constructeurs
    public Evalu() {}

    public Evalu(int id, int note) {
        this.id = id;
        this.note = note;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        // Gestion de la relation bidirectionnelle
        if (this.matiere != null) {
            this.matiere.getEvalus().remove(this);
        }
        this.matiere = matiere;
        if (matiere != null && !matiere.getEvalus().contains(this)) {
            matiere.getEvalus().add(this);
        }
    }

    public User getUser() {
        return user;
    }


}
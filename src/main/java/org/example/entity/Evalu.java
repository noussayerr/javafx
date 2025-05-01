package org.example.entity;

import java.util.List;

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
        // Handle the current matiere
        if (this.matiere != null) {
            List<Evalu> currentEvalus = this.matiere.getEvalus();
            if (currentEvalus != null) {
                currentEvalus.remove(this);
            }
        }

        // Set the new matiere
        this.matiere = matiere;

        // Handle the new matiere
        if (matiere != null) {
            List<Evalu> newEvalus = matiere.getEvalus();
            if (newEvalus != null && !newEvalus.contains(this)) {
                newEvalus.add(this);
            }
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }


}
package org.example.entity;

public class Rep {
    private int id;
    private String textR;
    private boolean estCorrect;
    private Ques quesId;

    // Constructeur
    public Rep() {
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTextR() {
        return textR;
    }

    public void setTextR(String textR) {
        this.textR = textR;
    }

    public boolean isEstCorrect() {
        return estCorrect;
    }

    public void setEstCorrect(boolean estCorrect) {
        this.estCorrect = estCorrect;
    }

    public Ques getQuesId() {
        return quesId;
    }

    public void setQuesId(Ques quesId) {
        this.quesId = quesId;
    }
}
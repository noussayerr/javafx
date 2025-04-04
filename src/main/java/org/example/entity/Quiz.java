package org.example.entity;
import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private int id;
    private String titreQ;
    private Cours cours_id;
    private List<Ques> ques;

    public Quiz() {
        this.ques = new ArrayList<>();
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitreQ() {
        return titreQ;
    }

    public void setTitreQ(String titreQ) {
        this.titreQ = titreQ;
    }

    public Cours getCours_id() {
        return cours_id;
    }

    public void setCours_id(Cours cours_id) {
        this.cours_id = cours_id;
    }

    public List<Ques> getQues() {
        return ques;
    }

    public void setQues(List<Ques> ques) {
        this.ques = ques;
    }

    // Méthodes utilitaires pour gérer la relation bidirectionnelle
    public void addQues(Ques que) {
        ques.add(que);
        que.setQuiz_id(this);
    }

    public void removeQues(Ques que) {
        ques.remove(que);
        que.setQuiz_id(null);
    }
}
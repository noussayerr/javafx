package org.example.entity;


import java.util.ArrayList;
import java.util.List;

public class Ques {
    private int id;
    private String textQ;
    private String typeQu;
    private Quiz quiz_id;
    private List<Rep> reps;

    public Ques() {
        this.reps = new ArrayList<>();
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTextQ() {
        return textQ;
    }

    public void setTextQ(String textQ) {
        this.textQ = textQ;
    }

    public String getTypeQu() {
        return typeQu;
    }

    public void setTypeQu(String typeQu) {
        this.typeQu = typeQu;
    }

    public Quiz getQuiz_id() {
        return quiz_id;
    }

    public void setQuiz_id(Quiz quiz_id) {
        this.quiz_id = quiz_id;
    }

    public List<Rep> getReps() {
        return reps;
    }

    public void setReps(List<Rep> reps) {
        this.reps = reps;
    }


}

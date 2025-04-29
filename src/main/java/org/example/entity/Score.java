package org.example.entity;

public class Score {
    private int id;
    private int highScore;
    private User user;
    private Jeux jeux;

    // Constructeur
    public Score(int id, int highScore, User user, Jeux jeux) {
        this.id = id;
        this.highScore = highScore;
        this.user = user;
        this.jeux = jeux;
    }

    public Score() {

    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Jeux getJeux() {
        return jeux;
    }

    public void setJeux(Jeux jeux) {
        this.jeux = jeux;
    }
}

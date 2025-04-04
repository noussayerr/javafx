package org.example.entity;

public class Rating {
    private int id;
    private int stars;
    private Event event;
    private Apprenant user;

    // Constructeur
    public Rating() {
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Apprenant getUser() {
        return user;
    }

    public void setUser(Apprenant user) {
        this.user = user;
    }
}
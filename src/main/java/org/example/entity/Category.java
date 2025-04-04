package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private int id;
    private String nom;
    private String description; // nullable
    private String image;      // nullable
    private List<Event> events = new ArrayList<>();

    // Constructeurs
    public Category() {}

    public Category(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    // Méthode utilitaire pour gérer la relation bidirectionnelle
    public void addEvent(Event event) {
        if (!events.contains(event)) {
            events.add(event);
            event.setCategory(this);
        }
    }

    public void removeEvent(Event event) {
        if (events.contains(event)) {
            events.remove(event);
            event.setCategory(null);
        }
    }
}

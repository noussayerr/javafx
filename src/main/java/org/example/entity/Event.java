package org.example.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    private String nom;
    private String description;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String lieu;
    private String image; // nullable
    private Category category; // non-null
    private List<Apprenant> apprenants = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    // Constructeurs
    public Event() {}

    public Event(int id, String nom, String description, LocalDate date,
                 LocalTime heureDebut, LocalTime heureFin, String lieu, Category category) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.lieu = lieu;
        this.category = category;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        // Gestion de la relation bidirectionnelle
        if (this.category != null) {
            this.category.getEvents().remove(this);
        }
        this.category = category;
        if (category != null && !category.getEvents().contains(this)) {
            category.getEvents().add(this);
        }
    }

    public List<Apprenant> getApprenants() {
        return apprenants;
    }

    public void setApprenants(List<Apprenant> apprenants) {
        this.apprenants = apprenants;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    // Méthodes utilitaires pour gérer les relations
    public void addApprenant(Apprenant apprenant) {
        if (!apprenants.contains(apprenant)) {
            apprenants.add(apprenant);
            if (!apprenant.getEvenements().contains(this)) {
                apprenant.getEvenements().add(this);
            }
        }
    }

    public void removeApprenant(Apprenant apprenant) {
        if (apprenants.contains(apprenant)) {
            apprenants.remove(apprenant);
            apprenant.getEvenements().remove(this);
        }
    }

    public void addReservation(Reservation reservation) {
        if (!reservations.contains(reservation)) {
            reservations.add(reservation);
            reservation.setEvenement(this);
        }
    }

    public void removeReservation(Reservation reservation) {
        if (reservations.contains(reservation)) {
            reservations.remove(reservation);
            reservation.setEvenement(null);
        }
    }
}
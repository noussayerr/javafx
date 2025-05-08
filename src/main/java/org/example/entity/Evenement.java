package org.example.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Evenement {
    private int id;

    private int categoryId;
    private String nomCategorie;

    private String nom;
    private String description;

    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;


    private String lieu;
    private String image;



    public Evenement() {}

    // 🔹 Constructeur sans ID
    public Evenement(int categoryId, String nom, String description, LocalDate date,
                     LocalTime heureDebut, LocalTime heureFin, String lieu, String image) {
        this.categoryId = categoryId;
        this.nom = nom;
        this.description = description;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.lieu = lieu;
        this.image = image;
    }
    public String getFormattedDate() {
        if (date != null) {
            return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        } else {
            return "Date inconnue";
        }
    }

    // 🔹 Constructeur avec ID
    public Evenement(int id, int categoryId, String nom, String description, LocalDate date,
                     LocalTime heureDebut, LocalTime heureFin,
                     String lieu, String image) {
        this.id = id;
        this.categoryId = categoryId;
        this.nom = nom;
        this.description = description;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.lieu = lieu;
        this.image = image;
    }

    // ✅ Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getTitre() {
        return this.nom;
    }

    public LocalDate getDateEvent() {
        return this.date;
    }


    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", lieu='" + lieu + '\'' +
                ", image='" + image + '\'' +
                '}';
    }


}

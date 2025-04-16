package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Apprenant extends User {
    private String niveau;
    private Abonnement abonnement;
    private List<Evenement> evenements = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private List<Reclamation> reclamations = new ArrayList<>();

    // Constructeurs
    public Apprenant() {}

    public Apprenant(int id, String email, String password, String nom, String prenom, String niveau) {
        super(id, email, password, nom, prenom);
        this.niveau = niveau;
    }

    // Getters et setters
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public Abonnement getAbonnement() { return abonnement; }
    public void setAbonnement(Abonnement abonnement) { this.abonnement = abonnement; }

    public List<Evenement> getEvenements() { return evenements; }
    public void setEvenements(List<Evenement> evenements) { this.evenements = evenements; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public List<Reclamation> getReclamations() { return reclamations; }
    public void setReclamations(List<Reclamation> reclamations) { this.reclamations = reclamations; }
}

package org.example.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String email;
    private List<String> roles = new ArrayList<>();
    private String password;
    private String nom;
    private String prenom;
    private transient  String dateNaissance;
    private String etat = "actif";
    private int telephone;
    private boolean isVerified = false;
    private String verificationToken;
    private String photoProfil;
    private int interactionsCount = 0;
    private Integer sessionsCount;
    private LocalDateTime lastActivity;

    // Constructeurs
    public User() {}

    public User(int id, String email, String password, String nom, String prenom) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.roles=new ArrayList<>();
    }
    public User(int id, String nom) {
        this.id = id;
        this.nom = nom;

    }
    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public int getTelephone() { return telephone; }
    public void setTelephone(int telephone) { this.telephone = telephone; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public int getInteractionsCount() { return interactionsCount; }
    public void setInteractionsCount(int interactionsCount) { this.interactionsCount = interactionsCount; }

    public Integer getSessionsCount() { return sessionsCount; }
    public void setSessionsCount(Integer sessionsCount) { this.sessionsCount = sessionsCount; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }


}

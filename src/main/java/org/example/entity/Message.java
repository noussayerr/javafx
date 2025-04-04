package org.example.entity;

import java.time.LocalDateTime;
import java.util.Date;

public class Message {
    private int id;
    private String content;
    private Channel channel;
    private LocalDateTime createdAt; // Utilisation de LocalDateTime au lieu de DateTimeImmutable
    private int likes;
    private Apprenant apprenant;

    // Constructeur par défaut
    public Message() {
        this.id = 0;
        this.content = "";
        this.channel = null;
        this.createdAt = LocalDateTime.now(); // Initialisation à la date/heure actuelle
        this.likes = 0; // Initialisé à 0 comme dans la version Symfony
        this.apprenant = null;
    }

    // Constructeur avec paramètres principaux
    public Message(String content, Channel channel, Apprenant apprenant) {
        this.id = 0;
        this.content = content;
        this.channel = channel;
        this.createdAt = LocalDateTime.now();
        this.likes = 0;
        this.apprenant = apprenant;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Apprenant getApprenant() {
        return apprenant;
    }

    public void setApprenant(Apprenant apprenant) {
        this.apprenant = apprenant;
    }
}
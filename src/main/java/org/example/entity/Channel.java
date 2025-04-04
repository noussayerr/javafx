package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Channel {
    private int id;
    private String name;
    private Forum forum; // Relation ManyToOne (non-null)
    private List<Message> messages = new ArrayList<>(); // Relation OneToMany

    // Constructeurs
    public Channel() {}

    public Channel(int id, String name, Forum forum) {
        this.id = id;
        this.name = name;
        this.forum = forum;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        // Gestion de la relation bidirectionnelle
        if (this.forum != null) {
            this.forum.getChannels().remove(this);
        }
        this.forum = forum;
        if (forum != null && !forum.getChannels().contains(this)) {
            forum.getChannels().add(this);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    // Méthodes utilitaires pour gérer les relations
    public void addMessage(Message message) {
        if (!messages.contains(message)) {
            messages.add(message);
            message.setChannel(this);
        }
    }

    public void removeMessage(Message message) {
        if (messages.contains(message)) {
            messages.remove(message);
            message.setChannel(null);
        }
    }
}
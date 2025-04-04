package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Forum {
    private int id;
    private String name;
    private List<Channel> channels = new ArrayList<>();

    // Constructeurs
    public Forum() {}

    public Forum(int id, String name) {
        this.id = id;
        this.name = name;
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

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    // Méthodes utilitaires pour gérer la relation bidirectionnelle
    public void addChannel(Channel channel) {
        if (!channels.contains(channel)) {
            channels.add(channel);
            channel.setForum(this);
        }
    }

    public void removeChannel(Channel channel) {
        if (channels.contains(channel)) {
            channels.remove(channel);
            channel.setForum(null);
        }
    }
}
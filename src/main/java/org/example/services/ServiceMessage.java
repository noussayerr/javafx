package org.example.services;

import org.example.entity.Apprenant;
import org.example.entity.Channel;
import org.example.entity.Message;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceMessage implements IService<Message> {

    private Connection connection;

    public ServiceMessage() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Message message) throws SQLException {
        String query = "INSERT INTO message (content, channel_id, created_at, likes, apprenant_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, message.getContent());
            stmt.setInt(2, message.getChannel().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(message.getCreatedAt()));
            stmt.setInt(4, message.getLikes());
            stmt.setInt(5, message.getApprenant().getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void modifier(Message message) throws SQLException {
        String query = "UPDATE message SET content = ?, channel_id = ?, created_at = ?, likes = ?, apprenant_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, message.getContent());
            stmt.setInt(2, message.getChannel().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(message.getCreatedAt()));
            stmt.setInt(4, message.getLikes());
            stmt.setInt(5, message.getApprenant().getId());
            stmt.setInt(6, message.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM message WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Message> afficher() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT m.*, u.nom, u.prenom, u.email FROM message m JOIN user u ON m.apprenant_id = u.id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }

    public List<Message> getMessagesByChannel(int channelId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT m.*, u.nom, u.prenom, u.email FROM message m JOIN user u ON m.apprenant_id = u.id WHERE m.channel_id = ? ORDER BY m.created_at ASC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, channelId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setContent(rs.getString("content"));
        message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        message.setLikes(rs.getInt("likes"));

        Apprenant apprenant = new Apprenant();
        apprenant.setId(rs.getInt("apprenant_id"));
        apprenant.setNom(rs.getString("nom"));
        apprenant.setPrenom(rs.getString("prenom"));
        apprenant.setEmail(rs.getString("email")); // Ensure email is set for logging purposes
        message.setApprenant(apprenant);

        Channel channel = new Channel();
        channel.setId(rs.getInt("channel_id"));
        message.setChannel(channel);

        return message;
    }
}
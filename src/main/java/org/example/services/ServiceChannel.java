package org.example.services;

import org.example.entity.Channel;
import org.example.entity.Forum;
import org.example.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceChannel {

    private Connection connection;

    public ServiceChannel() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    public void ajouter(Channel channel) throws SQLException {
        String query = "INSERT INTO channel (name, forum_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, channel.getName());
            statement.setInt(2, channel.getForum().getId());
            statement.executeUpdate();
        }
    }

    public List<Channel> afficher() throws SQLException {
        List<Channel> channels = new ArrayList<>();
        String query = "SELECT c.id, c.name, c.forum_id, f.name AS forum_name " +
                "FROM channel c JOIN forum f ON c.forum_id = f.id";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Forum forum = new Forum();
                forum.setId(resultSet.getInt("forum_id"));
                forum.setName(resultSet.getString("forum_name"));

                Channel channel = new Channel();
                channel.setId(resultSet.getInt("id"));
                channel.setName(resultSet.getString("name"));
                channel.setForum(forum);
                channels.add(channel);
            }
        }
        return channels;
    }

    public void supprimer(int channelId) throws SQLException {
        String query = "DELETE FROM channel WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, channelId);
            statement.executeUpdate();
        }
    }

    public void modifier(Channel channel) throws SQLException {
        String query = "UPDATE channel SET name = ?, forum_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, channel.getName());
            stmt.setInt(2, channel.getForum().getId());
            stmt.setInt(3, channel.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Aucun channel mis à jour, ID " + channel.getId() + " non trouvé.");
            }
        }
    }
}